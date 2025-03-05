package funding.startreum.domain.virtualaccount.exception

import funding.startreum.common.util.ApiResponse
import funding.startreum.domain.funding.exception.FundingNotFoundException
import funding.startreum.domain.transaction.transaction.TransactionNotFoundException
import funding.startreum.domain.virtualaccount.controller.VirtualAccountController
import jakarta.persistence.EntityNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [VirtualAccountController::class])
class VirtualAccountExceptionHandler {

    @ExceptionHandler(
        AccountNotFoundException::class,
        NotEnoughBalanceException::class,
        TransactionNotFoundException::class,
        FundingNotFoundException::class,
        EntityNotFoundException::class
    )
    fun handleException(e: RuntimeException): ResponseEntity<ApiResponse<Void>> {
        val status = STATUS_MAP.getOrDefault(e.javaClass, HttpStatus.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(status).body(ApiResponse.error(e.message))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException?): ResponseEntity<ApiResponse<Void>> {
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("금액을 확인해주세요."))
    }

    companion object {
        private val STATUS_MAP: Map<Class<out RuntimeException?>, HttpStatus> = java.util.Map.of(
            AccountNotFoundException::class.java, HttpStatus.NOT_FOUND,
            NotEnoughBalanceException::class.java, HttpStatus.BAD_REQUEST,
            TransactionNotFoundException::class.java, HttpStatus.NOT_FOUND,
            FundingNotFoundException::class.java, HttpStatus.NOT_FOUND,
            EntityNotFoundException::class.java, HttpStatus.NOT_FOUND
        )
    }
}
