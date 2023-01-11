import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.1"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
}

group = "com.mtg.tracker"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

val exposedVersion = "0.41.1"
val arrowVersion = "1.1.2"

repositories {
	mavenCentral()
}

dependencies {
	/* Spring stuff */
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	/* Jwt stuff */
	implementation("org.bitbucket.b_c:jose4j:0.9.1")
	/* Kotlin stuff */
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	/* Arrow */
	implementation("io.arrow-kt:arrow-core-jvm:$arrowVersion")
	/* Database stuff */
	implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
	implementation("com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9")
	implementation("org.liquibase:liquibase-core:4.18.0")
	implementation("com.zaxxer:HikariCP:5.0.1")
	/* Test stuff */
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val fatJar = task("fatJar", type = Jar::class) {
	manifest {
		attributes["Main-Class"] = "com.mtg.tracker.MtgTrackerApplicationKt"
	}
	with(tasks["jar"] as CopySpec)
}

tasks {
	"build" {
		dependsOn(fatJar)
	}
}