package funding.startreum.util;

import funding.startreum.common.util.JwtUtil;
import funding.startreum.domain.project.entity.Project;
import funding.startreum.domain.project.repository.ProjectRepository;
import funding.startreum.domain.users.CustomUserDetailsService;
import funding.startreum.domain.users.UserService;
import funding.startreum.domain.virtualaccount.entity.VirtualAccount;
import funding.startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Optional;

public class utilMethod {

    /**
     * 가상 계좌 데이터를 설정합니다.
     *
     * @param repository   VirtualAccountRepository mock
     * @param accountId    계좌 ID
     * @param accountOwner 계좌 소유자
     */
    public static void createVirtualAccount(VirtualAccountRepository repository, int accountId, String accountOwner) {
        funding.startreum.domain.users.User user = new funding.startreum.domain.users.User();
        user.setName(accountOwner);

        VirtualAccount account = new VirtualAccount();
        account.setAccountId(accountId);
        account.setUser(user);
        account.setBalance(new BigDecimal("0.00"));

        given(repository.findById(accountId)).willReturn(Optional.of(account));
    }

    /**
     * 가상 프로젝트 데이터를 설정합니다.
     *
     * @param repository    ProjectRepository mock
     * @param projectId     프로젝트 ID
     * @param projectOwner  프로젝트 소유자
     */
    public static void createVirtualProject(ProjectRepository repository, int projectId, String projectOwner) {
        funding.startreum.domain.users.User user = new funding.startreum.domain.users.User();
        user.setName(projectOwner);

        Project project = new Project();
        project.setCreator(user);

        given(repository.findById(projectId)).willReturn(Optional.of(project));
    }

    /**
     * 가상 사용자 정보를 설정합니다.
     *
     * @param userService 사용자 서비스 mock
     * @param userId      사용자 ID
     * @param username    사용자 이름
     * @param role        사용자 역할 (예: ADMIN, SPONSOR 등)
     */
    public static void setVirtualUser(UserService userService, int userId, String username, funding.startreum.domain.users.User.Role role) {
        funding.startreum.domain.users.User user = new funding.startreum.domain.users.User();
        user.setUserId(userId);
        user.setName(username);
        user.setRole(role);

        given(userService.getUserByName(username)).willReturn(user);
    }

    /**
     * 가상 사용자 세부 정보를 생성합니다.
     *
     * @param userDetailsService CustomUserDetailsService mock
     * @param username           사용자 이름
     * @param role               사용자 역할 (예: ADMIN, SPONSOR 등)
     */
    public static void createVirtualDetails(CustomUserDetailsService userDetailsService, String username, String role) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("1234")
                .roles(role)
                .build();

        given(userDetailsService.loadUserByUsername(username)).willReturn(userDetails);
    }

}
