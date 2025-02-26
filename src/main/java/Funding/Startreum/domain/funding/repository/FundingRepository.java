package funding.startreum.domain.funding.repository;


import funding.startreum.domain.funding.entity.Funding;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface FundingRepository extends JpaRepository<Funding, Integer> {
    @Query("""
        SELECT f FROM Funding f
        JOIN FETCH f.project p
        WHERE f.sponsor.email = :email
        ORDER BY f.fundedAt DESC
        """)
    Page<Funding> findBySponsorEmail(@Param("email") String email, Pageable pageable);

    Optional<Funding> findByFundingId(Integer fundingId);
}
