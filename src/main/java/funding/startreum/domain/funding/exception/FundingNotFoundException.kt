package funding.startreum.domain.funding.exception

class FundingNotFoundException(fundingId: Int) : RuntimeException("펀딩 내역을 찾을 수 없습니다. 펀딩 ID: : $fundingId")
