package funding.startreum.domain.inquiry

import funding.startreum.common.util.JwtUtil
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/beneficiary")
@PreAuthorize("hasRole('BENEFICIARY')")
public open class BeneficiaryInquiryController(
    private val inquiryService: InquiryService,
    private val jwtUtil: JwtUtil
) {
    @PostMapping("/inquiries")
    fun createInquiry(
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid inquiryRequest: InquiryRequest
    ): ResponseEntity<InquiryResponse> {
        val email = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        val response = inquiryService.createInquiry(email, inquiryRequest)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}
