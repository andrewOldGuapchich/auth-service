plugins {
    id("org.springframework.boot") version "3.1.8"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.spring") version "1.8.22"
    id("pl.allegro.tech.build.axion-release") version "1.15.0"
}

group = "com.andrew.greenhouse.auth"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
//    maven {
//        url = uri("http://localhost:8081/repository/SMART_GREENHOUSE_SNAPSHOT/")
//        credentials {
//            username = "admin"
//            password = "Andrew5525613"
//        }
//        isAllowInsecureProtocol = true
//    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.4")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("io.lettuce:lettuce-core:6.5.5.RELEASE")
    implementation("org.springframework.kafka:spring-kafka:3.3.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")
    //implementation("com.andrew.greenhouse.auth:api:1.0.36-20250316.140216-1")
    implementation("com.andrew.greenhouse.auth:auth-api:1.0-SNAPSHOT")
    runtimeOnly("org.postgresql:postgresql")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}