package net.sourceforge.plantuml.approvaltesting;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import net.sourceforge.plantuml.utils.functional.Callback;
import net.sourceforge.plantuml.utils.functional.SingleCallback;

public interface ApprovalTesting {

	ApprovalTesting approve(BufferedImage value);

	ApprovalTesting approve(String value);

	ApprovalTesting test(Callback callback);

	ApprovalTesting withDir(Path dir);
	
	ApprovalTesting withDuplicateFiles();
	
	ApprovalTesting withExtension(String extensionWithDot);

	ApprovalTesting withFileSpamLimit(int limit);

	ApprovalTesting withOutput(String name, String extensionWithDot, SingleCallback<Path> callback);
}
