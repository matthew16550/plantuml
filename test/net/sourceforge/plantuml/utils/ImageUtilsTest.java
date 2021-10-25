package net.sourceforge.plantuml.utils;

import org.junit.jupiter.api.Test;

import net.sourceforge.plantuml.test.logger.TestLogger;

class ImageUtilsTest {

	@Test
	void test_renderingDebugInfo(TestLogger logger) {
		logger.info(ImageUtils.renderingDebugInfo());
	}
}
