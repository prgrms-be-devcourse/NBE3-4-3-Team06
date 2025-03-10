package funding.startreum.domain.admin.repository

import funding.startreum.domain.transaction.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TransactionFindRepository : JpaRepository<Transaction, Int> {

    // 펀딩 ID로 트랜잭션 조회
    fun findByFunding_FundingId(fundingId: Int): Optional<Transaction>
}
