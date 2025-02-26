package funding.startreum.domain.sponsor;

import funding.startreum.domain.funding.repository.FundingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import funding.startreum.domain.funding.entity.Funding;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SponsorService {

    private final FundingRepository fundingRepository;

    // 후원 목록 조회 로직
    @Transactional
    public SponListResponse getFundingList(String username, Pageable pageable) {

        try {
            if (username == null || username.isBlank()) {
                return SponListResponse.error(400, "후원 목록 조회에 실패했습니다. 필수 필드를 확인해주세요.");
            }

            Page<Funding> fundingPage = fundingRepository.findBySponsorEmail(username, pageable);

            if (fundingPage.getTotalElements() == 0) {
                return SponListResponse.error(404, "해당 프로젝트를 찾을 수 없습니다.");
            }

            var fundings = fundingPage.getContent().stream()
                    .map(funding -> new SponListResponse.Funding(
                            funding.getFundingId(),
                            funding.getProject().getProjectId(),
                            funding.getProject().getTitle(),
                            funding.getReward().getRewardId(),
                            funding.getAmount().doubleValue(),
                            funding.getProject().getCreatedAt()
                    ))
                    .toList();

            var pagination = new SponListResponse.Pagination(
                    (int) fundingPage.getTotalElements(),
                    pageable.getPageNumber() + 1,
                    pageable.getPageSize()
            );

            return SponListResponse.success(fundings, pagination);
        } catch (IllegalArgumentException e) {
            return SponListResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return SponListResponse.error(500, "후원 목록 조회 중 오류가 발생했습니다.");
        }

    }

    // 후원 참여 로직
    @Transactional
    public FudingAttendResponse getAttendFunding(String email, Integer fundingId) {
        try {
            Funding funding = fundingRepository.findById(fundingId)
                    .orElseThrow(() -> new IllegalArgumentException("후원 정보 불러오기에 실패했습니다. 필수 필드를 확인해주세요."));

            if (!funding.getSponsor().getEmail().equals(email)) {
                return FudingAttendResponse.error(403, "해당 후원에 대한 접근 권한이 없습니다.");
            }

            if (funding.getAmount() != null && funding.getAmount().doubleValue() < 0) {
                return FudingAttendResponse.error(442, "0보다 큰 숫자를 입력하세요.");
            }

            var fundingAttend = new FudingAttendResponse.FudingAttend(
                    funding.getFundingId(),
                    funding.getProject().getProjectId(),
                    funding.getProject().getTitle(),
                    funding.getAmount().doubleValue(),
                    funding.getReward().getRewardId(),
                    funding.getFundedAt()
            );

            var data = new FudingAttendResponse.Data(fundingAttend);
            return FudingAttendResponse.success(data);
        } catch (IllegalArgumentException e) {
            return FudingAttendResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return FudingAttendResponse.error(500, "후원 참여 처리 중 오류가 발생했습니다.");
        }
    }
}
