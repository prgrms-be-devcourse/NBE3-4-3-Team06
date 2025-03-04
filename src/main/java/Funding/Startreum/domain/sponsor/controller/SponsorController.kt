package funding.startreum.domain.sponsor.controller

import funding.startreum.domain.sponsor.dto.response.FudingAttendResponse
import funding.startreum.domain.sponsor.dto.response.FudingAttendResponse.FundingRequest
import funding.startreum.domain.sponsor.dto.response.SponListResponse
import funding.startreum.domain.sponsor.service.AuthService
import funding.startreum.domain.sponsor.service.SponsorService
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sponsor")
@RequiredArgsConstructor
class SponsorController {
    private val sponsorService: SponsorService? = null
    private val authService: AuthService? = null

    @GetMapping("/sponsoredList")
    fun getFundingList(
        @RequestHeader("Authorization") token: String,
        @PageableDefault(size = 5) pageable: Pageable?
    ): ResponseEntity<SponListResponse> {
        val email = authService!!.extractEmail(token)
        return ResponseEntity.ok(sponsorService!!.getFundingList(email, pageable))
    }

    @PostMapping("/funding")
    fun getFundingAttend(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: FundingRequest
    ): ResponseEntity<FudingAttendResponse> {
        val email = authService!!.extractEmail(token)
        return ResponseEntity.ok(sponsorService!!.getAttendFunding(email, request.projectId))
    }
}
