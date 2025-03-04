package funding.startreum.domain.inquiry;


import funding.startreum.common.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/beneficiary")
@RequiredArgsConstructor
public class BeneficiaryInquiryController {

    private final InquiryService inquiryService;
    private final JwtUtil jwtUtil;

    @PostMapping("/inquiries")
    @PreAuthorize("hasRole('BENEFICIARY')")
    public ResponseEntity<InquiryResponse> createInquiry(@RequestHeader("Authorization") String token, @RequestBody @Valid InquiryRequest inquiryRequest) {
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        InquiryResponse response = inquiryService.createInquiry(email, inquiryRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
