package net.sourceforge.plantuml.test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
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

	public static List<String> listAllFilesRecursive(Path dir) {
		return glob(dir, "**")
				.filter(path -> !Files.isDirectory(path))
				.map(dir::relativize)
				.map(Path::toString)
				.map(s -> s.replace('\\', '/'))
				.sorted()
				.collect(toList());
	}

	public static Stream<Path> glob(Path dir, String globPattern) {
		try {
			final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
			return Files
					.walk(dir)
					.filter(matcher::matches);
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return Stream.empty();  // this line will never run - but it appeases the compiler
		}
	}
}
