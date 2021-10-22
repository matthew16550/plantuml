package net.sourceforge.plantuml.test.outputs;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface TestOutputs {

	interface RegisteredPath {
		Path getPath();
	}

	//
	// Configure
	//

	TestOutputs autoSpamCount(boolean autoSpamCount);
	
	TestOutputs deleteAfterTestPasses(String pattern);

	TestOutputs dir(Path dir);

	TestOutputs reuseFiles(boolean allowOverwrite);

	TestOutputs spamLimit(int spamLimit);

	//
	// Use
	//

	TestOutputs bumpSpamCount();

	RegisteredPath registerPath(String name);

	Path usePath(String name);

	boolean write(String name, BufferedImage image);

	boolean write(RegisteredPath path, BufferedImage image);

	boolean write(String name, String content);

	boolean write(RegisteredPath path, String content);
}
