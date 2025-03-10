package funding.startreum.domain.funding.repository

import funding.startreum.domain.funding.entity.Funding
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface FundingRepository : JpaRepository<Funding, Int> {

    @Query(
        """
        SELECT f FROM Funding f
        JOIN FETCH f.project p
        WHERE f.sponsor.email = :email
        ORDER BY f.fundedAt DESC
    """
    )
    fun findBySponsorEmail(
        @Param("email") email: String,
        pageable: Pageable
    ): Page<Funding>

    fun findByFundingId(fundingId: Int): Optional<Funding>
}