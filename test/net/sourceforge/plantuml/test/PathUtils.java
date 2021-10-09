package net.sourceforge.plantuml.test;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Stream;

public class PathUtils {

	public static List<String> listAllFilesRecursive(Path dir) throws Exception {
		return glob(dir, "**")
				.filter(path -> !Files.isDirectory(path))
				.map(dir::relativize)
				.map(Path::toString)
				.map(s -> s.replace('\\', '/'))
				.sorted()
				.collect(toList());
	}

	public static Stream<Path> glob(Path dir, String globPattern) throws IOException {
		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
		return Files
				.walk(dir)
				.filter(matcher::matches);
	}
}
