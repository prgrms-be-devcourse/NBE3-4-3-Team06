package funding.startreum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["funding.startreum"])
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
open class StartreumApplication

fun main(args: Array<String>) {
    runApplication<StartreumApplication>(*args)
}

