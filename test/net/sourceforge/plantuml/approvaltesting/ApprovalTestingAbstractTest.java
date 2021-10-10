package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathUtils.listAllFilesRecursive;
import static net.sourceforge.plantuml.test.TestUtils.createFile;
import static net.sourceforge.plantuml.test.TestUtils.writeUtf8File;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;

abstract class ApprovalTestingAbstractTest {

	@TempDir
	static Path dir;

	static void assertThatDirContainsExactlyTheseFiles(String... files) {
		assertThat(listFilesInDir())
				.containsExactlyInAnyOrder(files);
	}

	static AssertThatFile assertThatFile(String file) {
		return new AssertThatFile(file);
	}

	static AssertThatFileSet assertThatFiles(String... files) {
		return new AssertThatFileSet(files);
	}

	static GivenFile givenFile(String file) {
		return new GivenFile(file);
	}

	static GivenFileSet givenFiles(String... files) {
		return new GivenFileSet(files);
	}

	static List<String> listFilesInDir() {
		return listAllFilesRecursive(dir);
	}

	static class AssertThatFile {
		private final String file;

		public AssertThatFile(String file) {
			this.file = file;
		}

		AssertThatFile hasContent(String expected) {
			assertThat(dir.resolve(file))
					.hasContent(expected);
			return this;
		}

		AssertThatFile isRegularFile() {
			assertThat(dir.resolve(file))
					.isRegularFile();
			return this;
		}

		AssertThatFile hasBinaryContent(byte[] expected) {
			assertThat(dir.resolve(file))
					.hasBinaryContent(expected);
			return this;
		}
	}

	static class AssertThatFileSet {
		private final String[] files;

		public AssertThatFileSet(String... files) {
			this.files = files;
		}

		AssertThatFileSet haveContent(String expected) {
			for (String f : files) {
				assertThat(dir.resolve(f))
						.hasContent(expected);
			}
			return this;
		}

		AssertThatFileSet areRegularFiles() {
			for (String f : files) {
				assertThat(dir.resolve(f))
						.isRegularFile();
			}
			return this;
		}
	}

	static class GivenFile {
		private final String file;

		public GivenFile(String file) {
			this.file = file;
		}

		GivenFile contains(String content) {
			writeUtf8File(dir.resolve(file), content);
			return this;
		}

		GivenFile exists() {
			createFile(dir.resolve(file));
			return this;
		}
	}

	static class GivenFileSet {
		private final String[] files;

		public GivenFileSet(String... files) {
			this.files = files;
		}

		GivenFileSet contain(String content) {
			for (String f : files) {
				writeUtf8File(dir.resolve(f), content);
			}
			return this;
		}

		GivenFileSet exist() {
			for (String f : files) {
				createFile(dir.resolve(f));
			}
			return this;
		}
	}
}
