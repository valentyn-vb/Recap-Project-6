package com.example.demo;

import com.example.demo.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestContainer.class)
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
