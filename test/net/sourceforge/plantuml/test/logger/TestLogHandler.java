package net.sourceforge.plantuml.test.logger;

import net.sourceforge.plantuml.test.logger.impl.TestLoggerExtension;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface TestLogHandler {

	static void register(ExtensionContext context, TestLogHandler handler) {
		TestLoggerExtension.registerHandler(context, handler);
	}

	void handle(TestLogEvent event);
}
