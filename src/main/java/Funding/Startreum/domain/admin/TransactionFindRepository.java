package Funding.Startreum.domain.admin;

import Funding.Startreum.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionFindRepository extends JpaRepository<Transaction, Integer> {

    // 펀딩 ID로 트랜잭션 조회
    Optional<Transaction> findByFunding_FundingId(Integer fundingId);
}