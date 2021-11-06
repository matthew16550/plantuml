package net.sourceforge.plantuml.test.logger;

import org.junit.jupiter.api.extension.ExtensionContext;

import net.sourceforge.plantuml.test.logger.impl.TestLoggerImpl;

public interface TestLogger {

	static TestLogger forContext(ExtensionContext context) {
		return new TestLoggerImpl(context);
	}

	void debug(String format, Object... args);

	void info(String format, Object... args);

	void warning(String format, Object... args);

	void error(String format, Object... args);

	TestLogger withDetails(String format, Object... args);

	TestLogger withDetails(Throwable throwable);
}
