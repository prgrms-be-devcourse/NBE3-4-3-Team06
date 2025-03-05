package funding.startreum.domain.virtualaccount.repository

import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VirtualAccountRepository : JpaRepository<VirtualAccount, Int> {

    fun findByUser_UserId(userId: Int): Optional<VirtualAccount> // userId를 사용하여 VirtualAccount 찾기

    @Query(
        "SELECT va FROM VirtualAccount va " +
                "JOIN va.user u " +
                "JOIN u.projects p " +
                "WHERE p.projectId = :projectId"
    )
    fun findBeneficiaryAccountByProjectId(@Param("projectId") projectId: Int): Optional<VirtualAccount>

    @Query(
        "SELECT va FROM VirtualAccount va " +
                "JOIN va.user u " +
                "WHERE u.name = :username"
    )
    fun findBeneficiaryAccountByUser_Name(@Param("username") username: String): Optional<VirtualAccount>

    @Query(
        "SELECT va FROM VirtualAccount va " +
                "JOIN Transaction t ON t.receiverAccount = va " +
                "WHERE t.transactionId = :transactionId"
    )
    fun findReceiverAccountByTransactionId(@Param("transactionId") transactionId: Int): Optional<VirtualAccount>

    fun findByUser_Name(userName: String): Optional<VirtualAccount>
}
