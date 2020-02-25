package flour.fmc.utils;

import java.util.Arrays;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

/**
 * Log4j Filter that filters console using a given array of strings
 * 
 * @author Flourick
 */
public class Log4jFilter implements Filter
{
	private LifeCycle.State state = LifeCycle.State.STARTED;;
	private String[] filterWords;
	
	private boolean startsWithMode;
	
	public Log4jFilter(String[] filterWords)
	{
		this.filterWords = filterWords;
		startsWithMode = false;
	}
	
	public Log4jFilter(String[] filterWords, boolean startsWithMode)
	{
		this.filterWords = filterWords;
		this.startsWithMode = startsWithMode;
	}
	
	private Filter.Result checkMessage(String message)
	{
		if(startsWithMode) {
			if(Arrays.stream(filterWords).parallel().anyMatch(message::startsWith)) {
				return Filter.Result.DENY;
			}
			else {
				return Filter.Result.NEUTRAL;
			}
		}
		else {
			if(Arrays.stream(filterWords).parallel().anyMatch(message::contains)) {
				return Filter.Result.DENY;
			}
			else {
				return Filter.Result.NEUTRAL;
			}
		}
    }
	
	@Override
	public LifeCycle.State getState()
	{
		return state;
	}
	
	@Override
	public void initialize()
	{
	}

	@Override
	public void start()
	{
		state = LifeCycle.State.STARTED;
	}

	@Override
	public void stop()
	{
		state = LifeCycle.State.STOPPED;
	}

	@Override
	public boolean isStarted()
	{
		return true ? state == LifeCycle.State.STARTED : false;
	}

	@Override
	public boolean isStopped()
	{
		return true ? state == LifeCycle.State.STOPPED : false;
	}
	
	@Override
	public Filter.Result getOnMismatch()
	{
		return Filter.Result.NEUTRAL;
	}

	@Override
	public Filter.Result getOnMatch()
	{
		return Filter.Result.NEUTRAL;
	}

	@Override
	public Filter.Result filter(LogEvent le)
	{
		return checkMessage(le.getMessage().getFormattedMessage());
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o)
	{
		return checkMessage(string);
	}
	
	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object... os)
	{
		return checkMessage(string);
	}
	
	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, Object o, Throwable thrwbl)
	{
		return checkMessage(o.toString());
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable thrwbl)
	{
		return checkMessage(msg.getFormattedMessage());
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8)
	{
		return checkMessage(string);
	}

	@Override
	public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
	{
		return checkMessage(string);
	}
}
