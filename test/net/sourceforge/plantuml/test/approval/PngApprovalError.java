package net.sourceforge.plantuml.test.approval;

import static net.sourceforge.plantuml.test.MultilineStringBuilder.multilineStringBuilder;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import net.sourceforge.plantuml.test.MultilineStringBuilder;
import net.sourceforge.plantuml.utils.InterestingProperties;

public class PngApprovalError extends AssertionFailedError {

	public PngApprovalError(AssertionFailedError cause, String message, String expectedMetadata) {
		super(
				message != null ? message : cause.getMessage(),
				ValueWrapper.create(
						cause.isExpectedDefined() ? cause.getExpected().getValue() : null,
						expectedStringRepresentation(cause, expectedMetadata)
				),
				ValueWrapper.create(
						cause.isActualDefined() ? cause.getActual().getValue() : null,
						actualStringRepresentation(cause)
				),
				cause
		);
	}

	private static String expectedStringRepresentation(AssertionFailedError cause, String expectedMetadata) {
		final MultilineStringBuilder b = multilineStringBuilder();

		if (cause.isExpectedDefined())
			b.lines(
					cause.getExpected().getStringRepresentation(),
					""
			);

		return b.lines(
				expectedMetadata
		).toString();
	}

	private static String actualStringRepresentation(AssertionFailedError cause) {
		final MultilineStringBuilder b = multilineStringBuilder();

		if (cause.isActualDefined())
			b.lines(
					cause.getActual().getStringRepresentation(),
					""
			);

		return b.lines(
				"<This Runtime>",
				InterestingProperties.interestingProperties()
		).toString();
	}
}
