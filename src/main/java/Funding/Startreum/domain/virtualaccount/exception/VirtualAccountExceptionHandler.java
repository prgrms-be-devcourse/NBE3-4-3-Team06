package funding.startreum.domain.virtualaccount.exception;

import funding.startreum.common.util.ApiResponse;
import funding.startreum.domain.funding.exception.FundingNotFoundException;
import funding.startreum.domain.transaction.transaction.TransactionNotFoundException;
import funding.startreum.domain.virtualaccount.controller.VirtualAccountController;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = VirtualAccountController.class)
public class VirtualAccountExceptionHandler {

    private static final Map<Class<? extends RuntimeException>, HttpStatus> STATUS_MAP = Map.of(
            AccountNotFoundException.class, HttpStatus.NOT_FOUND,
            NotEnoughBalanceException.class, HttpStatus.BAD_REQUEST,
            TransactionNotFoundException.class, HttpStatus.NOT_FOUND,
            FundingNotFoundException.class, HttpStatus.NOT_FOUND,
            EntityNotFoundException.class,  HttpStatus.NOT_FOUND
    );

    @ExceptionHandler({
            AccountNotFoundException.class,
            NotEnoughBalanceException.class,
            TransactionNotFoundException.class,
            FundingNotFoundException.class,
            EntityNotFoundException.class,
    })
    public ResponseEntity<ApiResponse<Void>> handleException(RuntimeException e) {
        HttpStatus status = STATUS_MAP.getOrDefault(e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(status).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("금액을 확인해주세요."));
    }

}
