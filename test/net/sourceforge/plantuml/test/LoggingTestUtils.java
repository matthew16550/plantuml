package net.sourceforge.plantuml.test;

public class LoggingTestUtils {

	public static void logError(Exception e, String message, Object... args) {
		System.err.printf("ERROR  : " + message + "%n", args);
		System.err.println();
		e.printStackTrace();
	}
	
	public static void logWarning(String message, Object... args) {
		System.err.printf("WARNING : " + message + "%n", args);
		System.err.println();
	}
}
