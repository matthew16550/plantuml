package net.sourceforge.plantuml.test;

import static java.util.Objects.requireNonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.Locale;

public class TestLogger {

	// kludge for testing the test logging :-(
	public static final String SUPPRESS_GITHUB_MAGIC = "net.sourceforge.plantuml.test.TestLogger.SUPPRESS_GITHUB_MAGIC";

	private static final String CLASS_NAME = TestLogger.class.getName();

	private final boolean githubMagic = !Boolean.getBoolean(SUPPRESS_GITHUB_MAGIC)
			&& "true".equals(System.getenv("GITHUB_ACTIONS"));

	//
	// Public API
	//

	enum Level {
		DEBUG,
		INFO,
		WARNING,
		ERROR;
	}

	public void debug(String message, Object... args) {
		log(Level.DEBUG, message, args);
	}

	public void debug(Throwable t, String message, Object... args) {
		log(Level.DEBUG, t, message, args);
	}

	public void info(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	public void info(Throwable t, String message, Object... args) {
		log(Level.INFO, t, message, args);
	}

	public void error(String message, Object... args) {
		log(Level.ERROR, message, args);
	}

	public void error(Throwable t, String message, Object... args) {
		log(Level.ERROR, t, message, args);
	}

	public void warning(String message, Object... args) {
		log(Level.WARNING, message, args);
	}

	public void warning(Throwable t, String message, Object... args) {
		log(Level.WARNING, t, message, args);
	}

	public void log(Level level, String message, Object... args) {
		log(level, null, message, args);
	}

	public void log(Level level, Throwable throwable, String message, Object... args) {
		requireNonNull(level);
		requireNonNull(message);
		requireNonNull(args);

		if (githubMagic) {
			switch (level) {
				case WARNING:
					System.out.println(formatLogForGithubActions("::warning   ", "Test Warning", message, args));
					break;
				case ERROR:
					System.out.println(formatLogForGithubActions("::error     ", "Test Error", message, args));
					break;
			}
		}

		// Why System.out?  JUnit5 has a publishReportEntry() feature, but it seems that tests run by Maven do not put the data anywhere.
		// System.err feels wrong so using System.out !
		System.out.print(formatLog(level, throwable, message, args));
		System.out.println();
	}

	//
	// Internals
	//

	private static String formatLog(TestLogger.Level level, Throwable throwable, String message, Object... args) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter w = new PrintWriter(stringWriter);
		w.format(Locale.US, "%-7s   : ", level);
		w.format(Locale.US, message, args);
		w.println();
		if (throwable != null) throwable.printStackTrace(w);
		return stringWriter.toString();
	}

	// See https://docs.github.com/en/actions/learn-github-actions/workflow-commands-for-github-actions
	private static String formatLogForGithubActions(String command, String title, String message, Object... args) {
		final StringBuilder b = new StringBuilder()
				.append(command);

		final StackTraceElement element = findCaller();

		if (element != null && element.getFileName() != null) {
			final String file = Paths.get("test", element.getClassName().split("\\."))
					.getParent().resolve(element.getFileName()).toString();
			b.append("file=").append(file).append(',');
			b.append("line=").append(element.getLineNumber()).append(',');
		}

		b.append("title=").append(title).append("::");
		new Formatter(b).format(Locale.US, message, args);
		return b.toString();
	}

	// "caller" is the first method before the TestLogger class
	private static StackTraceElement findCaller() {
		boolean seenTestLogger = false;
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			if (CLASS_NAME.equals(e.getClassName())) {
				seenTestLogger = true;
			} else if (seenTestLogger) {
				return e;
			}
		}
		return null;
	}
}
