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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import net.sourceforge.plantuml.security.SImageIO;

public class PngIO {

	public static final String PLANTUML_METADATA_TAG = "plantuml";

	public static String readPlantUmlMetadata(File file) throws Exception {
		try (PngReader reader = reader(file)) {
			return reader.findMetadataValue(PLANTUML_METADATA_TAG);
		}
	}

	public static PngReader reader(File file) throws IOException {
		return new PngReader(SImageIO.createImageInputStream(file));
	}

	public static PngReader reader(InputStream is) throws IOException {
		return new PngReader(SImageIO.createImageInputStream(is));
	}

	public static PngReader reader(Path path) throws IOException {
		return new PngReader(SImageIO.createImageInputStream(path.toFile()));
	}

	public static PngWriter writer() throws IOException {
		return new PngWriter();
	}

//	/** writes a BufferedImage of type TYPE_INT_ARGB to PNG using PNGJ */
//	public static void writeARGB(BufferedImage bi, OutputStream os, String metadata) {
//		// if (bi.getType() != BufferedImage.TYPE_INT_ARGB)
//		// throw new PngjException("This method expects  BufferedImage.TYPE_INT_ARGB");
//		ImageInfo imi = new ImageInfo(bi.getWidth(), bi.getHeight(), 8, false);
//		PngChunkTEXT chunkText = new PngChunkTEXT(imi, "copyleft", copyleft);
//		// PngChunkTEXT chunkTextDebug = new PngChunkTEXT(imi, "debug", "debugData");
//		PngChunkITXT meta = new PngChunkITXT(imi);
//		meta.setKeyVal("plantuml", metadata);
//		meta.setCompressed(true);
//
//		PngWriter pngw = new PngWriter(os, imi);
//		pngw.setCompLevel(9);// maximum compression, not critical usually
//		// pngw.setFilterType(FilterType.FILTER_ADAPTIVE_FAST); // see what you prefer here
//		// pngw.setFilterType(FilterType.FILTER_ADAPTIVE_MEDIUM); // see what you prefer here
//		pngw.setFilterType(FilterType.FILTER_ADAPTIVE_FULL); // see what you prefer here
//		pngw.queueChunk(chunkText);
//		// // pngw.queueChunk(chunkTextDebug);
//		pngw.queueChunk(meta);
//		DataBufferInt db = ((DataBufferInt) bi.getRaster().getDataBuffer());
//		SinglePixelPackedSampleModel samplemodel = (SinglePixelPackedSampleModel) bi.getSampleModel();
//		if (db.getNumBanks() != 1)
//			throw new PngjException("This method expects one bank");
//		ImageLineInt line = new ImageLineInt(imi);
//		for (int row = 0; row < imi.rows; row++) {
//			int elem = samplemodel.getOffset(0, row);
//			for (int col = 0, j = 0; col < imi.cols; col++) {
//				int sample = db.getElem(elem++);
//				line.scanline[j++] = (sample & 0xFF0000) >> 16; // R
//				line.scanline[j++] = (sample & 0xFF00) >> 8; // G
//				line.scanline[j++] = (sample & 0xFF); // B
//				// line.scanline[j++] = (((sample & 0xFF000000) >> 24) & 0xFF); // A
//			}
//			pngw.writeRow(line, row);
//		}
//		pngw.end();
//	}

}
