package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.StringUtils.EOL;
import static net.sourceforge.plantuml.test.MultilineStringBuilder.f;
import static net.sourceforge.plantuml.test.MultilineStringBuilder.multilineStringBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.opentest4j.AssertionFailedError;

public class ThrowableTestUtils {

	public static String stackTraceToString(Throwable t) {
		if (t instanceof AssertionFailedError)
			return stackTraceToString_AssertionFailedError((AssertionFailedError) t);
		else
			return stackTraceToString_default(t);
	}

	private static String stackTraceToString_default(Throwable t) {
		final StringWriter w = new StringWriter();
		t.printStackTrace(new PrintWriter(w));
		return w.toString();
	}

	private static String stackTraceToString_AssertionFailedError(AssertionFailedError error) {
		final String stackTrace = stackTraceToString_default(error);

		if (!error.isExpectedDefined() && !error.isActualDefined())
			return stackTrace;

		final String header = error.toString();
		final String expected = error.isExpectedDefined() ? error.getExpected().getStringRepresentation() : "<null>";
		final String actual = error.isActualDefined() ? error.getActual().getStringRepresentation() : "<null>";

		final MultilineStringBuilder b = multilineStringBuilder(
				header,
				""
		);

		if (expected.contains(EOL) || actual.contains(EOL)) {
			b.lines(
					"Expected",
					"--------",
					expected,
					"",
					"Actual",
					"------",
					actual,
					""
			);
		} else {
			b.lines(
					f("Expected : %s", expected),
					f("Actual   : %s", actual),
					""
			);
		}

		return b.lines(stackTrace.substring(header.length() + 1))
				.toString();
	}

	public static Optional<Path> pathForTestSource(StackTraceElement stackTraceElement) {
		if (stackTraceElement.getFileName() == null)
			return Optional.empty();

		return Optional.of(
				Paths.get("test", stackTraceElement.getClassName().split("\\."))
						.getParent().resolve(stackTraceElement.getFileName())
		);
	}
}
