package funding.startreum.domain.admin;

import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VirtualAccountFindRepository extends JpaRepository<VirtualAccount, Integer> {

    // 후원자 ID로 가상 계좌 조회
    Optional<VirtualAccount> findByUser_UserId(Integer userId);
}
