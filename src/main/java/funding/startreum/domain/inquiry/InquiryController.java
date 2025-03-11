package funding.startreum.domain.inquiry;

import funding.startreum.common.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sponsor")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final JwtUtil jwtUtil;

    @PostMapping("/inquiries")
    public ResponseEntity<InquiryResponse> createInquiry(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid InquiryRequest request) {

        String email =jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        InquiryResponse response = inquiryService.createInquiry(email, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
