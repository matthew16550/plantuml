package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.StringUtils.EOL;
import static net.sourceforge.plantuml.test.FileTestUtils.writeUtf8File;
import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_withFileSpamLimit {

	@TempDir
	static Path dir;

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting()
			.withDuplicateFiles(true);

	@BeforeAll
	static void beforeAll() {
		Stream.of(
				"ApprovalTestingTest_withFileSpamLimit.testA.1.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.2.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.3.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.4.approved.txt",

				"ApprovalTestingTest_withFileSpamLimit.testB.1.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testB.2.approved.txt"
		).forEach(file ->
				writeUtf8File(dir.resolve(file), "foo")
		);
	}

	@AfterAll
	static void afterAll() {
		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_withFileSpamLimit.testA.1.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.2.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.3.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.4.approved.txt",

				"ApprovalTestingTest_withFileSpamLimit.testB.1.approved.txt",
				"ApprovalTestingTest_withFileSpamLimit.testB.2.approved.txt",

				"ApprovalTestingTest_withFileSpamLimit.testA.1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.1.OUTPUT_1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.1.OUTPUT_2.failed.txt",

				"ApprovalTestingTest_withFileSpamLimit.testA.2.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.2.OUTPUT_1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.2.OUTPUT_2.failed.txt",

				"ApprovalTestingTest_withFileSpamLimit.testB.1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testB.1.OUTPUT_1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testB.1.OUTPUT_2.failed.txt"
		);

		Stream.of(
				"ApprovalTestingTest_withFileSpamLimit.testA.1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.2.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testB.1.failed.txt"
		).forEach(file ->
				assertThat(dir.resolve(file))
						.hasContent("bar")
		);

		Stream.of(
				"ApprovalTestingTest_withFileSpamLimit.testA.1.OUTPUT_1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.1.OUTPUT_2.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.2.OUTPUT_1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testA.2.OUTPUT_2.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testB.1.OUTPUT_1.failed.txt",
				"ApprovalTestingTest_withFileSpamLimit.testB.1.OUTPUT_2.failed.txt"
		).forEach(file ->
				assertThat(dir.resolve(file))
						.hasContent("123")
		);
	}

	@RepeatedTest(value = 4, name = "{currentRepetition}")
	void testA(RepetitionInfo repetitionInfo) {
		doApprove(2, repetitionInfo.getCurrentRepetition());
	}

	@RepeatedTest(value = 2, name = "{currentRepetition}")
	void testB(RepetitionInfo repetitionInfo) {
		doApprove(1, repetitionInfo.getCurrentRepetition());
	}

	void doApprove(int fileSpamLimit, int currentRepetition) {
		final String expectedMessagePrefix = currentRepetition > fileSpamLimit
				? String.format("** APPROVAL FAILURE FILES WERE SUPPRESSED (test has failed %d times) ** ", currentRepetition)
				: EOL + "expected: ";

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() ->
						approvalTesting
								.withDir(dir)
								.withFileSpamLimit(fileSpamLimit)
								.withOutput("OUTPUT_1", ".txt", path -> writeUtf8File(path, "123"))
								.withOutput("OUTPUT_2", ".txt", path -> writeUtf8File(path, "123"))
								.approve("bar")
				)
				.withMessageStartingWith(expectedMessagePrefix);
	}
}
