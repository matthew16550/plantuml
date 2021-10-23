package net.sourceforge.plantuml.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LoggingTestUtils {

	//
	// Public API
	//

	public static void logError(Exception e, String message, Object... args) {
		output("ERROR  : ", message, e, args);
	}

	public static void logWarning(String message, Object... args) {
		output("WARNING : ", message, null, args);
	}

	public static void enableCaptureThisThread() {
		capture.set(new ArrayList<>());
	}

	public static void disableCaptureThisThread() {
		capture.remove();
	}

	public static List<String> getCaptureThisThread() {
		return capture.get();
	}

	//
	// Internals
	//

	private static ThreadLocal<List<String>> capture = new ThreadLocal<>();

	private static void output(String prefix, String message, Exception exception, Object... args) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter w = new PrintWriter(stringWriter);
		w.print(prefix);
		w.format(Locale.US, message, args);
		w.println();
		if (exception != null) exception.printStackTrace(w);
		w.flush();

		final List<String> captured = capture.get();
		if (captured == null) {
			System.err.print(stringWriter);
			System.err.println();
		} else {
			captured.addAll(Arrays.asList(stringWriter.toString().split("\\R")));
		}
	}
}
