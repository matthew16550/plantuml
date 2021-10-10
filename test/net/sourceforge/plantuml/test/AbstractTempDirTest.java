package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.test.PathTestUtils.listAllFilesRecursive;
import static net.sourceforge.plantuml.test.FileTestUtils.createFile;
import static net.sourceforge.plantuml.test.FileTestUtils.writeUtf8File;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;

public abstract class AbstractTempDirTest {

	@TempDir
	public static Path dir;

	public static void assertThatDirContainsExactlyTheseFiles(String... files) {
		assertThat(listFilesInDir())
				.containsExactlyInAnyOrder(files);
	}

	public static AssertThatFile assertThatFile(String file) {
		return new AssertThatFile(file);
	}

	public static AssertThatFileSet assertThatFiles(String... files) {
		return new AssertThatFileSet(files);
	}

	public static GivenFile givenFile(String file) {
		return new GivenFile(file);
	}

	public static GivenFileSet givenFiles(String... files) {
		return new GivenFileSet(files);
	}

	public static List<String> listFilesInDir() {
		return listAllFilesRecursive(dir);
	}

	public static class AssertThatFile {
		private final String file;

		public AssertThatFile(String file) {
			this.file = file;
		}

		public AssertThatFile hasContent(String expected) {
			assertThat(dir.resolve(file))
					.hasContent(expected);
			return this;
		}

		public AssertThatFile isRegularFile() {
			assertThat(dir.resolve(file))
					.isRegularFile();
			return this;
		}

		public AssertThatFile hasBinaryContent(byte[] expected) {
			assertThat(dir.resolve(file))
					.hasBinaryContent(expected);
			return this;
		}
	}

	public static class AssertThatFileSet {
		private final String[] files;

		public AssertThatFileSet(String... files) {
			this.files = files;
		}

		public AssertThatFileSet haveContent(String expected) {
			for (String f : files) {
				assertThat(dir.resolve(f))
						.hasContent(expected);
			}
			return this;
		}

		public AssertThatFileSet areRegularFiles() {
			for (String f : files) {
				assertThat(dir.resolve(f))
						.isRegularFile();
			}
			return this;
		}
	}

	public static class GivenFile {
		private final String file;

		public GivenFile(String file) {
			this.file = file;
		}

		public GivenFile contains(String content) {
			writeUtf8File(dir.resolve(file), content);
			return this;
		}

		public GivenFile exists() {
			createFile(dir.resolve(file));
			return this;
		}
	}

	public static class GivenFileSet {
		private final String[] files;

		public GivenFileSet(String... files) {
			this.files = files;
		}

		public GivenFileSet contain(String content) {
			for (String f : files) {
				writeUtf8File(dir.resolve(f), content);
			}
			return this;
		}

		public GivenFileSet exist() {
			for (String f : files) {
				createFile(dir.resolve(f));
			}
			return this;
		}
	}
}
