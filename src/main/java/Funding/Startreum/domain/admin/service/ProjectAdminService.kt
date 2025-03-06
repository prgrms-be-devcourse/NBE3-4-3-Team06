package funding.startreum.domain.admin.service

import funding.startreum.domain.admin.dto.ProjectAdminUpdateDto
import funding.startreum.domain.admin.repository.FundingFindRepository
import funding.startreum.domain.admin.repository.ProjectAdminRepository
import funding.startreum.domain.admin.repository.TransactionFindRepository
import funding.startreum.domain.admin.repository.VirtualAccountFindRepository
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.transaction.repository.TransactionRepository
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
open class ProjectAdminService(
    private val projectAdminRepository: ProjectAdminRepository,
    private val entityManager: EntityManager,
    private val fundingFindRepository: FundingFindRepository,
    private val transactionRepository: TransactionRepository,
    private val virtualAccountFindRepository: VirtualAccountFindRepository,
    private val transactionFindRepository: TransactionFindRepository
) {
    private val logger = LoggerFactory.getLogger(ProjectAdminService::class.java)

    /**
     * 🔹 프로젝트 승인 상태 변경
     * @param projectId 변경할 프로젝트 ID
     * @param isApproved 새로운 승인 상태
     */
    @Transactional
    fun updateApprovalStatus(projectId: Int, isApproved: Project.ApprovalStatus) {
        logger.info("🟠 프로젝트 승인 상태 변경 - projectId: $projectId, isApproved: $isApproved")

        val updatedRows = projectAdminRepository.updateApprovalStatus(projectId, isApproved)
        if (updatedRows == 0) {
            throw IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.")
        }

        entityManager.flush()
    }

    /**
     * 🔹 프로젝트 진행 상태 변경
     * @param projectId 변경할 프로젝트 ID
     * @param status 새로운 진행 상태
     */
    @Transactional
    fun updateProjectStatus(projectId: Int, status: Project.Status) {
        logger.info("🟠 프로젝트 진행 상태 변경 - projectId: $projectId, status: $status")

        val updatedRows = projectAdminRepository.updateProjectStatus(projectId, status)
        if (updatedRows == 0) {
            throw IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.")
        }

        entityManager.flush()
    }

    /**
     * 🔹 프로젝트 삭제 상태 변경
     * @param projectId 변경할 프로젝트 ID
     * @param isDeleted 삭제 여부 (true: 삭제됨)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateIsDeletedTransaction(projectId: Int, isDeleted: Boolean) {
        projectAdminRepository.updateIsDeleted(projectId, isDeleted)
        entityManager.flush()
    }

    /**
     * 🔹 프로젝트 실패 시 후원금을 환불 처리
     * @param project 실패한 프로젝트
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun processRefunds(project: Project) {
        val projectId = project.projectId ?: throw IllegalArgumentException("프로젝트 ID가 null입니다.")
        val fundings = fundingFindRepository.findActiveFundingsByProjectId(projectId)

        for (funding in fundings) {
            val sponsorAccount = virtualAccountFindRepository.findByUser_UserId(funding.sponsor.userId)
                .orElseThrow { IllegalArgumentException("❌ 후원자의 가상 계좌를 찾을 수 없습니다.") }

            val fundingId = funding.fundingId ?: throw IllegalArgumentException("펀딩 ID가 null입니다.")
            val originalTransaction = transactionFindRepository.findByFunding_FundingId(fundingId)
                .orElseThrow { IllegalArgumentException("❌ 해당 펀딩의 결제 트랜잭션을 찾을 수 없습니다.") }

            val beneficiaryAccount = originalTransaction.receiverAccount
            val refundAmount = funding.amount

            sponsorAccount.balance = sponsorAccount.balance.add(refundAmount)
            virtualAccountFindRepository.save(sponsorAccount)

            if (beneficiaryAccount.balance < refundAmount) {
                throw IllegalStateException("❌ 수혜자 계좌의 잔액이 부족하여 환불할 수 없습니다.")
            }
            beneficiaryAccount.balance = beneficiaryAccount.balance.subtract(refundAmount)
            virtualAccountFindRepository.save(beneficiaryAccount)

            val refundTransaction = Transaction(
                funding = funding,
                admin = originalTransaction.admin,
                senderAccount = beneficiaryAccount,
                receiverAccount = sponsorAccount,
                amount = refundAmount,
                type = Transaction.TransactionType.REFUND,
                transactionDate = LocalDateTime.now()
            )

            transactionRepository.save(refundTransaction)

            funding.isDeleted = true
            fundingFindRepository.save(funding)

            logger.info("🔢 환불 완료 - 후원자 ID: ${funding.sponsor.userId}, 환불 금액: $refundAmount")
        }
    }

    /**
     * 🔹 프로젝트 승인 및 진행 상태 업데이트
     * @param projectId 프로젝트 ID
     * @param updateDto 업데이트할 승인 상태 및 진행 상태
     */
    @Transactional
    fun updateProject(projectId: Int, updateDto: ProjectAdminUpdateDto) {
        updateDto.isApproved?.let {
            updateApprovalStatus(projectId, it)
        }
        updateDto.status?.let {
            updateProjectStatus(projectId, it)
        }
    }

    /**
     * 🔹 매일 자정에 종료된 프로젝트의 상태를 자동으로 업데이트
     * - 목표 금액을 달성하면 SUCCESS
     * - 목표 금액을 달성하지 못하면 FAILED (환불 실행)
     */

    /**
     * 🔹 테스트용 스케줄러 - 매 30초, 40초에 실행 (테스트 용도)
     */
    /*
    @Scheduled(cron = "30 * * * * *") //  30초마다 실행
    @Scheduled(cron = "40 * * * * *") //  40초마다 실행
    */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00시 00분에 실행
    @Transactional
    fun autoUpdateProjectStatus() {
        val now = LocalDateTime.now().withNano(0) // 밀리초 제거하여 비교 정확도 높이기

        logger.info("🔎 현재 시간 기준: {}", now)

        // ✅ 현재 실행할 조건을 로그로 출력
        logger.info("🔎 실행할 조건 - end_date < {}, status not in ({}, {})", now, Project.Status.SUCCESS, Project.Status.FAILED)

        // SUCCESS, FAILED 상태가 아닌 프로젝트만 조회
        val expiredProjects = projectAdminRepository.findByEndDateBeforeAndStatusNotIn(
            now, listOf(Project.Status.SUCCESS, Project.Status.FAILED)
        )

        // ✅ 조회된 프로젝트 목록 확인
        logger.info("🔎 조회된 만료 프로젝트 수: {}", expiredProjects.size)

        if (expiredProjects.isNotEmpty()) {
            logger.info("⏳ [자동 업데이트] 종료된 프로젝트 ${expiredProjects.size}개 상태 업데이트")

            for (project in expiredProjects) {
                if (project.currentFunding >= project.fundingGoal) {
                    // 목표 금액 달성 -> 성공 처리
                    updateProjectStatus(project.projectId!!, Project.Status.SUCCESS)
                    logger.info("✅ 프로젝트 성공 - projectId: ${project.projectId}, title: ${project.title}")
                } else {
                    // 목표 금액 미달 -> 실패 처리 및 환불 실행
                    updateProjectStatus(project.projectId!!, Project.Status.FAILED)
                    processRefunds(project)
                    logger.info("🔴 프로젝트 실패 - projectId: ${project.projectId}, title: ${project.title}")
                }
            }
        } else {
            logger.info("✅ [자동 업데이트] 상태 변경할 프로젝트 없음")
        }
    }


    /**
     * 🔹 매일 새벽 1시에 SUCCESS 또는 FAILED 상태인 프로젝트를 자동으로 거절
     */
    /*@Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 실행*/
    /*@Scheduled(cron = "40 * * * * *") //  40초마다 실행 */

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 실행
    @Transactional
    fun autoRejectFailedProjects() {
        logger.info("🔎 [자동 승인 거절] SUCCESS 또는 FAILED 상태 프로젝트 조회 중...")

        val rejectedProjects = projectAdminRepository.findByStatusInAndIsApproved(
            listOf(Project.Status.SUCCESS, Project.Status.FAILED), Project.ApprovalStatus.APPROVE
        )

        logger.info("🔎 승인 거절할 프로젝트 수: {}", rejectedProjects.size)

        for (project in rejectedProjects) {
            updateApprovalStatus(project.projectId!!, Project.ApprovalStatus.REJECTED)
            logger.info("❌ 프로젝트 승인 거절 - projectId: ${project.projectId}, title: ${project.title}")
        }

        logger.info("✅ [자동 승인 거절] 작업 완료")
    }


    }
