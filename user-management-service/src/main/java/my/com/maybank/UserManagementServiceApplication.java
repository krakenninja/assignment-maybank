package my.com.maybank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Management Service Spring Boot main application
 * @since 1.0.0
 * @author ChristopherCKW
 */
@SpringBootApplication(
    scanBasePackages = {
        "my.com.maybank"
    }
)
@Slf4j
public class UserManagementServiceApplication
{
    public static void main(final String[] args)
    {
        log.debug(
            "Start {}",
            UserManagementServiceApplication.class.getName()
        );
        SpringApplication.run(
            UserManagementServiceApplication.class, 
            args
        );
    }
}
