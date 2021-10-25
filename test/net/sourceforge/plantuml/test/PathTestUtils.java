package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PathTestUtils {

	public static void assertThatDirContainsExactlyTheseFiles(Path dir, String... values) {
		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder(values);
	}

	public static void assertThatDirContainsExactlyTheseFiles(Path dir, Iterable<String> values) {
		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrderElementsOf(values);
	}

	public static Path createDirectories(Path path) {
		try {
			return Files.createDirectories(path);
		} catch (IOException e) {
			throwAsUncheckedException(e);
			return Paths.get("");  // this line will never run - but it appeases the compiler
		}
	}

	public static Path createFile(Path path, FileAttribute<?>... attrs) {
		try {
			return Files.createFile(path, attrs);
		} catch (IOException e) {
			throwAsUncheckedException(e);
			return Paths.get("");  // this line will never run - but it appeases the compiler
		}
	}

	public static Optional<String> getFileExtension(Path path) {
		final String fileName = path.getFileName().toString();
		final int index = fileName.lastIndexOf('.');
		return index == -1 ? Optional.empty() : Optional.of(fileName.substring(index + 1));
	}

	public static Stream<Path> glob(Path dir, String globPattern) {
		try {
			final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
			return Files
					.walk(dir)
					.filter(matcher::matches);
		} catch (IOException e) {
			throwAsUncheckedException(e);
			return Stream.empty();  // this line will never run - but it appeases the compiler
		}
	}

	public static List<String> listAllFilesRecursive(Path dir) {
		return glob(dir, "**")
				.filter(path -> !Files.isDirectory(path))
				.map(dir::relativize)
				.map(Path::toString)
				.map(s -> s.replace('\\', '/'))
				.sorted()
				.collect(toList());
	}

	public static String readUtf8File(Path path) {
		try {
			return new String(Files.readAllBytes(path), UTF_8);
		} catch (IOException e) {
			throwAsUncheckedException(e);
			return "";  // this line will never run - but it appeases the compiler
		}
	}

	public static void writeUtf8File(String string, Path path) {
		try {
			Files.createDirectories(path.getParent());
			Files.write(path, string.getBytes(UTF_8));
		} catch (IOException e) {
			throwAsUncheckedException(e);
		}
	}
}
