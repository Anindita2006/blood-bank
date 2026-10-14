package karate;

import com.bloodbank.BloodBankApplication;
import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

// WHY THIS FILE EXISTS:
// Karate tests need the app to be RUNNING (they send real HTTP requests).
// This class starts the app automatically before the tests, so `mvn test`
// (and Jenkins) can run everything without anyone starting the app by hand.

// Step 1: start the full app on any free port (avoids clashing with something already on 8080)
@SpringBootTest(classes = BloodBankApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KarateRunnerTest {

    // Step 2: Spring tells us which port it picked
    @LocalServerPort
    int port;

    @Karate.Test
    Karate runAll() {
        // Step 3: pass the port to Karate (karate-config.js reads it to build the base URL)
        System.setProperty("app.port", String.valueOf(port));
        // Step 4: run both .feature files that sit in this same folder
        return Karate.run("blood-api", "js-logic").relativeTo(getClass());
    }
}
