rootProject.name = "service"

pluginManagement {
    repositories {
        maven {
            url = uri("http://localhost:8081/repository/maven-public/")
            credentials {
                username = "admin"
                password = "Andrew5525613"
            }
            isAllowInsecureProtocol = true
        }
    }
}