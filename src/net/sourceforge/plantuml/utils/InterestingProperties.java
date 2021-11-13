package net.sourceforge.plantuml.utils;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.plantuml.security.SecurityProfile;
import net.sourceforge.plantuml.security.SecurityUtils;
import net.sourceforge.plantuml.version.Version;

public class InterestingProperties {

	public static List<String> interestingProperties() {
		return InstanceHolder.INTERESTING_PROPERTIES;
	}

	// Rasterizer metadata can be helpful when comparing pixel output from different JREs
	//
	// It is only available when PLANTUML_SECURITY_PROFILE=UNSECURE
	//
	// And from Java 9 we would need to run java with these:
	//   --add-exports java.desktop/sun.java2d.marlin=ALL-UNNAMED
	//   --add-exports java.desktop/sun.java2d.pipe=ALL-UNNAMED
	public static boolean isRasterizerMetadataAvailable() {
		return InstanceHolder.RASTERIZER_METADATA_AVAILABLE;
	}

	private static class InstanceHolder {
		private static final List<String> INTERESTING_PROPERTIES;
		private static final boolean RASTERIZER_METADATA_AVAILABLE;

		static {
			final List<String> interesting = new ArrayList<>();
			boolean rasterizerMetadataAvailable = false;
			final Properties system = System.getProperties();

			interesting.add(String.format("PlantUML Version: %s (%s)",
					Version.versionString(), DateTimeUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z.format(Version.compileTime())));

			// Different JREs use different combinations of java.runtime / java.vendor / java.vm,
			// so we include all of them to make sure we capture the needed info

			interesting.add("Java Runtime: " + system.get("java.runtime.name"));
			interesting.add("Java Vendor: " + system.get("java.vendor"));
			interesting.add("JVM: " + system.get("java.vm.name"));

			if (SecurityUtils.getSecurityProfile() == SecurityProfile.UNSECURE) {
				interesting.add("Java Runtime Version: " + system.get("java.runtime.version"));
				interesting.add("Java Vendor Version: " + system.get("java.vendor.version"));
				interesting.add("Java VM Version: " + system.get("java.vm.version"));
				interesting.add("Operating System: " + system.get("os.name"));
				interesting.add("OS Version: " + system.get("os.version"));
				interesting.add("OS Architecture: " + system.get("os.arch"));
				interesting.add("Default Encoding: " + system.get("file.encoding"));

				String renderingEngine = "";
				try {
					renderingEngine = Class.forName("sun.java2d.pipe.RenderingEngine").getMethod("getInstance").invoke(null).getClass().getName();
					interesting.add("Rendering Engine: " + renderingEngine);
					rasterizerMetadataAvailable = true;
				} catch (Throwable ignored) {
				}

				if (renderingEngine.startsWith("sun.java2d.marlin.")) {
					try {
						interesting.add("Marlin Version: " +
								Class.forName("sun.java2d.marlin.Version").getMethod("getVersion").invoke(null));
					} catch (Throwable ignored) {
						rasterizerMetadataAvailable = false;
					}
				}
			}

			interesting.add("Language: " + system.get("user.language"));
			interesting.add("Country: " + system.get("user.country"));

			INTERESTING_PROPERTIES = unmodifiableList(interesting);
			RASTERIZER_METADATA_AVAILABLE = rasterizerMetadataAvailable;
		}
	}
}
