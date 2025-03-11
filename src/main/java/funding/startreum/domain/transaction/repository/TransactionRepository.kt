package funding.startreum.domain.transaction.repository

import funding.startreum.domain.transaction.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int>
