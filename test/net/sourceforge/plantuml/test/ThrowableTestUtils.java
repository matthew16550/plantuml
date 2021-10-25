package net.sourceforge.plantuml.test;

import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import org.opentest4j.AssertionFailedError;

public class ThrowableTestUtils {

	public static void rethrowWithMessage(Throwable t, String message) {
		if (t instanceof AssertionFailedError) {
			final AssertionFailedError assertionFailedError = (AssertionFailedError) t;
			throw new AssertionFailedError(message, assertionFailedError.getExpected(), assertionFailedError.getActual(), t);
		}

		// throwAsUncheckedException() is a kludge to avoid spreading "throws Throwable" through the code
		throwAsUncheckedException(new Throwable(message, t));
	}
}
