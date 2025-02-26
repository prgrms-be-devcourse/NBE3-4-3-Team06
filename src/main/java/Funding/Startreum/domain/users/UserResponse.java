package funding.startreum.domain.users;

import java.time.LocalDateTime;

// 응답용 DTO
public record UserResponse(

        String name,
        String email,
        User.Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}