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

    @Transactional
    open fun updateApprovalStatus(projectId: Int, isApproved: Project.ApprovalStatus) {
        println("🟠 updateApprovalStatus() 실행됨 - projectId: $projectId, isApproved: $isApproved")

        val updatedRows = projectAdminRepository.updateApprovalStatus(projectId, isApproved)
        if (updatedRows == 0) {
            throw IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.")
        }

        entityManager.flush()

        val project = projectAdminRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("❌ 해당 프로젝트를 찾을 수 없습니다.") }

        println("🟠 DB 저장 후 isApproved 값: ${project.isApproved}")

        if (project.isApproved == Project.ApprovalStatus.REJECTED) {
            println("🔢 프로젝트 승인 거절 -> isDeleted 변경 실행")
            updateIsDeletedTransaction(projectId, true)
        }
    }

    @Transactional
    open fun updateProjectStatus(projectId: Int, status: Project.Status) {
        println("🟠 updateProjectStatus() 실행됨 - projectId: $projectId, status: $status")

        val updatedRows = projectAdminRepository.updateProjectStatus(projectId, status)
        if (updatedRows == 0) {
            throw IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.")
        }

        entityManager.flush()

        val project = projectAdminRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("❌ 해당 프로젝트를 찾을 수 없습니다.") }

        println("🟠 DB 저장 후 status 값: ${project.status}")

        when (status) {
            Project.Status.SUCCESS -> {
                println("✅ 프로젝트 성공 - 후원 차단 및 삭제 처리")
                updateIsDeletedTransaction(projectId, true)
            }
            Project.Status.FAILED -> {
                println("🔢 프로젝트 실패 -> 후원자 환불 처리 및 삭제 처리 실행")
                updateIsDeletedTransaction(projectId, true)
                processRefunds(project)
            }
            else -> println("ℹ️ 프로젝트 상태 변경됨 - 추가 조치 없음")
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun updateIsDeletedTransaction(projectId: Int, isDeleted: Boolean) {
        projectAdminRepository.updateIsDeleted(projectId, isDeleted)
        entityManager.flush()
        val projectAfterUpdate = projectAdminRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("❌ 해당 프로젝트를 찾을 수 없습니다.") }
        println("🟠 업데이트 후 isDeleted 값: ${projectAfterUpdate.isDeleted}")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun processRefunds(project: Project) {
        val projectId = project.projectId ?: throw IllegalArgumentException("프로젝트 ID가 null입니다.") // ✅ 널 체크 후 예외 처리
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

            println("🔢 환불 완료 - 후원자 ID: ${funding.sponsor.userId}, 환불 금액: $refundAmount")
        }
    }


    @Transactional
    open fun updateProject(projectId: Int, updateDto: ProjectAdminUpdateDto) {
        updateDto.isApproved?.let { updateApprovalStatus(projectId, it) }
        updateDto.status?.let { updateProjectStatus(projectId, it) }
    }
}
