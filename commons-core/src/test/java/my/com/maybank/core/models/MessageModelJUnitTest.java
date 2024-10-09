package my.com.maybank.core.models;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class MessageModelJUnitTest
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
    public void errorMessage_dataStructure_Json_ExpectOK()
    {
        final MessageModel.MessageModelResponse errorMessageModelResponse = MessageModel.errorMessage(
            "Something went wrong", 
            1001l
        );
        
        final String errorMessageModelResponseAsJson = errorMessageModelResponse.toJson();
        assertNotNull(
            errorMessageModelResponseAsJson
        );
        
        log.info(
            "Got simple error message model response JSON string --- \n\t{}",
            errorMessageModelResponseAsJson
        );
    }
}
