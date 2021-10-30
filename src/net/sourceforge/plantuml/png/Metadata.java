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

import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.security.SImageIO;
import net.sourceforge.plantuml.utils.ImageUtils;

public class Metadata {

	public static void main(String[] args) throws IOException {
		final Metadata meta = new Metadata();
		for (String arg : args) {
			meta.readAndDisplayMetadata(new SFile(arg));
		}
	}

	public void readAndDisplayMetadata(SFile file) throws IOException {
		final ImageInputStream iis = SImageIO.createImageInputStream(file);
		final ImageReader reader = ImageUtils.createImageReader(iis);

		// read metadata of first image
		final IIOMetadata metadata = reader.getImageMetadata(0);

		for (String name : metadata.getMetadataFormatNames()) {
			displayMetadata(metadata.getAsTree(name));
		}
	}

	private void displayMetadata(Node root) {
		displayMetadata(root, 0);
	}

	private void indent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("    ");
		}
	}

	private void displayMetadata(Node node, int level) {
		// print open tag of element
		indent(level);
		System.out.print("<" + node.getNodeName());
		final NamedNodeMap map = node.getAttributes();
		if (map != null) {

			// print attribute values
			final int length = map.getLength();
			for (int i = 0; i < length; i++) {
				final Node attr = map.item(i);
				System.out.print(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
			}
		}

		Node child = node.getFirstChild();
		if (child == null) {
			// no children, so close element and return
			System.out.println("/>");
			return;
		}

		// children, so close current tag
		System.out.println(">");
		while (child != null) {
			// print children recursively
			displayMetadata(child, level + 1);
			child = child.getNextSibling();
		}

		// print close tag of element
		indent(level);
		System.out.println("</" + node.getNodeName() + ">");
	}

}
