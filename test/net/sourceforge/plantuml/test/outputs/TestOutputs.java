package net.sourceforge.plantuml.test.outputs;

import java.nio.file.Path;

@SuppressWarnings("UnusedReturnValue")
public interface TestOutputs {

	//
	// Configure
	//

	TestOutputs deleteAfterTestPasses(String pattern);

	TestOutputs dir(Path dir);

	TestOutputs reusePaths(boolean allowOverwrite);

	TestOutputs spamLimit(int spamLimit);

	//
	// Use
	//

	Output out(String name);

	Output spam(String name);

	interface Output {
		Path getPath();

		boolean write(Object content);
	}
}
