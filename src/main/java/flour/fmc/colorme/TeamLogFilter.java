package flour.fmc.colorme;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

/**
 * Filters team command console logging so it does not spam the console
 * 
 * @author Flourick
 */
public class TeamLogFilter implements Filter
{
	private LifeCycle.State state = LifeCycle.State.STARTED;;
	
	public Filter.Result checkMessage(String message)
	{
        if(message.contains("Created team ") || message.contains("A team already exists")) {
			return Filter.Result.DENY;
		}
		else {
			return Filter.Result.NEUTRAL;
		}
    }
	
	@Override
	public State getState()
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
	public Result getOnMismatch()
	{
		return Result.NEUTRAL;
	}

	@Override
	public Result getOnMatch()
	{
		return Result.NEUTRAL;
	}

	@Override
	public Result filter(LogEvent le)
	{
		return checkMessage(le.getMessage().getFormattedMessage());
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o)
	{
		return checkMessage(string);
	}
	
	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object... os)
	{
		return checkMessage(string);
	}
	
	@Override
	public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable thrwbl)
	{
		return checkMessage(o.toString());
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable thrwbl)
	{
		return checkMessage(msg.getFormattedMessage());
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8)
	{
		return checkMessage(string);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String string, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
	{
		return checkMessage(string);
	}
}
