package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.StringUtils.EOL;

import java.util.Formatter;
import java.util.Locale;
import java.util.stream.Stream;

public class MultilineStringBuilder {

	public static MultilineStringBuilder multilineStringBuilder(Object... lines) {
		return new MultilineStringBuilder().lines(lines);
	}

	public static Object f(String format, Object... args) {
		return new LazyFormat(format, args);
	}

	private static class LazyFormat {
		final String format;
		final Object[] args;

		public LazyFormat(String format, Object[] args) {
			this.format = format;
			this.args = args;
		}
	}

	private final Formatter formatter;
	private final StringBuilder internal = new StringBuilder();

	private MultilineStringBuilder() {
		formatter = new Formatter(internal, Locale.US);
	}

	public MultilineStringBuilder append(Object obj) {
		if (obj instanceof LazyFormat) {
			final LazyFormat lazyFormat = (LazyFormat) obj;
			formatter.format(lazyFormat.format, lazyFormat.args);
		} else {
			internal.append(obj);
		}
		return this;
	}

	public MultilineStringBuilder line(Object obj) {
		final int length = internal.length();
		append(obj);
		if (internal.length() == length || !endsWithEOL())
			internal.append(EOL);
		return this;
	}

	public MultilineStringBuilder lines(Object... objects) {
		for (Object o : objects) {
			if (o instanceof Iterable)
				((Iterable<?>) o).forEach(this::line);
			else if (o instanceof Stream)
				((Stream<?>) o).forEach(this::line);
			else
				line(o);
		}
		return this;
	}

	public boolean endsWithEOL() {
		if (internal.length() < EOL.length())
			return false;

		final int offset = internal.length() - EOL.length();

		for (int i = 0; i < EOL.length(); i++)
			if (EOL.charAt(i) != internal.charAt(i + offset))
				return false;

		return true;
	}

	@Override
	public String toString() {
		return internal.toString();
	}
}
