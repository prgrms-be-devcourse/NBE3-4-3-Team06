plugins {
	kotlin("jvm") version "1.9.22"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"

	// 🔥 `kotlin-jpa` 대신 `kotlin-allopen` 사용
	id("org.jetbrains.kotlin.plugin.allopen") version "1.9.22"
}
allOpen {
	annotation("org.springframework.stereotype.Service")
	annotation("org.springframework.transaction.annotation.Transactional")
	annotation("org.springframework.web.bind.annotation.RestController")
	annotation("org.springframework.stereotype.Component")
	annotation("org.springframework.stereotype.Repository")
}
group = "funding"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	//developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Kotlin 관련 라이브러리
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// JPA 관련
	runtimeOnly("com.h2database:h2")
	runtimeOnly("mysql:mysql-connector-java:8.0.33")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation("org.springframework.boot:spring-boot-starter-logging")

	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// JSON 관련
	implementation("org.json:json:20240303")
	implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

	// JSON Web Token (JJWT)
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

	// 테스트 관련
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core:5.14.2")
	testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// ✅ Spring AOP 프록시 설정 추가 (JDK 동적 프록시 사용)
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
	systemProperty("spring.aop.proxy-target-class", "false")
}

tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
}
tasks.withType<Jar> {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
