package net.sourceforge.plantuml.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	public static final DateFormat DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z = createDateFormatUtc("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public static DateFormat createDateFormatUtc(String pattern) {
		final DateFormat result = new SimpleDateFormat(pattern, Locale.US);
		result.setTimeZone(UTC);
		return result;
	}
}
