package funding.startreum.domain.users;

import funding.startreum.domain.users.User;
import funding.startreum.domain.users.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MyProjectService {

    private final MyProjectRepository myProjectRepository;
    private final UserService userService;

    public MyProjectService(MyProjectRepository myProjectRepository, UserService userService) {
        this.myProjectRepository = myProjectRepository;
        this.userService = userService;
    }

    public List<MyProjectDTO> getProjectsByUser(String username) {
        // 사용자 정보 조회
        User user = userService.getUserByName(username);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 해당 사용자의 프로젝트 조회 후 DTO로 변환
        return myProjectRepository.findByCreator(user)
                .stream()
                .map(MyProjectDTO::from)
                .collect(Collectors.toList());
    }
}