package net.sourceforge.plantuml.test.outputs;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.ExtensionContext;

@SuppressWarnings("UnusedReturnValue")
public interface TestOutputs {

	static TestOutputs forContext(ExtensionContext context) {
		return new TestOutputsImpl(context);
	}

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

	Output spamOrException(String name);

	Output spamOrLogged(String name);

	interface Output {
		Path getPath();

		void write(Object content) throws IOException;
	}
}
