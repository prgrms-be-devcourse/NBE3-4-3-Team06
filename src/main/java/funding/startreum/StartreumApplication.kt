package funding.startreum

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication(scanBasePackages = ["funding.startreum"])
@EnableAspectJAutoProxy(proxyTargetClass = false)

open class StartreumApplication {

    /*
       @Bean
        open fun commandLineRunner(ctx: ApplicationContext) = CommandLineRunner {
           println("✅ UserService registered: ${ctx.containsBean("userService")}")
            println("✅ UserController registered: ${ctx.containsBean("userController")}")

            val userController = ctx.getBean(UserController::class.java)
            val userService = ctx.getBean(UserService::class.java)
            println("🟠 UserController instance: $userController")
           println("🟠 UserService instance: $userService")
           println("🟠 UserController's ApplicationContext: ${userController.javaClass.classLoader}")
            println("🟠 UserService's ApplicationContext: ${userService.javaClass.classLoader}")
        }*/

}

fun main(args: Array<String>) {
    println("🚀 Starting StartreumApplication...") // 실행 로그 추가
    runApplication<StartreumApplication>(*args)
}
