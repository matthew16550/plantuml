package net.sourceforge.plantuml.test.logger.impl;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import net.sourceforge.plantuml.test.logger.TestLogEvent;
import net.sourceforge.plantuml.test.logger.TestLogHandler;
import net.sourceforge.plantuml.test.logger.TestLogger;
import net.sourceforge.plantuml.test.logger.TestLoggerCapture;

public class TestLoggerExtension implements ParameterResolver, BeforeAllCallback {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TestLoggerExtension.class);

	public static void registerHandler(ExtensionContext context, TestLogHandler handler) {
		getHandlers(context.getStore(NAMESPACE)).add(handler);
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		final StackTraceElement[] stackTrace = new Throwable().getStackTrace();

		if (stackTrace[stackTrace.length - 1].getClassName().startsWith("com.intellij."))
			registerHandler(context, new TeamCityTestLogHandler());
		else
			registerHandler(context, new SimpleTestLogHandler());
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		final Class<?> type = parameterContext.getParameter().getType();
		return type == TestLogger.class || type == TestLoggerCapture.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		final Class<?> type = parameterContext.getParameter().getType();

		if (type == TestLogger.class) {
			return TestLogger.forContext(extensionContext);

		} else if (type == TestLoggerCapture.class) {
			final CapturedEvents capturedEvents = extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(CapturedEvents.class);
			return (TestLoggerCapture) () -> unmodifiableList(capturedEvents);

		} else {
			throw new IllegalStateException();
		}
	}

	//
	// Internals
	//

	private static class CapturedEvents extends ArrayList<TestLogEvent> {
	}

	private static class Handlers extends ArrayList<TestLogHandler> {
	}

	private static Handlers getHandlers(Store store) {
		return store.getOrComputeIfAbsent(Handlers.class);
	}

	static void log(TestLogEvent event, ExtensionContext extensionContext) {
		final Store store = extensionContext.getStore(NAMESPACE);

		final CapturedEvents capturedEvents = store.get(CapturedEvents.class, CapturedEvents.class);
		if (capturedEvents != null) {
			capturedEvents.add(event);
			return;
		}

		for (TestLogHandler handler : getHandlers(store)) {
			handler.handle(event);
		}
	}
}
