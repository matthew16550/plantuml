package net.sourceforge.plantuml.utils;

import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.security.SImageIO;

public class ImageUtils {

	public static ImageReader createImageReader(ImageInputStream iis) throws IOException {
		final Iterator<ImageReader> readers = SImageIO.getImageReaders(iis);

		if (!readers.hasNext()) {
			throw new IOException("No suitable ImageReader");
		}

		final ImageReader reader = readers.next();
		reader.setInput(iis, true);
		return reader;
	}

	public static String renderingDebugInfo() {
		return RenderingDebugInfoHolder.renderingDebugInfo;
	}

	private static class RenderingDebugInfoHolder {
		private static final String renderingDebugInfo;

		static {
			final StringBuilder b = new StringBuilder();

			final String[] props = new String[]{
					"java.runtime.name", "java.runtime.version",
					"java.vendor", "java.vendor.version",
					"java.vm.name", "java.vm.version",
					"os.arch", "os.name", "os.version"
			};

			for (String p : props) {
				b.append(p).append('=').append(System.getProperty(p)).append('\n');
			}

			b.append("Rendering Engine=");
			try {
				final Object renderingEngine = Class.forName("sun.java2d.pipe.RenderingEngine").getMethod("getInstance").invoke(new Object[]{});
				b.append(renderingEngine.getClass().getName());
			} catch (Throwable t) {
				if (t instanceof IllegalAccessError) {
					Log.error("--add-exports java.desktop/sun.java2d.pipe=ALL-UNNAMED");
				}
				b.append("<exception>");
			}
			b.append('\n');

			b.append("sun.java2d.marlin.Version=");
			try {
				b.append(Class.forName("sun.java2d.marlin.Version").getMethod("getVersion").invoke(new Object[]{}));
			} catch (Throwable t) {
				if (t instanceof IllegalAccessError) {
					Log.error("--add-exports java.desktop/sun.java2d.marlin=ALL-UNNAMED");
				}
				b.append("<exception>");
			}
			b.append('\n');

			renderingDebugInfo = b.toString();
		}
	}
}
