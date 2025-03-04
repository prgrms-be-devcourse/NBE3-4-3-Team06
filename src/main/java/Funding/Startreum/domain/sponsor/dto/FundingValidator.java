package funding.startreum.domain.sponsor.dto;

import funding.startreum.domain.funding.entity.Funding;
import funding.startreum.domain.sponsor.exception.FundingAccessDeniedException;
import funding.startreum.domain.sponsor.exception.InvalidFundingAmountException;
import funding.startreum.domain.sponsor.exception.InvalidUsernameException;
import org.springframework.stereotype.Component;

@Component
public class FundingValidator {
    public void validateFundingAccess(String email, Funding funding) {
        if (!funding.getSponsor().getEmail().equals(email)) {
            throw new FundingAccessDeniedException();
        }
    }

    public void validateFundingAmount(Funding funding) {
        if (funding.getAmount() != null && funding.getAmount().doubleValue() < 0) {
            throw new InvalidFundingAmountException();
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException();
        }
    }
}
