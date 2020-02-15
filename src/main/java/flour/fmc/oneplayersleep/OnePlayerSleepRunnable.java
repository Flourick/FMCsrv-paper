package flour.fmc.oneplayersleep;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;

/**
 * Runnable of the OnePlayerSleep module
 * 
 * @author Flourick
 */
public class OnePlayerSleepRunnable implements Runnable
{
	private final PlayerBedEnterEvent event;
    private boolean cancelled;
	private Player player;
    
    public OnePlayerSleepRunnable(PlayerBedEnterEvent e)
	{
        this.event = e;
		this.player = e.getPlayer();
        this.cancelled = false;
    }
    
    @Override
    public void run()
	{
        // check to make sure the player is still in bed or the event was cancelled
        if(!event.getPlayer().isSleeping() || cancelled) {
			return;
		}

        // skip time & weather
        event.getPlayer().getWorld().setTime(0L);
        event.getPlayer().getWorld().setStorm(false);
        event.getPlayer().getWorld().setThundering(false);
        
        cancelled = true;
    }
	
	public Player getPlayer()
	{
		return player;
	}
	
	public boolean isCancelled()
	{
		return cancelled;
	}
    
    public void cancel()
	{
        cancelled = true;
    }
}
