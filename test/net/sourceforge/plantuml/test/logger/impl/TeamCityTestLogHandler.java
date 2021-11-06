package net.sourceforge.plantuml.test.logger.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.test.logger.TestLogEvent;
import net.sourceforge.plantuml.test.logger.TestLogHandler;

/**
 * https://www.jetbrains.com/help/teamcity/service-messages.html
 */
public class TeamCityTestLogHandler implements TestLogHandler {

	@Override
	public void handle(TestLogEvent event) {
		final String status;

		switch (event.getLevel()) {
			case DEBUG:
				// TODO
				return;
			case INFO:
				status = "NORMAL";
				break;
			case WARNING:
				status = "WARNING";
				break;
			case ERROR:
				status = "ERROR";
				break;
			default:
				throw new UnsupportedOperationException();
		}
// TODO details are not shown if test passes?  or for INFO ?
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		final PrintWriter w = new PrintWriter(baos);

		// Suspect that "flowId" would be important when tests are run in parallel
		w.format("##teamcity[message status='%s' text='", status);
		appendValue(w, event.getMessage());

		if (event.getDetails() != null) {
			w.append("' errorDetails='");
			appendValue(w, event.getDetails());
		}
		
		w.append("']").append(StringUtils.EOL);
		w.flush();

		final byte[] bytes = baos.toByteArray();
		System.out.write(bytes, 0, bytes.length);
		System.out.flush();
	}

	private void appendValue(PrintWriter w, String value) {
		for (int c : (Iterable<Integer>) value.codePoints()::iterator) {
			switch (c) {
				case '\'':
				case '|':
				case '[':
				case ']':
					w.append('|').append((char) c);
					continue;
				case '\n':
					w.append("|n");
					continue;
				case '\r':
					w.append("|r");
					continue;
			}

			if (c > 255) {
				w.format("|0x%04X", c);
			} else {
				w.append((char) c);
			}
		}
	}
}
