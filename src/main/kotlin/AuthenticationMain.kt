import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EntityScan("entities.model")
@ComponentScan("services")
class AuthenticationMain
    fun main(args: Array<String>){
        runApplication<AuthenticationMain>(*args)
    }