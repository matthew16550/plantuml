package net.sourceforge.plantuml.test.outputs;


import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestOutputsExtension implements ParameterResolver {

	public static TestOutputs getTestOutputs(ExtensionContext context) {
		return new TestOutputsImpl(context);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return parameterContext.getParameter().getType() == TestOutputs.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return getTestOutputs(extensionContext);
	}
}
