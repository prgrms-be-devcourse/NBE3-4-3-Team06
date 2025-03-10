package funding.startreum.domain.users.service

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ServiceCheck @Autowired constructor(private val userService: UserService) {
    @PostConstruct
    fun checkService() {
        println("✅ UserService is available: ${userService != null}")
    }
}