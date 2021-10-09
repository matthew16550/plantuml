package net.sourceforge.plantuml.approvaltesting;

import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.util.ReflectionUtils.findFields;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApprovalTestingJUnitExtension implements BeforeAllCallback, BeforeEachCallback {

	private ApprovalTestingImpl approvalTestingImpl;

	@Override
	public void beforeAll(ExtensionContext context) {
		approvalTestingImpl = new ApprovalTestingImpl(Paths.get("test"), context.getRequiredTestClass().getName());
	}

	/**
	 * Injects {@link ApprovalTesting} fields;
	 */
	@Override
	public void beforeEach(ExtensionContext context) {

		final Predicate<Field> filter = field -> ApprovalTesting.class.isAssignableFrom(field.getType());

		findFields(context.getRequiredTestClass(), filter, TOP_DOWN).forEach(field -> {
			try {
				makeAccessible(field)
						.set(context.getRequiredTestInstance(), approvalTestingImpl.forExtensionContext(context));
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});
	}
}
