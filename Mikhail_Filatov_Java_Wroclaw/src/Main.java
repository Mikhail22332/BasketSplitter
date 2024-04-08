import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Main {

    public static void main(String[] args) throws Exception {
        assertDoesNotThrow(() -> {
            Class.forName("org.json.simple.JSONObject");
            Class.forName("org.json.simple.parser.JSONParser");
        });
        // Test JUnit Jupiter
        assertNotNull(Test.class);
    }
}