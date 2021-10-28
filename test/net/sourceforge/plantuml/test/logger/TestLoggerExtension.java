package net.sourceforge.plantuml.test.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestLoggerExtension implements ParameterResolver {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TestLoggerExtension.class);

	private static class CapturedLogs extends ArrayList<String> {
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
			return createTestLogger(extensionContext);
		}
		if (type == TestLoggerCapture.class) {
			final CapturedLogs capturedLogs = new CapturedLogs();
			extensionContext.getStore(NAMESPACE).put(CapturedLogs.class, capturedLogs);
			return (TestLoggerCapture) () -> capturedLogs;
		}
		throw new IllegalStateException();
	}

	static TestLogger createTestLogger(ExtensionContext context) {

		return (level, throwable, message, args) -> {
			final String log = formatLog(level, throwable, message, args);

			final CapturedLogs capturedLogs = context.getStore(NAMESPACE).get(CapturedLogs.class, CapturedLogs.class);
			if (capturedLogs != null) {
				capturedLogs.addAll(Arrays.asList(log.split("\\R")));
				return;
			}

			// Why System.out?
			// JUnit5 has a publishReportEntry() system, but it seems that tests run by Maven do not put the data anywhere
			// System.err feels wrong so using System.out !
			System.out.print(log);
			System.out.println();
		};
	}

	private static String formatLog(TestLogger.Level level, Throwable throwable, String message, Object... args) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter w = new PrintWriter(stringWriter);
		w.format(Locale.US, "%-7s : ", level);
		w.format(Locale.US, message, args);
		w.println();
		if (throwable != null) throwable.printStackTrace(w);
		w.flush();
		return stringWriter.toString();
	}
}
