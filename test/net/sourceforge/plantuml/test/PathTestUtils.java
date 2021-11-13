package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PathTestUtils {

	public static void assertThatDirContainsExactlyTheseFiles(Path dir, String... values) throws IOException {
		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder(values);
	}

	public static void assertThatDirContainsExactlyTheseFiles(Path dir, Iterable<String> values) throws IOException {
		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrderElementsOf(values);
	}

	public static Optional<String> getFileExtension(Path path) {
		final String fileName = path.getFileName().toString();
		final int index = fileName.lastIndexOf('.');
		return index == -1 ? Optional.empty() : Optional.of(fileName.substring(index + 1));
	}

	public static Stream<Path> glob(Path dir, String globPattern) throws IOException {
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
		return Files
				.walk(dir)
				.filter(matcher::matches);
	}

	public static List<String> listAllFilesRecursive(Path dir) throws IOException {
		return glob(dir, "**")
				.filter(path -> !Files.isDirectory(path))
				.map(dir::relativize)
				.map(Path::toString)
				.map(s -> s.replace('\\', '/'))
				.sorted()
				.collect(toList());
	}

	public static String readUtf8File(Path path) throws IOException {
		return new String(Files.readAllBytes(path), UTF_8);
	}

	public static void writeUtf8File(String string, Path path) throws IOException {
		Files.createDirectories(path.getParent());
		Files.write(path, string.getBytes(UTF_8));
	}
}
