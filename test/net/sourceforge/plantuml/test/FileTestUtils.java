package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static net.sourceforge.plantuml.test.LoggingTestUtils.logError;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class FileTestUtils {

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
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return Paths.get("");  // this line will never run - but it appeases the compiler
		}
	}

	public static void delete(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logError(e, "Error deleting file '%s' : %s", path, e.getMessage());
		}
	}

	public static String readUtf8File(Path path) {
		try {
			return new String(readAllBytes(path), UTF_8);
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return "";  // this line will never run - but it appeases the compiler
		}
	}

	public static void writeUtf8File(Path path, String string) {
		try {
			Files.createDirectories(path.getParent());
			Files.write(path, string.getBytes(UTF_8));
		} catch (Exception e) {
			throwAsUncheckedException(e);
		}
	}
}
