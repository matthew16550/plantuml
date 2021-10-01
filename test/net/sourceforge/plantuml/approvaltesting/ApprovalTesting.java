package net.sourceforge.plantuml.approvaltesting;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface ApprovalTesting {

	ApprovalTesting approve(BufferedImage value);

	ApprovalTesting approve(String value);

	Path createPathForOutput(String suffix);

	String getBaseName();

	Path getDir();

	ApprovalTesting withExtension(String extensionWithDot);

	ApprovalTesting withSuffix(String suffix);
}