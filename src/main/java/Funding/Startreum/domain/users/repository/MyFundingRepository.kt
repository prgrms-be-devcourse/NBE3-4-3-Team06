package funding.startreum.domain.users.repository

import funding.startreum.domain.funding.entity.Funding
import funding.startreum.domain.users.dto.MyFundingResponseDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MyFundingRepository : JpaRepository<Funding, Int> {

    @Query(
        """
        SELECT 
            p.title as projectTitle, 
            p.status as projectStatus, 
            f.amount as fundingAmount, 
            f.fundedAt as fundedAt,
            CASE 
                WHEN t.type = funding.startreum.domain.transaction.entity.Transaction.TransactionType.REMITTANCE THEN '송금 완료' 
                ELSE '환불' 
            END as transactionStatus
        FROM Funding f 
        JOIN f.project p 
        LEFT JOIN f.transactions t 
        WHERE f.sponsor.userId = :sponsorId AND f.isDeleted = false
        """
    )
    fun findMyFundingsBySponsorId(@Param("sponsorId") sponsorId: Int): List<MyFundingResponseDTO>
}
