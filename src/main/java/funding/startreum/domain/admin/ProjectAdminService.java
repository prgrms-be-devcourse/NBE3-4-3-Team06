package funding.startreum.domain.admin;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.repository.TransactionRepository;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectAdminService {

    private final ProjectAdminRepository projectAdminRepository;
    private final EntityManager entityManager;
    private final funding.startreum.domain.admin.FundingFindRepository fundingFindRepository;
    private final TransactionRepository transactionRepository;
    private final funding.startreum.domain.admin.VirtualAccountFindRepository vrtualAccountFindRepository;
    private final funding.startreum.domain.admin.TransactionFindRepository transactionFindRepository;

    public ProjectAdminService(ProjectAdminRepository projectAdminRepository, EntityManager entityManager,
                               funding.startreum.domain.admin.FundingFindRepository fundingFindRepository, TransactionRepository transactionRepository,
                               funding.startreum.domain.admin.VirtualAccountFindRepository vrtualAccountFindRepository, funding.startreum.domain.admin.TransactionFindRepository transactionFindRepository) {
        this.projectAdminRepository = projectAdminRepository;
        this.entityManager = entityManager;
        this.fundingFindRepository = fundingFindRepository;
        this.transactionRepository = transactionRepository;
        this.vrtualAccountFindRepository = vrtualAccountFindRepository;
        this.transactionFindRepository = transactionFindRepository;
    }

    /**
     * 프로젝트 승인 상태 변경
     */
    @Transactional
    public void updateApprovalStatus(Integer projectId, Project.ApprovalStatus isApproved) {
        System.out.println("🟠 updateApprovalStatus() 실행됨 - projectId: " + projectId + ", isApproved: " + isApproved);

        int updatedRows = projectAdminRepository.updateApprovalStatus(projectId, isApproved);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.");
        }

        entityManager.flush(); // 변경 사항 즉시 적용

        Project project = projectAdminRepository.findById(projectId).orElseThrow();
        System.out.println("🟠 DB 저장 후 isApproved 값: " + project.getIsApproved());

        if (project.getIsApproved().toString().equals("REJECTED")) {
            System.out.println("🔢 프로젝트 승인 거절 -> isDeleted 변경 실행");
            updateIsDeletedTransaction(projectId, true);
        }
    }

    /**
     * 프로젝트 진행 상태 변경
     */
    @Transactional
    public void updateProjectStatus(Integer projectId, Project.Status status) {
        System.out.println("🟠 updateProjectStatus() 실행됨 - projectId: " + projectId + ", status: " + status);

        int updatedRows = projectAdminRepository.updateProjectStatus(projectId, status);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("❌ 해당 프로젝트가 존재하지 않습니다.");
        }

        entityManager.flush(); // 변경 사항 즉시 적용

        Project project = projectAdminRepository.findById(projectId).orElseThrow();
        System.out.println("🟠 DB 저장 후 status 값: " + project.getStatus());

        // 프로젝트 상태에 따른 추가 처리
        switch (status) {
            case SUCCESS:
                System.out.println("✅ 프로젝트 성공 - 후원 차단 및 삭제 처리");
                updateIsDeletedTransaction(projectId, true);  // 성공 시에도 isDeleted = true로 설정하여 후원 차단
                break;

            case FAILED:
                System.out.println("🔢 프로젝트 실패 -> 후원자 환불 처리 및 삭제 처리 실행");
                updateIsDeletedTransaction(projectId, true);  // 실패 시에도 isDeleted = true
                processRefunds(project);  // 환불 처리
                break;

            default:
                System.out.println("ℹ️ 프로젝트 상태 변경됨 - 추가 조치 없음");
                break;
        }
    }

    /**
     * isDeleted 값을 변경하는 트랜잭션
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateIsDeletedTransaction(Integer projectId, Boolean isDeleted) {
        int deletedRows = projectAdminRepository.updateIsDeleted(projectId, isDeleted);
        entityManager.flush();
        System.out.println("🟠 업데이트 후 isDeleted 값 확인");
        Project projectAfterUpdate = projectAdminRepository.findById(projectId).orElseThrow();
        System.out.println("🟠 업데이트 후 isDeleted 값: " + projectAfterUpdate.getIsDeleted());
    }

    /**
     * 환불 처리 메서드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRefunds(Project project) {
        List<Funding> fundings = fundingFindRepository.findActiveFundingsByProjectId(project.getProjectId());

        for (Funding funding : fundings) {
            VirtualAccount sponsorAccount = vrtualAccountFindRepository.findByUser_UserId(funding.getSponsor().getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("❌ 후원자의 가상 계좌를 찾을 수 없습니다."));

            Transaction originalTransaction = transactionFindRepository.findByFunding_FundingId(funding.getFundingId())
                    .orElseThrow(() -> new IllegalArgumentException("❌ 해당 펀딩의 결제 트랜잭션을 찾을 수 없습니다."));

            VirtualAccount beneficiaryAccount = originalTransaction.getReceiverAccount();  // 수혜자 계좌

            BigDecimal refundAmount = funding.getAmount();

            // 후원자 계좌에 환불 금액 추가
            sponsorAccount.setBalance(sponsorAccount.getBalance().add(refundAmount));
            vrtualAccountFindRepository.save(sponsorAccount);

            // 수혜자 계좌에서 환불 금액 차감
            if (beneficiaryAccount.getBalance().compareTo(refundAmount) < 0) {
                throw new IllegalStateException("❌ 수혜자 계좌의 잔액이 부족하여 환불할 수 없습니다.");
            }
            beneficiaryAccount.setBalance(beneficiaryAccount.getBalance().subtract(refundAmount));
            vrtualAccountFindRepository.save(beneficiaryAccount);

            // 환불 트랜잭션 생성
            Transaction refundTransaction = new Transaction();
            refundTransaction.setFunding(funding);
            refundTransaction.setAdmin(originalTransaction.getAdmin());  // 결제 당시 관리자
            refundTransaction.setSenderAccount(beneficiaryAccount);      // 수혜자 계좌(송신자)
            refundTransaction.setReceiverAccount(sponsorAccount);       // 후원자 계좌(수신자)
            refundTransaction.setAmount(refundAmount);
            refundTransaction.setType(Transaction.TransactionType.REFUND);  // 환불 기록
            refundTransaction.setTransactionDate(LocalDateTime.now());

            transactionRepository.save(refundTransaction);  // 트랜잭션 저장

            // 펀딩 기록 삭제 처리
            funding.setDeleted(true);
            fundingFindRepository.save(funding);

            System.out.println("🔢 환불 완료 - 후원자 ID: " + funding.getSponsor().getUserId() + ", 환불 금액: " + refundAmount);
        }
    }
    /**
     * 관리자용 상태 변경 (승인 및 진행 상태 모든 변경 가능)
     */
    @Transactional
    public void updateProject(Integer projectId, funding.startreum.domain.admin.ProjectAdminUpdateDto updateDto) {
        if (updateDto.getIsApproved() != null) {
            updateApprovalStatus(projectId, updateDto.getIsApproved());
        }
        if (updateDto.getStatus() != null) {
            updateProjectStatus(projectId, updateDto.getStatus());
        }
    }
}
