package net.sourceforge.plantuml.test.outputs;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestOutputsExtension implements BeforeAllCallback, AfterEachCallback, ParameterResolver {

	public static TestOutputs getImpl(ExtensionContext context) {
		return new TestOutputsImpl(context);
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		final String classWithSlashes = context.getRequiredTestClass().getName().replace('.', File.separatorChar);
		final Path dir = Paths.get("test").resolve(classWithSlashes).getParent();
		new TestOutputsImpl(context).dir(dir);
	}

//	@Override
//	public void beforeEach(ExtensionContext context) {
//		final String displayName = context.getDisplayName();
//		final String methodName = context.getRequiredTestMethod().getName();
//
//		StringBuilder b = new StringBuilder()
//				.append(simplifyName(context.getRequiredTestClass().getSimpleName()))
//				.append('.')
//				.append(simplifyName(methodName))
//				.append('.');
//
//		if (!displayName.equals(methodName + "()")) {
//			b.append(simplifyName(displayName)).append('.');
//		}
//
//		new TestArtifactManagerImpl(context).basename(b.toString());
//	}

	@Override
	public void afterEach(ExtensionContext context) {
		if (!context.getExecutionException().isPresent()) {
			new TestOutputsImpl(context).doDeleteAfterTestPasses();
		}
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return parameterContext.getParameter().getType() == TestOutputs.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return getImpl(extensionContext);
	}
}
