package net.sourceforge.plantuml.test.github;

import static net.sourceforge.plantuml.StringUtils.truncateStringToByteLength;
import static net.sourceforge.plantuml.test.MultilineStringBuilder.f;
import static net.sourceforge.plantuml.test.MultilineStringBuilder.multilineStringBuilder;
import static net.sourceforge.plantuml.test.ThrowableTestUtils.stackTraceToString;
import static net.sourceforge.plantuml.utils.DateTimeUtils.createDateFormatUtc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateTestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.test.MultilineStringBuilder;
import net.sourceforge.plantuml.test.logger.TestLogHandler;

/**
 * see https://docs.github.com/en/rest/reference/checks
 */
public class GitHubAnnotationsListener extends SummaryGeneratingListener implements BeforeAllCallback {

	private static final String TRUNCATED = "\n[Truncated]";

	private static final int MAX_ANNOTATIONS = 50;  // Limit comes from the REST api

	private static final DateFormat DATE_FORMAT = createDateFormatUtc("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private static final ObjectWriter JSON_WRITER = new ObjectMapper()
			.setDateFormat(DATE_FORMAT)
			.setSerializationInclusion(Include.NON_NULL)
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
			.writerWithDefaultPrettyPrinter();

	@SuppressWarnings("BooleanVariableAlwaysNegated")
	private final boolean active = "true".equals(System.getenv("GITHUB_ACTIONS"));

	private final List<Annotation> annotations = new ArrayList<>();
	private final List<String> failedTests = new ArrayList<>();

	//
	// Annotating test failures
	//

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		super.executionFinished(testIdentifier, testExecutionResult);
		if (!active)
			return;

		if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
			final String prettyIdentifier = formatTestIdentifier(testIdentifier);
			failedTests.add(prettyIdentifier);

			final Throwable throwable = testExecutionResult.getThrowable().orElseThrow(IllegalStateException::new);

			final Annotation a = new Annotation();
			a.annotation_level = AnnotationLevel.failure;
			a.title = truncateStringToByteLength("Test Failed: " + prettyIdentifier, 1024, "");
			a.message = truncateStringToByteLength(throwable.getMessage(), 65535, TRUNCATED);
			a.raw_details = truncateStringToByteLength(stackTraceToString(throwable), 65535, TRUNCATED);
			fillTestLocation(a, testIdentifier, throwable.getStackTrace());
			annotations.add(a);
		}
	}

	private static String formatTestIdentifier(TestIdentifier identifier) {
		final List<String> parts = identifier.getUniqueIdObject().getSegments().stream()
				.map(GitHubAnnotationsListener::formatSegment)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		parts.set(parts.size() - 1, identifier.getDisplayName());
		return String.join(" > ", parts);
	}

	private static String formatSegment(Segment s) {
		switch (s.getType()) {
			case "engine":
				return null; // ignore it;
			case ClassTestDescriptor.SEGMENT_TYPE:
				return StringUtils.substringAfterLast(s.getValue(), '.');
			case TestTemplateTestDescriptor.SEGMENT_TYPE:
				return StringUtils.substringBefore(s.getValue(), '(') + "()";
			default:
				return s.getValue();
		}
	}

	private void fillTestLocation(Annotation annotation, TestIdentifier testIdentifier, StackTraceElement[] stackTrace) {
		StringBuilder classBuilder = new StringBuilder();
		String fileName = null;
		String methodName = null;
		int line = 1;

		for (Segment s : testIdentifier.getUniqueIdObject().getSegments()) {
			switch (s.getType()) {
				case ClassTestDescriptor.SEGMENT_TYPE:
					classBuilder.append(s.getValue());
					break;
				case NestedClassTestDescriptor.SEGMENT_TYPE:
					classBuilder.append("$").append(s.getValue());
					break;
				case TestMethodTestDescriptor.SEGMENT_TYPE:
				case TestTemplateTestDescriptor.SEGMENT_TYPE:
					methodName = StringUtils.substringBefore(s.getValue(), '(');
					break;
			}
		}

		final String className = classBuilder.toString();

		if (!className.isEmpty() && methodName != null) {
			for (StackTraceElement element : stackTrace) {
				if (element.getClassName().equals(className) && element.getMethodName().equals(methodName)) {
					fileName = element.getFileName();
					line = element.getLineNumber();
					break;
				}
			}
		}

		if (className.isEmpty()) {
			annotation.path = "unknown";
		} else {
			final Path path = Paths.get("test", StringUtils.substringBefore(className, '$').split("\\."));
			annotation.path = fileName == null
					? path + ".java"
					: path.getParent().resolve(fileName).toString();
		}

		annotation.start_line = line;
		annotation.end_line = line;
	}

	//
	// Capture logs as annotations
	// (we do not use "Workflow Commands" like "::notice" because they do not allow for raw_details)
	//

	@Override
	public void beforeAll(ExtensionContext context) {
		if (!active)
			return;

		TestLogHandler.register(context, event -> {
			final AnnotationLevel annotationLevel;

			switch (event.getLevel()) {
				case INFO:
					annotationLevel = AnnotationLevel.notice;
					break;
				case WARNING:
				case ERROR:
					annotationLevel = AnnotationLevel.warning;
					break;
				default:
					return;  // no annotation from other levels
			}

			final Annotation a = new Annotation();
			a.annotation_level = annotationLevel;
			a.path = event.getFile() != null ? event.getFile() : "unknown";
			a.title = event.getLevel().toString();
			a.message = truncateStringToByteLength(event.getMessage(), 65535, TRUNCATED);
			a.start_line = event.getLine();
			a.end_line = event.getLine();
			if (event.getDetails() != null)
				a.raw_details = truncateStringToByteLength(event.getDetails(), 65535, TRUNCATED);
			annotations.add(a);
		});
	}

	//
	// After all the tests
	//

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		super.testPlanExecutionFinished(testPlan);
		if (!active)
			return;

		final TestExecutionSummary summary = getSummary();
		final MultilineStringBuilder builder = multilineStringBuilder();

		final CheckRun checkRun = new CheckRun();
		checkRun.status = "completed";
		checkRun.started_at = new Date(summary.getTimeStarted());
		checkRun.conclusion = summary.getTotalFailureCount() == 0 ? "success" : "failure";
		checkRun.completed_at = new Date(summary.getTimeFinished());
		checkRun.output.title = "Summary";

		if (annotations.size() <= MAX_ANNOTATIONS) {
			checkRun.output.annotations = annotations;
		} else {
			// We could do more annotations but would need to upload them in multiple REST requests and perhaps not worth the effort
			checkRun.output.annotations = annotations.subList(0, MAX_ANNOTATIONS);
			builder.lines(
					f("#### :warning: There are %d annotations but only %d were sent to GitHub", annotations.size(), MAX_ANNOTATIONS),
					""
			);
		}

		builder.lines(
				f("| | Tests | Containers |"),
				f("|-|-|-|"),
				f("| **Passed**  | %d | %d |", summary.getTestsSucceededCount(), summary.getContainersSucceededCount()),
				f("| **Failed**  | %d | %d |", summary.getTestsFailedCount(), summary.getContainersFailedCount()),
				f("| **Skipped** | %d | %d |", summary.getTestsSkippedCount(), summary.getContainersSkippedCount()),
				f("| **Aborted** | %d | %d |", summary.getTestsAbortedCount(), summary.getContainersAbortedCount())
		);

		if (!failedTests.isEmpty()) {
			builder.lines(
					"",
					"## Failures",
					"```",
					failedTests.stream().sorted(),
					"```"
			);
		}

		checkRun.output.summary = truncateStringToByteLength(builder.toString(), 65535, TRUNCATED);

		try {
			JSON_WRITER.writeValue(new File("target/github-check-run.json"), checkRun);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//
	// REST models
	//

	private static class Annotation {
		String path;
		int start_line;
		int end_line;
		AnnotationLevel annotation_level;
		String message;      // Max 65535 bytes (& must not be empty string?)
		String title;        // Max 1024 bytes
		String raw_details;  // Max 65535 bytes
	}

	private enum AnnotationLevel {
		failure, notice, warning
	}

	private static class CheckRun {
		String status;
		Date started_at;
		String conclusion;
		Date completed_at;
		final CheckRunOutput output = new CheckRunOutput();
	}

	private static class CheckRunOutput {
		String title;    // Max 1024 bytes (not documented but silently truncated in server)
		String summary;  // Max 65535 bytes
		List<Annotation> annotations;
	}
}
