package funding.startreum.domain.virtualaccount.service


import funding.startreum.domain.funding.service.FundingService
import funding.startreum.domain.project.entity.Project
import funding.startreum.domain.project.repository.ProjectRepository
import funding.startreum.domain.project.service.ProjectService
import funding.startreum.domain.transaction.entity.Transaction
import funding.startreum.domain.transaction.repository.TransactionRepository
import funding.startreum.domain.transaction.service.TransactionService
import funding.startreum.domain.transaction.transaction.TransactionNotFoundException
import funding.startreum.domain.users.repository.UserRepository
import funding.startreum.domain.virtualaccount.dto.VirtualAccountDtos
import funding.startreum.domain.virtualaccount.dto.request.AccountPaymentRequest
import funding.startreum.domain.virtualaccount.dto.request.AccountRequest
import funding.startreum.domain.virtualaccount.dto.response.AccountPaymentResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountRefundResponse
import funding.startreum.domain.virtualaccount.dto.response.AccountResponse
import funding.startreum.domain.virtualaccount.entity.VirtualAccount
import funding.startreum.domain.virtualaccount.exception.AccountNotFoundException
import funding.startreum.domain.virtualaccount.exception.NotEnoughBalanceException
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
open class VirtualAccountService(
    private val virtualAccountRepository: VirtualAccountRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val fundingService: FundingService,
    private val transactionService: TransactionService,
    private val projectService: ProjectService
) {

    fun findByName(name: String): VirtualAccountDtos {
        val user = userRepository.findByName(name).orElse(null) ?: return VirtualAccountDtos(false)
        val account = virtualAccountRepository.findByUser_UserId(user.userId).orElse(null)
        return account?.let { VirtualAccountDtos.fromEntity(it) } ?: VirtualAccountDtos(false)
    }

    fun createAccount(name: String): VirtualAccountDtos {
        val user = userRepository.findByName(name)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $name") }

        if (virtualAccountRepository.findByUser_UserId(user.userId).isPresent) {
            throw IllegalStateException("이미 계좌가 존재합니다.")
        }

        val newAccount = VirtualAccount(
            user = user,
            balance = BigDecimal.ZERO,
            fundingBlock = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        virtualAccountRepository.save(newAccount)
        return VirtualAccountDtos.fromEntity(newAccount)
    }

    @Transactional
    open fun chargeByAccountId(accountId: Int, request: AccountRequest): AccountPaymentResponse {
        val account = getAccount(accountId)
        return chargeAccount(account, request)
    }

    @Transactional
    open fun chargeByUsername(username: String, request: AccountRequest): AccountPaymentResponse {
        val account = getAccount(username)
        return chargeAccount(account, request)
    }

    private fun chargeAccount(account: VirtualAccount, request: AccountRequest): AccountPaymentResponse {
        val beforeMoney = account.balance
        account.balance = account.balance.add(request.amount)

        val transaction = transactionService.createTransaction(null, account, account, request.amount, Transaction.TransactionType.REMITTANCE)
        return AccountPaymentResponse.mapToAccountPaymentResponse(account, transaction, beforeMoney, request.amount)
    }

    fun getAccount(accountId: Int): VirtualAccount =
        virtualAccountRepository.findById(accountId)
            .orElseThrow { AccountNotFoundException(accountId) }

    fun getAccount(username: String): VirtualAccount =
        virtualAccountRepository.findByUser_Name(username)
            .orElseThrow { AccountNotFoundException(username) }

    @Transactional(readOnly = true)
    open fun getAccountInfo(accountId: Int): AccountResponse =
        AccountResponse.mapToAccountResponse(getAccount(accountId))

    @Transactional(readOnly = true)
    open fun getAccountInfo(username: String): AccountResponse =
        AccountResponse.mapToAccountResponse(getAccount(username))

    @Transactional
    open fun payment(accountId: Int, request: AccountPaymentRequest, username: String): AccountPaymentResponse {
        val project = projectService.getProject(request.projectId)
        val payerAccount = getAccount(accountId)
        val projectAccount = virtualAccountRepository.findBeneficiaryAccountByProjectId(request.projectId)
            .orElseThrow { AccountNotFoundException(accountId) }

        return processPayment(project, payerAccount, projectAccount, request, username)
    }

    @Transactional
    open fun payment(request: AccountPaymentRequest, username: String): AccountPaymentResponse {
        val project = projectService.getProject(request.projectId)
        val payerAccount = getAccount(username)
        val projectAccount = virtualAccountRepository.findBeneficiaryAccountByProjectId(request.projectId)
            .orElseThrow { AccountNotFoundException(request.projectId) }

        return processPayment(project, payerAccount, projectAccount, request, username)
    }

    private fun processPayment(
        project: Project,
        payerAccount: VirtualAccount,
        projectAccount: VirtualAccount,
        request: AccountPaymentRequest,
        username: String
    ): AccountPaymentResponse {
        val payerBalanceBefore = payerAccount.balance
        val paymentAmount = request.amount

        processAccountPayment(paymentAmount, payerAccount, projectAccount)
        project.currentFunding = project.currentFunding.add(paymentAmount)

        val funding = fundingService.createFunding(project, username, paymentAmount)
        val transaction = transactionService.createTransaction(funding, payerAccount, projectAccount, paymentAmount, Transaction.TransactionType.REMITTANCE)

        return AccountPaymentResponse.mapToAccountPaymentResponse(payerAccount, transaction, payerBalanceBefore, paymentAmount)
    }

    @Transactional
    open fun refund(payerAccountId: Int, transactionId: Int): AccountRefundResponse {
        val oldTransaction = transactionRepository.findById(transactionId)
            .orElseThrow { TransactionNotFoundException(transactionId) }

        val payerAccount = getAccount(payerAccountId)
        val projectAccount = virtualAccountRepository.findReceiverAccountByTransactionId(transactionId)
            .orElseThrow { AccountNotFoundException(transactionId) }

        val beforeMoney = payerAccount.balance
        val refundAmount = oldTransaction.amount

        processAccountPayment(refundAmount, projectAccount, payerAccount)

        val funding = oldTransaction.funding.fundingId?.let { fundingService.cancelFunding(it) }
        val newTransaction = transactionService.createTransaction(funding, projectAccount, payerAccount, refundAmount, Transaction.TransactionType.REFUND)
        val project = projectRepository.findProjectByTransactionId(transactionId)
        project.currentFunding = project.currentFunding.subtract(refundAmount)

        return AccountRefundResponse.mapToAccountRefundResponse(payerAccount, newTransaction, transactionId, refundAmount, beforeMoney)
    }

    private fun processAccountPayment(amount: BigDecimal, sourceAccount: VirtualAccount, targetAccount: VirtualAccount) {
        if (sourceAccount.balance < amount) {
            throw NotEnoughBalanceException(sourceAccount.balance)
        }
        sourceAccount.balance = sourceAccount.balance.subtract(amount)
        targetAccount.balance = targetAccount.balance.add(amount)
    }
}
