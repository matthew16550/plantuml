package net.sourceforge.plantuml.test.outputs;

import static java.util.Objects.requireNonNull;
import static net.sourceforge.plantuml.test.ImageTestUtils.writeImageFile;
import static net.sourceforge.plantuml.test.ImageTestUtils.writePngWithCreationMetadata;
import static net.sourceforge.plantuml.test.PathTestUtils.createDirectories;
import static net.sourceforge.plantuml.test.PathTestUtils.getFileExtension;
import static net.sourceforge.plantuml.test.PathTestUtils.glob;
import static net.sourceforge.plantuml.test.PathTestUtils.writeUtf8File;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

import net.sourceforge.plantuml.test.logger.TestLogger;

class TestOutputsImpl implements TestOutputs {

	private static final AtomicLong uniqueId = new AtomicLong();

	private final ExtensionContext context;
	private final TestLogger logger;

	public TestOutputsImpl(ExtensionContext context) {
		this.context = context;
		this.logger = TestLogger.forContext(context);
	}

	//
	// Configure
	//

	@Override
	public TestOutputs deleteAfterTestPasses(String pattern) {
		requireNonNull(pattern);
		getStore().put(Key.DELETE_AFTER_TEST_PASSES + "_" + uniqueId.getAndIncrement(), (CloseableResource) () ->
				glob(getDir(getStore()), "**/" + getBasename() + pattern)
						.filter(Files::isRegularFile)
						.filter(file -> !file.toString().endsWith(".java"))  // do not accidentally delete the test code !
						.forEach(file -> {
							try {
								Files.deleteIfExists(file);
							} catch (IOException e) {
								logger.error(e, "Error deleting '%s' : %s", file, e.getMessage());
							}
						})
		);
		return this;
	}

	@Override
	public TestOutputs dir(Path dir) {
		requireNonNull(dir);
		getStore().put(Key.DIR, dir);
		return this;
	}

	@Override
	public TestOutputs reusePaths(boolean reuseFiles) {
		getStore().put(Key.REUSE_PATHS, reuseFiles);
		return this;
	}

	@Override
	public TestOutputs spamLimit(int spamLimit) {
		getStore().put(Key.SPAM_LIMIT, spamLimit);
		return this;
	}

	//
	// Use
	//

	@Override
	public Output out(String name) {
		return new NormalOutput(usePath(name));
	}

	@Override
	public Output spam(String name) {
		return new SpamOutput(usePath(name));
	}

	//
	// Internals
	//

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TestOutputsExtension.class);

	enum Key {
		BASENAME,
		DELETE_AFTER_TEST_PASSES,
		DIR,
		REUSE_PATHS,
		SPAM_LIMIT,
	}

	private Store getStore() {
		return context.getStore(NAMESPACE);
	}

	private Path getDir(Store store) {
		return store.getOrComputeIfAbsent(
				Key.DIR,
				k -> Paths.get("test").resolve(context.getRequiredTestClass().getName().replace('.', File.separatorChar)).getParent(),
				Path.class
		);
	}

	private static final Pattern REPETITION_PATTERN = Pattern.compile("repetition (\\d+) of (\\d+)");

	private String getBasename() {
		return getStore().getOrComputeIfAbsent(Key.BASENAME, key -> createBasename(), String.class);
	}

	private String createBasename() {
		final String displayName = context.getDisplayName();
		final String methodName = context.getRequiredTestMethod().getName();

		final StringBuilder b = new StringBuilder()
				.append(simplifyName(context.getRequiredTestClass().getSimpleName()))
				.append('.')
				.append(simplifyName(methodName));

		if (!displayName.startsWith(methodName + "(")) {
			final Matcher matcher = REPETITION_PATTERN.matcher(displayName);
			if (matcher.matches()) {
				b.append('.');
				final String currentRepetition = matcher.group(1);
				final String totalRepetitions = matcher.group(2);
				for (int i = totalRepetitions.length(); i > currentRepetition.length(); i--) b.append('0');
				b.append(currentRepetition);
			} else {
				b.append('.').append(simplifyName(displayName));
			}
		}

		return b.append('.').toString();
	}

	private static class PathsUsed extends HashSet<Path> {
	}

	private Path usePath(String name) {
		requireNonNull(name);
		final Store store = getStore();
		final Path path = getDir(store).resolve(getBasename() + name);
		final PathsUsed pathsUsed = context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(PathsUsed.class);
		if (!(pathsUsed.add(path) || store.getOrDefault(Key.REUSE_PATHS, Boolean.class, false))) {
			throw new IllegalArgumentException(String.format("Trying to reuse output path '%s'", path));
		}
		return path;
	}

	private static class NormalOutput implements Output {
		protected final Path path;

		public NormalOutput(Path path) {
			this.path = path;
		}

		@Override
		public Path getPath() {
			return path;
		}

		@Override
		public boolean write(Object content) {
			requireNonNull(content);
			createDirectories(path.getParent());
			if (content instanceof BufferedImage) {
				write_image((BufferedImage) content);
			} else {
				write_object(content);
			}
			return true;
		}

		private void write_image(BufferedImage content) {
			if (getFileExtension(path).orElse("").equals("png")) {
				writePngWithCreationMetadata(content, path);
			} else {
				writeImageFile(content, path);
			}
		}

		private void write_object(Object content) {
			writeUtf8File(content.toString(), path);
		}
	}

	private static class SpamCounts extends HashMap<String, Integer> {
	}

	private class SpamOutput extends NormalOutput {
		public SpamOutput(Path path) {
			super(path);
		}

		private boolean canWrite() {
			final Store store = getStore();
			final int spamLimit = store.getOrDefault(Key.SPAM_LIMIT, Integer.class, 0);
			if (spamLimit <= 0)
				return true;
			final SpamCounts spamCounts = context.getParent().orElse(context).getStore(NAMESPACE).getOrComputeIfAbsent(SpamCounts.class);
			final int count = spamCounts.compute(context.getRequiredTestMethod().getName(), (k, v) -> v == null ? 1 : v + 1);
			if (count > spamLimit) {
				logger.warning("Suppressing spammy output file '%s'", getDir(store).relativize(path));
				return false;
			}
			return true;
		}

		@Override
		public boolean write(Object content) {
			return canWrite() && super.write(content);
		}
	}

	// VisibleForTesting
	static String simplifyName(String name) {
		return name
				.replaceAll("[^A-Za-z0-9]+", "_")
				.replaceAll("(^_+|_+$)", "");
	}
}
