package net.sourceforge.plantuml.test.outputs;

import java.io.IOException;

public class TestOutputSuppressed extends IOException {

	public TestOutputSuppressed(String message) {
		super(message);
	}
}
