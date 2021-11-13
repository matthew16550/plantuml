package net.sourceforge.plantuml.test.logger.impl;

import static java.util.Objects.requireNonNull;
import static net.sourceforge.plantuml.test.ThrowableTestUtils.stackTraceToString;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;

import net.sourceforge.plantuml.test.ThrowableTestUtils;
import net.sourceforge.plantuml.test.logger.TestLogEvent;
import net.sourceforge.plantuml.test.logger.TestLogEvent.Level;
import net.sourceforge.plantuml.test.logger.TestLogger;

public class TestLoggerImpl implements TestLogger {

	private static final String CLASS_NAME = TestLoggerImpl.class.getName();

	final ExtensionContext extensionContext;
	final String details;

	public TestLoggerImpl(ExtensionContext extensionContext) {
		this.extensionContext = extensionContext;
		this.details = null;
	}

	private TestLoggerImpl(ExtensionContext extensionContext, String details) {
		this.extensionContext = extensionContext;
		this.details = details;
	}

	@Override
	public void debug(String message, Object... args) {
		log(Level.DEBUG, message, args);
	}

	@Override
	public void info(String format, Object... args) {
		log(Level.INFO, format, args);
	}

	@Override
	public void warning(String format, Object... args) {
		log(Level.WARNING, format, args);
	}

	@Override
	public void error(String format, Object... args) {
		log(Level.ERROR, format, args);
	}

	@Override
	public TestLogger withDetails(String format, Object... args) {
		requireNonNull(format);
		return new TestLoggerImpl(extensionContext, String.format(Locale.US, format, args));
	}

	@Override
	public TestLogger withDetails(Throwable throwable) {
		requireNonNull(throwable);
		return withDetails(stackTraceToString(throwable));
	}

	private void log(Level level, String format, Object... args) {
		requireNonNull(format);
		final Optional<StackTraceElement> caller = findCaller();
		final Optional<Path> path = caller.flatMap(ThrowableTestUtils::pathForTestSource);
		final String file = path.map(Path::toString).orElse(null);
		final int line = path.isPresent() ? caller.get().getLineNumber() : 1;
		final String message = String.format(format, args);
		final TestLogEvent event = new TestLogEvent(level, message, details, file, line);
		TestLoggerExtension.log(event, extensionContext);
	}

	// "caller" is the first method before this class
	private Optional<StackTraceElement> findCaller() {
		boolean seenTestLogger = false;
		for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
			if (CLASS_NAME.equals(element.getClassName())) {
				seenTestLogger = true;
			} else if (seenTestLogger) {
				return Optional.of(element);
			}
		}
		return Optional.empty();
	}
}
