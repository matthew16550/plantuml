package net.sourceforge.plantuml.test.logger.impl;

import net.sourceforge.plantuml.test.logger.TestLogEvent;
import net.sourceforge.plantuml.test.logger.TestLogHandler;

public class SimpleTestLogHandler implements TestLogHandler {

	@Override
	public void handle(TestLogEvent event) {
		System.out.printf("%-7s : %s%n", event.getLevel(), event.getMessage());
		if (event.getDetails() != null)
			System.out.println(event.getDetails());
		System.out.println();
	}
}
