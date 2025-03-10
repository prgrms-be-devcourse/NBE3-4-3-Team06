package funding.startreum.domain.transaction.service;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.transaction.entity.Transaction;
import funding.startreum.domain.transaction.repository.TransactionRepository;
import funding.startreum.domain.users.repository.UserRepository;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public void transfer(String email, Integer projectId) {

    }

    /**
     * 거래 내역 생성 메서드
     *
     * @param funding         관련 펀딩 내역
     * @param senderAccount   자금 출금 계좌 (결제 시에는 결제자, 환불 시에는 프로젝트 계좌)
     * @param receiverAccount 자금 입금 계좌 (결제 시에는 프로젝트 계좌, 환불 시에는 결제자 계좌)
     * @param amount          거래 금액
     * @param type            거래 유형 (REMITTANCE 또는 REFUND)
     * @return 생성된 Transaction 객체
     */
    @Transactional
    public Transaction createTransaction(Funding funding, VirtualAccount senderAccount, VirtualAccount receiverAccount, BigDecimal amount, Transaction.TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setFunding(funding);
        transaction.setAdmin(userRepository.findByName("funding.startreum.domain.admin.entity.Admin").orElse(null));
        transaction.setSenderAccount(senderAccount);
        transaction.setReceiverAccount(receiverAccount);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionRepository.save(transaction);

        return transaction;
    }
}
