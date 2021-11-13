package net.sourceforge.plantuml.test.logger;

import java.util.List;

/**
 * Kludge so we can test the test logging :-)
 */
public interface TestLoggerCapture {
	List<TestLogEvent> getLogEvents();
}
