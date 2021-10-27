package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import java.io.FileInputStream;
import java.io.InputStream;

public class FontSpriteSheetDumper {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("No font sprite file specified");
			return;
		}

		final InputStream in = new FileInputStream(args[0]);
		final FontSpriteSheet sheet = new FontSpriteSheet(in);
		System.out.println(sheet.dumpProperties());
	}
}
