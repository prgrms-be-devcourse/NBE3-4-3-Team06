package funding.startreum.domain.transaction.transaction

class TransactionNotFoundException(transactionId: Int) :
    RuntimeException("해당 거래 내역을 찾을 수 없습니다 : $transactionId")
