package net.sourceforge.plantuml.test.logger;

public class TestLogEvent {
	public enum Level {
		DEBUG, INFO, ERROR, WARNING
	}

	private final Level level;
	private final String message;
	private final String details;
	private final String file;
	private final int line;

	public TestLogEvent(Level level, String message, String details, String file, int line) {
		this.level = level;
		this.message = message;
		this.details = details;
		this.file = file;
		this.line = line;
	}

	public String getDetails() {
		return details;
	}

	public String getFile() {
		return file;
	}

	public Level getLevel() {
		return level;
	}

	public int getLine() {
		return line;
	}

	public String getMessage() {
		return message;
	}
}
