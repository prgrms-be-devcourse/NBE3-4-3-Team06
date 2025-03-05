package funding.startreum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["funding.startreum"])
@EntityScan(basePackages = ["funding.startreum.domain"]) // 루트 패키지로 설정
@EnableJpaRepositories
open class StartreumApplication

fun main(args: Array<String>) {
    runApplication<StartreumApplication>(*args)
}
