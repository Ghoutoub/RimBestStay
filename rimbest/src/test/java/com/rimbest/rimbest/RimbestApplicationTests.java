package com.rimbest.rimbest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "app.jwtSecret=testSecret",
    "app.jwtExpirationMs=86400000"
})
class RimbestApplicationTests {

	@Test
	void contextLoads() {
	}

}
