package my.com.maybank.usermgmt;

import com.password4j.Password;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class Password4jJUnitTest
{
    @BeforeAll
    public static void setUpClass() {}
    
    @AfterAll
    public static void tearDownClass() {}
    
    @BeforeEach
    public void setUp() {}
    
    @AfterEach
    public void tearDown() {}
    
    @Test
    public void hashAndVerify_ExpectOK()
    {
        final String passwordPlain = "admin";
        final String salt = "sBT0WFr4Uwp6OmHmSaqRgspM4rVD4O6Q";
        
        final String passwordHash = Base64.encodeBase64URLSafeString(
            Password.hash(
                passwordPlain
            ).addSalt(
                salt
            ).withArgon2().getResultAsBytes()
        );
        assertNotNull(
            passwordHash
        );
        log.info(
            "Got passwod hash '{}' for '{}', salt '{}'",
            passwordHash,
            passwordPlain,
            salt
        );
        
        final boolean passwordVerify = Password.check(
            passwordPlain,
            new String(
                Base64.decodeBase64(
                    passwordHash
                ),
                StandardCharsets.UTF_8
            )
        ).withArgon2();
        assertTrue(
            passwordVerify
        );
        log.info(
            "Verified passwod hash '{}' for '{}', salt '{}'",
            passwordHash,
            passwordPlain,
            salt
        );
    }
}
