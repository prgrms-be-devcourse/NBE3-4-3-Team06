package funding.startreum.domain.inquiry

import funding.startreum.domain.users.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class InquiryService(
    private val inquiryRepository: InquiryRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    open fun createInquiry(email: String, request: InquiryRequest): InquiryResponse {
        return try {
            // 요청 필드 검증
            if (request.title.isNullOrBlank() || request.content.isNullOrBlank()) {
                return InquiryResponse.error(400, "문의 내용 불러오기에 실패했습니다. 필수 필드를 확인해주세요.")
            }

            val user = userRepository.findByEmail(email)
                .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

            val inquiry = inquiryRepository.save(
                Inquiry(
                    title = request.title,
                    content = request.content,
                    status = Inquiry.Status.PENDING,
                    user = user,
                    createdAt = LocalDateTime.now()
                )
            )

            val data = InquiryResponse.Data(
                inquiry.inquiryId,
                inquiry.title,
                inquiry.content,
                inquiry.status,
                inquiry.createdAt
            )

            InquiryResponse.success(data)

        } catch (e: IllegalArgumentException) {
            InquiryResponse.error(400, e.message ?: "잘못된 요청입니다.")
        } catch (e: Exception) {
            InquiryResponse.error(500, "문의 생성 중 오류가 발생했습니다.")
        }
    }
}
