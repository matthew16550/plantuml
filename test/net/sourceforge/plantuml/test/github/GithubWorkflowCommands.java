package net.sourceforge.plantuml.test.github;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import net.sourceforge.plantuml.Log;

/**
 * Helpers for
 * <a href="https://docs.github.com/en/actions/learn-github-actions/workflow-commands-for-github-actions">GitHub Workflow Commands</a>
 */
public class GithubWorkflowCommands {

	/**
	 * Beware you need to enable debug logging in GitHub.
	 * @see <a href="https://docs.github.com/en/actions/monitoring-and-troubleshooting-workflows/enabling-debug-logging#enabling-step-debug-logging">GitHub Docs</a>
	 */
	public static void debug(String message) {
		System.out.format("::debug::%s\n", message);
	}

	public static void error(String message) {
		System.out.println(formatAnnotation("error", null, message));
	}

	public static void error(String title, String message) {
		System.out.println(formatAnnotation("error", title, message));
	}

	public static void notice(String message) {
		System.out.println(formatAnnotation("notice", null, message));
	}

	public static void notice(String title, String message) {
		System.out.println(formatAnnotation("notice", title, message));
	}

	public static void warning(String message) {
		System.out.println(formatAnnotation("warning", null, message));
	}

	public static void warning(String title, String message) {
		System.out.println(formatAnnotation("warning", title, message));
	}

	//
	// Internals
	//

	private static String formatAnnotation(String level, String title, String message) {
		Objects.requireNonNull(level);
		Objects.requireNonNull(message);

		final StackTraceElement element = Thread.currentThread().getStackTrace()[3];
		final String file = findFileForAnnotation(element);

		final StringBuilder builder = new StringBuilder()
				.append("::").append(level).append(' ');

		if (file != null) {
			builder
					.append("file=").append(file).append(',')
					.append("line=").append(element.getLineNumber());
		}

		if (title != null) {
			if (file != null) builder.append(',');
			builder.append("title=").append(title.replaceAll("::", "_"));
		}

		builder.append("::").append(message);

		return builder.toString();
	}

	private static String findFileForAnnotation(StackTraceElement element) {
		final String[] path = element.getClassName().split("\\.");
		path[path.length - 1] = element.getFileName();

		final String file = Stream.of("test", "src")
				.map(dir -> Paths.get(dir, path))
				.filter(Files::exists)
				.map(Path::toString)
				.findFirst()
				.orElse(null);

		if (file == null) {
			Log.error("GithubWorkflowCommands cannot find a file for " + Arrays.toString(path));
		}

		return file;
	}
}
