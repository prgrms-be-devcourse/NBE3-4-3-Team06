package funding.startreum.domain.virtualaccount.dto

import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import lombok.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
data class VirtualAccountDtos(
    var accountId: Int? = null,
    var accountExists: Boolean, // 계좌 존재 여부
    var username: String? = null, // 사용자 이름
    var balance: BigDecimal? = null, // 잔액
    var createdAt: LocalDateTime? = null, // 계좌 생성 날짜
    var fundingBlocked: Boolean? = null // 펀딩 차단 여부
) {

    // 계좌가 없는 경우를 위한 생성자
    constructor(accountExists: Boolean) : this(
        accountId = null,
        accountExists = accountExists,
        username = null,
        balance = null,
        createdAt = null,
        fundingBlocked = null
    )

    constructor(account: VirtualAccount) : this(
        accountId = account.accountId,
        accountExists = true,
        username = account.user.name,
        balance = account.balance,
        createdAt = account.createdAt,
        fundingBlocked = account.fundingBlock
    )

    companion object {
        fun fromEntity(account: VirtualAccount): VirtualAccountDtos =
            VirtualAccountDtos(
                accountId = account.accountId,
                accountExists = true,
                username = account.user.name,
                balance = account.balance,
                createdAt = account.createdAt,
                fundingBlocked = account.fundingBlock
            )
    }
}