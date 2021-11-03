package net.sourceforge.plantuml.utils;

import static net.sourceforge.plantuml.StringUtils.multilineString;
import static net.sourceforge.plantuml.utils.InterestingProperties.isRasterizerMetadataAvailable;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.sourceforge.plantuml.test.logger.TestLogger;

class InterestingPropertiesTest {

	@Test
	void test_interestingProperties(TestLogger logger) {
		final String interestingProperties = multilineString(InterestingProperties.interestingProperties());
		logger
				.withDetails(interestingProperties)
				.info("Interesting Properties");

		// Just a smoke test
		assertThat(interestingProperties)
				.startsWith("PlantUML Version: ");
	}

	@Test
	void test_isRasterizerMetadataAvailable() {
		// This test alerts if we accidentally change CI so rasterizer metadata is not available
		// because it might be important when an image approval test fails
		assertThat(isRasterizerMetadataAvailable())
				.isTrue();
	}
}
