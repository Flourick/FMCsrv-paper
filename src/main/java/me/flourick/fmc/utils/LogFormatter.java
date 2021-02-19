package me.flourick.fmc.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Java log formatter for logging to files in Protection module.
 * 
 * @author Flourick
 */
public class LogFormatter extends Formatter
{
	private static final String DATE_PATTERN = "dd.MM.yyyy HH:mm:ss";

	@Override
	public String format(LogRecord record)
	{
		return String.format("[%1$s] %2$s\n", new SimpleDateFormat(DATE_PATTERN).format(new Date(record.getMillis())), formatMessage(record));
	}
}
