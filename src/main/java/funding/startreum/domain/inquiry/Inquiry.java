package funding.startreum.domain.inquiry;

import funding.startreum.domain.users.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@ToString
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inquiries")
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inquiryId; // 문의 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 문의 작성자 ID

    private String title; // 문의 제목

    @Lob
    private String content; // 문의 내용

    @Enumerated(EnumType.STRING)
    private Status status; // 문의 상태

    @Lob
    private String adminResponse; // 관리자 응답 내용

    private LocalDateTime createdAt; // 문의 작성 일자

    private LocalDateTime updatedAt; // 문의 업데이트 일자

    public enum Status {
        PENDING, // 대기중
        RESOLVED // 완료
    }
}
