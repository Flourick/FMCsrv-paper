package me.flourick.fmc.discord;

import java.util.logging.Level;

import javax.security.auth.login.LoginException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.flourick.fmc.FMC;
import me.flourick.fmc.utils.CConfig;
import me.flourick.fmc.utils.IModule;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import net.md_5.bungee.api.ChatColor;

/**
 * Discord bot chat integration module
 * 
 * @author Flourick
 */
public class Discord extends ListenerAdapter implements IModule
{
	private boolean isEnabled = false;
	private final FMC fmc;

	private final CConfig discordConfig;

	private DiscordBot bot;

	private String messageTemplate;

	public Discord(FMC fmc)
	{
		this.fmc = fmc;
		this.discordConfig = new CConfig(fmc, "discord.yml");

		// Creates default config if not already present
		discordConfig.saveDefaultConfig();
	}

	@Override
	public boolean onEnable()
	{
		String token;

		if((token = discordConfig.getConfig().getString("discord-token")).equals("TOKEN")) {
			// default config file
			fmc.getLogger().log(Level.WARNING, "[Discord] Default values in discord.yml! Please change them and restart/reload the server.");
			onDisable();
			return false;
		}

		try {
			bot = new DiscordBot(token, discordConfig.getConfig().getLong("discord-channel-id"), this);
		}
		catch (LoginException | InterruptedException e) {
			fmc.getLogger().log(Level.WARNING, "[Discord] Failed to connect to bot! Please check that the token is valid!");
			onDisable();
			return false;
		}

		messageTemplate = discordConfig.getConfig().getString("message-format");

		fmc.getServer().getPluginManager().registerEvents(new Listener()
		{
			@EventHandler(priority=EventPriority.HIGHEST)
			public void OnPlayerAsyncChat(AsyncPlayerChatEvent e)
			{
				bot.sendMessage("<**" + e.getPlayer().getName() + "**> " + ChatColor.stripColor(e.getMessage()));
			}
		}, fmc);

		if(discordConfig.getConfig().getBoolean("send-death-messages")) {
			fmc.getServer().getPluginManager().registerEvents(new Listener()
			{
				@EventHandler(priority=EventPriority.HIGHEST)
				public void OnPlayerDeath(PlayerDeathEvent e)
				{
					bot.sendMessage(ChatColor.stripColor(e.getDeathMessage()));
				}
			}, fmc);
		}

		if(discordConfig.getConfig().getBoolean("send-join-leave-messages")) {
			fmc.getServer().getPluginManager().registerEvents(new Listener()
			{
				@EventHandler(priority=EventPriority.HIGHEST)
				public void OnPlayerDeath(PlayerJoinEvent e)
				{
					if(fmc.getServer().hasWhitelist()) {
						if(e.getPlayer().isWhitelisted()) {
							bot.sendMessage(ChatColor.stripColor(e.getJoinMessage()));
						}
					}
					else {
						bot.sendMessage(ChatColor.stripColor(e.getJoinMessage()));
					}
				}
			}, fmc);

			fmc.getServer().getPluginManager().registerEvents(new Listener()
			{
				@EventHandler(priority=EventPriority.HIGHEST)
				public void OnPlayerDeath(PlayerQuitEvent e)
				{
					bot.sendMessage(ChatColor.stripColor(e.getQuitMessage()));
				}
			}, fmc);
		}

		return isEnabled = true;
	}

	public void sendChatMessage(String author, String message)
	{
		fmc.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', messageTemplate.replace("{PLAYER}", author) + message));
	}

	@Override
	public void onDisable()
	{
		if(bot != null) {
			bot.close();
		}
		
		isEnabled = false;
	}

	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}

	@Override
	public String getName()
	{
		return "Discord";
	}

	private class DiscordBot extends ListenerAdapter
	{
		private JDA jda;
		private long channelID;

		private Discord discord;

		public DiscordBot(String token, long channelID, Discord discord) throws LoginException, InterruptedException
		{
			this.jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES).build();
			this.discord = discord;
			this.channelID = channelID;

			jda.addEventListener(this);
			jda.awaitReady();
		}

		public void sendMessage(String message)
		{
			jda.getTextChannelById(channelID).sendMessage(message).queue();
		}

		@Override
		public void onMessageReceived(MessageReceivedEvent event)
		{
			if(event.getChannel().getIdLong() != channelID || event.getAuthor().isBot()) {
				return;
			}

			discord.sendChatMessage(event.getAuthor().getName(), event.getMessage().getContentStripped());
		}

		public void close()
		{
			jda.shutdown();
		}
	}
}