package net.sourceforge.plantuml.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TestLogger {

	//
	// Public API
	//

	public static void error(Throwable t, String message, Object... args) {
		output("ERROR  : ", message, t, args);
	}

	public static void warning(String message, Object... args) {
		output("WARNING : ", message, null, args);
	}

	public static void enableCapture() {
		capture.set(new ArrayList<>());
	}

	public static void disableCapture() {
		capture.remove();
	}

	public static List<String> getCaptured() {
		return capture.get();
	}

	//
	// Internals
	//

	private static final ThreadLocal<List<String>> capture = new ThreadLocal<>();

	private static void output(String prefix, String message, Throwable throwable, Object... args) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter w = new PrintWriter(stringWriter);
		w.print(prefix);
		w.format(Locale.US, message, args);
		w.println();
		if (throwable != null) throwable.printStackTrace(w);
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
