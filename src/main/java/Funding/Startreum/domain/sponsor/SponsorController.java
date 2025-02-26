package funding.startreum.domain.sponsor;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.inquiry.InquiryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sponsor")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorService sponsorService;
    private final JwtUtil jwtUtil;

    @GetMapping("/sponsoredList")
    public ResponseEntity<SponListResponse> getFundingList(
            @RequestHeader("Authorization") String token,
            @PageableDefault(size = 5) Pageable pageable) {

        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        SponListResponse response = sponsorService.getFundingList(email, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/funding")
    public ResponseEntity<FudingAttendResponse> getFundingAttend(
            @RequestHeader("Authorization") String token,
            @RequestBody FudingAttendResponse.FundingRequest request) {

        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        FudingAttendResponse response = sponsorService.getAttendFunding(email, request.projectId());
        return ResponseEntity.ok(response);
    }
}
