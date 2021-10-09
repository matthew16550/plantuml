package net.sourceforge.plantuml.approvaltesting;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import net.sourceforge.plantuml.utils.functional.Callback;
import net.sourceforge.plantuml.utils.functional.SingleCallback;

public interface ApprovalTesting {

	ApprovalTesting approve(BufferedImage value);

	ApprovalTesting approve(String value);

	ApprovalTesting test(Callback callback);

	ApprovalTesting withExtension(String extensionWithDot);

	ApprovalTesting withFileSpamLimit(int limit);

	ApprovalTesting withOutput(String extraSuffix, String extensionWithDot, SingleCallback<Path> callback);

	ApprovalTesting withSuffix(String suffix);
}
