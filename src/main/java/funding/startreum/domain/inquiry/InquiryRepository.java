package funding.startreum.domain.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByUserEmailOrderByCreatedAtDesc(String email);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}
