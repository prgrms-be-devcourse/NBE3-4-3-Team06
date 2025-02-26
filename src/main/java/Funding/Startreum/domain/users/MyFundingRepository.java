package funding.startreum.domain.users;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.users.MyFundingResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyFundingRepository extends JpaRepository<Funding, Integer> {

    @Query("SELECT new funding.startreum.domain.users.MyFundingResponseDTO(" +
            "p.title, p.status, f.amount, f.fundedAt, " +
            "CASE WHEN t.type = funding.startreum.domain.transaction.entity.Transaction.TransactionType.REMITTANCE THEN '송금 완료' ELSE '환불' END) " +
            "FROM Funding f " +
            "JOIN f.project p " +
            "LEFT JOIN f.transactions t " +
            "WHERE f.sponsor.userId = :sponsorId AND f.isDeleted = false")
    List<MyFundingResponseDTO> findMyFundingsBySponsorId(@Param("sponsorId") Integer sponsorId);



}