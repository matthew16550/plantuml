package net.sourceforge.plantuml.test.logger;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface TestLogger {

	enum Level {
		INFO,
		WARNING,
		ERROR;
	}
	
	static TestLogger forContext(ExtensionContext context) {
		return TestLoggerExtension.createTestLogger(context);
	}

	default void info(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	default void info(Throwable t, String message, Object... args) {
		log(Level.INFO, t, message, args);
	}

	default void error(String message, Object... args) {
		log(Level.ERROR, message, args);
	}

	default void error(Throwable t, String message, Object... args) {
		log(Level.ERROR, t, message, args);
	}

	default void warning(String message, Object... args) {
		log(Level.WARNING, message, args);
	}

	default void warning(Throwable t, String message, Object... args) {
		log(Level.WARNING, t, message, args);
	}

	default void log(Level level, String message, Object... args) {
		log(level, null, message, args);
	}

	void log(Level level, Throwable throwable, String message, Object... args);
}
