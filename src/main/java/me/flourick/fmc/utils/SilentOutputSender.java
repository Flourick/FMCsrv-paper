package me.flourick.fmc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/**
 * Class mainly for wrapping ConsoleCommandSender to not log messages into
 * console and instead store it internally
 * 
 * @author Flourick
 */
public class SilentOutputSender implements ConsoleCommandSender {
	private final CommandSender sender;
	private final List<String> currentMessages;

	/**
	 * The one and only constructor for SilentOutputSender class
	 * 
	 * @param sender CommandSender for which to suppress sendMessage(...) output
	 */
	public SilentOutputSender(CommandSender sender) {
		this.sender = sender;
		this.currentMessages = new ArrayList<>();
	}

	/**
	 * Gets the received messages and clears the list afterwards
	 * 
	 * @return last received message or null if no present or already consumed
	 */
	public List<String> consumeCurrentMessages() {
		List<String> msg = new ArrayList<>();
		msg.addAll(currentMessages);
		currentMessages.clear();

		return msg;
	}

	public void clearCurrentMessages() {
		currentMessages.clear();
	}

	@Override
	public void sendMessage(String message) {
		currentMessages.add(ChatColor.stripColor(message));
	}

	@Override
	public void sendMessage(String[] messages) {
		Collections.addAll(currentMessages, messages);
	}

	@Override
	public void sendMessage(UUID sender, String message)
	{
		currentMessages.add(ChatColor.stripColor(message));
	}

	@Override
	public void sendMessage(UUID sender, String[] messages)
	{
		Collections.addAll(currentMessages, messages);
	}

	@Override
	public void sendRawMessage(UUID sender, String message)
	{
		currentMessages.add(ChatColor.stripColor(message));
	}

	@Override
	public Server getServer() {
		return sender.getServer();
	}

	@Override
	public String getName() {
		return sender.getName();
	}

	@Override
	public boolean isPermissionSet(String name) {
		return sender.isPermissionSet(name);
	}

	@Override
	public boolean isPermissionSet(Permission perm) {
		return sender.isPermissionSet(perm);
	}

	@Override
	public boolean hasPermission(String name) {
		return sender.hasPermission(name);
	}

	@Override
	public boolean hasPermission(Permission perm) {
		return sender.hasPermission(perm);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		return sender.addAttachment(plugin, name, value);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin) {
		return sender.addAttachment(plugin);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
		return sender.addAttachment(plugin, name, value, ticks);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		return sender.addAttachment(plugin, ticks);
	}

	@Override
	public void removeAttachment(PermissionAttachment attachment) {
		sender.removeAttachment(attachment);
	}

	@Override
	public void recalculatePermissions() {
		sender.recalculatePermissions();
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return sender.getEffectivePermissions();
	}

	@Override
	public boolean isOp() {
		return sender.isOp();
	}

	@Override
	public void setOp(boolean value) {
		sender.setOp(value);
	}

	@Override
	public Spigot spigot() {
		return sender.spigot();
	}

	@Override
	public boolean isConversing() {
		return true;
	}

	@Override
	public void acceptConversationInput(String input) {
	}

	@Override
	public boolean beginConversation(Conversation conversation) {
		return false;
	}

	@Override
	public void abandonConversation(Conversation conversation) {
	}

	@Override
	public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
	}

	@Override
	public void sendRawMessage(String message) {
	}
}