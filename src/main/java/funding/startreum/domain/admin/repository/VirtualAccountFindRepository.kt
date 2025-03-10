package funding.startreum.domain.admin.repository

import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VirtualAccountFindRepository : JpaRepository<VirtualAccount, Int> {

    // 후원자 ID로 가상 계좌 조회
    fun findByUser_UserId(userId: Int): Optional<VirtualAccount>
}
