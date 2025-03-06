package funding.startreum.domain.virtualaccount.service

import funding.startreum.domain.users.repository.UserRepository
import funding.startreum.domain.virtualaccount.dto.VirtualAccountDtos
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse.Companion.mapToAccountResponse
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime


@Service
open class AccountQueryService(
    private val virtualAccountRepository: VirtualAccountRepository,
    private val userRepository: UserRepository,
) {

    /**
     * 사용자의 계좌 정보를 가져와 DTO로 반환
     */
    fun findByName(name: String): VirtualAccountDtos {
        val user = userRepository.findByName(name).orElse(null)
            ?: return VirtualAccountDtos(false) // 계좌 없음 응답

        val account = virtualAccountRepository.findByUser_UserId(user.userId).orElse(null)
        return if (account != null) VirtualAccountDtos.fromEntity(account) else VirtualAccountDtos(false)
    }

    /**
     * 계좌 생성
     */
    fun createAccount(name: String): VirtualAccountDtos {
        val user = userRepository.findByName(name).orElseThrow {
            IllegalArgumentException(
                "사용자를 찾을 수 없습니다: $name"
            )
        }

        // 이미 계좌가 있는지 확인
        check(!virtualAccountRepository.findByUser_UserId(user.userId).isPresent) { "이미 계좌가 존재합니다." }

        val newAccount = VirtualAccount()
        newAccount.user = user
        newAccount.balance = BigDecimal.ZERO // 초기 잔액 0원
        newAccount.fundingBlock = false // 기본적으로 펀딩 차단 없음
        newAccount.createdAt = LocalDateTime.now()
        newAccount.updatedAt = LocalDateTime.now()

        virtualAccountRepository.save(newAccount)
        return VirtualAccountDtos.fromEntity(newAccount)
    }

    /**
     * 계좌를 조회합니다. (계좌 ID 기반)
     *
     * @param accountId 조회할 계좌 ID
     * @return 조회한 계좌를 반환합니다.
     */
    fun getAccountByAccountId(accountId: Int): VirtualAccount {
        return virtualAccountRepository.findById(accountId)
            .orElseThrow { AccountNotFoundException(accountId) }
    }

    /**
     * 계좌를 조회합니다. (username 기반)
     *
     * @param username 조회할 유저 ID
     * @return 조회한 계좌를 반환합니다.
     */
    fun getAccountByUsername(username: String): VirtualAccount {
        return virtualAccountRepository.findByUser_Name(username)
            .orElseThrow {
                AccountNotFoundException(
                    username
                )
            }
    }

    /**
     * 프로젝트의 소유자 계좌를 조회합니다.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 소유자 게좌
     */
    @Transactional(readOnly = true)
    open fun getAccountByProjectId(projectId: Int): VirtualAccount {
        return virtualAccountRepository.findBeneficiaryAccountByProjectId(projectId)
            .orElseThrow { IllegalArgumentException("해당 프로젝트의 수혜자를 찾을 수 없습니다.") }
    }

    /**
     * 거래내역의 수신자 계좌를 조회합니다.
     *
     * @param transactionId 거래내역 ID
     * @return 프로젝트 소유자 게좌
     */
    @Transactional(readOnly = true)
    open fun getReceiverAccountByTransactionId(transactionId: Int): VirtualAccount {
        return virtualAccountRepository.findReceiverAccountByTransactionId(transactionId)
            .orElseThrow {
                AccountNotFoundException(
                    transactionId
                )
            }
    }


    /**
     * 계좌를 조회합니다. (계좌 ID 기반)
     *
     * @param accountId 조회할 계좌 ID
     * @return 조회한 계좌의 정보 DTO를 반환합니다.
     */
    @Transactional(readOnly = true)
    open fun getAccountInfo(accountId: Int): AccountResponse {
        return mapToAccountResponse(getAccountByAccountId(accountId))
    }

    /**
     * 계좌를 조회합니다. (username 기반)
     *
     * @param username 현재 로그인한 유저의 이름
     * @return 조회한 계좌의 정보 DTO를 반환합니다.
     */
    @Transactional(readOnly = true)
    open fun getAccountInfo(username: String): AccountResponse {
        return mapToAccountResponse(getAccountByUsername(username))
    }
}