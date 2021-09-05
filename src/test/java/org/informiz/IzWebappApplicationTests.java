package org.informiz;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IzWebappApplicationTests {

	@Test
	public void contextLoads() {
	}

	/**
	 * 	@ClassRule
	public static GenericContainer simpleWebServer
	= new GenericContainer("alpine:3.2")
	.withExposedPorts(80)
	.withCommand("/bin/sh", "-c", "while true; do echo "
	+ "\"HTTP/1.1 200 OK\n\nHello World!\" | nc -l -p 80; done");

	 */

}
