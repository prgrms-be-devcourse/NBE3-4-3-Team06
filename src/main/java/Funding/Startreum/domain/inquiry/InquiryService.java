package Funding.Startreum.domain.inquiry;

import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponse createInquiry(String email, InquiryRequest request) {
        try {
            if (request.title() == null || request.title().isBlank() ||
            request.content() == null || request.content().isBlank()) {
                return InquiryResponse.error(400, "문의 내용 불러오기에 실패했습니다. 필수 필드를 확인해주세요.");
            }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Inquiry inquiry = Inquiry.builder()
                .title(request.title())
                .content(request.content())
                .status(Inquiry.Status.PENDING)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        inquiry = inquiryRepository.save(inquiry);

        var data = new InquiryResponse.Data(
                inquiry.getInquiryId(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getCreatedAt()
        );

        return InquiryResponse.success(data);

        } catch (IllegalArgumentException e) {
            return InquiryResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return InquiryResponse.error(500, "문의 생성 중 오류가 발생했습니다.");
        }
    }
}
