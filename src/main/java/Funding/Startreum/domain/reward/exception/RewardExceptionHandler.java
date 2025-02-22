package Funding.Startreum.domain.reward.exception;

import Funding.Startreum.common.util.ApiResponse;
import Funding.Startreum.domain.reward.controller.RewardRestController;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RewardRestController.class)
public class RewardExceptionHandler {

    @ExceptionHandler({EntityNotFoundException.class,})
    public ResponseEntity<ApiResponse<Void>> handleException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
    }

}
