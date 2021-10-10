package net.sourceforge.plantuml.test;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Stream;

public class PathUtils {

	public static List<String> listAllFilesRecursive(Path dir) {
		try {
			return glob(dir, "**")
					.filter(path -> !Files.isDirectory(path))
					.map(dir::relativize)
					.map(Path::toString)
					.map(s -> s.replace('\\', '/'))
					.sorted()
					.collect(toList());
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return emptyList();  // this line will never run - but it appeases the compiler
		}
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
