package net.sourceforge.plantuml.test.github;

import net.sourceforge.plantuml.test.logger.TestLogger;

import org.junit.jupiter.api.Test;

public class TempTest {
	
	@Test
	void test_logging(TestLogger logger) {
//		logger.debug("the-debug");
//		logger.info("the-info");
//		logger.warning("the-warning");
		logger.withDetails(new Throwable()).error("the-error");
//		System.out.println("##teamcity[message text='the-info' errorDetails='stack trace' status='NORMAL']");
//		System.out.println("##teamcity[message text='the-warning' errorDetails='stack trace' status='WARNING']");
//		System.out.println("##teamcity[message text='the-error' errorDetails='stack trace' status='ERROR']");
	}
	
	@Test
	void test_foo() {
	}
}
