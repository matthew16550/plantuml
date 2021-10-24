/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.png;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import net.sourceforge.plantuml.security.ImageIO;

public class PngMetadataReader {

	public static PngMetadataReader create(File file) throws IOException {
		final ImageInputStream stream = ImageIO.createImageInputStream(file);
		final ImageReader reader = ImageIO.createImageReader(stream);
		return new PngMetadataReader(reader.getImageMetadata(0));
	}

	private final IIOMetadata metadata;

	public PngMetadataReader(IIOMetadata metadata) {
		this.metadata = metadata;
	}

	public String findMetadataValue(String tag) {
			final String[] names = metadata.getMetadataFormatNames();
			final int length = names.length;
			for (int i = 0; i < length; i++) {
				final String result = displayMetadata(metadata.getAsTree(names[i]), tag);
				if (result != null) {
					return result;
				}
			}

		return null;
	}

	public float getRequiredFloat(String tag) {
		return parseFloat(getRequiredString(tag));
	}

	public int getRequiredInt(String tag) {
		return parseInt(getRequiredString(tag));
	}

	public String getRequiredString(String tag) {
		final String string = findMetadataValue(tag);
		if (string == null) {
			throw new IllegalStateException("PNG metadata is missing: " + tag);
		}
		return string;
	}

	public String getPlantUmlMetadata() throws IOException {
		return findMetadataValue("plantuml");
	}

	private static String displayMetadata(Node root, String tag) {
		return displayMetadata(root, tag, 0);
	}

	private static String displayMetadata(Node node, String tag, int level) {
		final NamedNodeMap map = node.getAttributes();
		if (map != null) {
			final Node keyword = map.getNamedItem("keyword");
			if (keyword != null && tag.equals(keyword.getNodeValue())) {
				final Node text = map.getNamedItem("value");
				if (text != null) {
					return text.getNodeValue();
				}
			}
		}

		Node child = node.getFirstChild();

		// children, so close current tag
		while (child != null) {
			// print children recursively
			final String result = displayMetadata(child, tag, level + 1);
			if (result != null) {
				return result;
			}
			child = child.getNextSibling();
		}

		return null;

	}

}
