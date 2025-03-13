import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan("entities.model")
@EnableJpaRepositories("com.andrew.greenhouse.auth.repositories")
@ComponentScan("services", "com.andrew.greenhouse.auth")
@EnableDiscoveryClient
class AuthenticationMain
    fun main(args: Array<String>){
        runApplication<AuthenticationMain>(*args)
    }