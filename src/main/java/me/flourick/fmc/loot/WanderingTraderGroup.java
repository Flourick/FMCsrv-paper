package me.flourick.fmc.loot;

import java.util.List;

/**
 * Holds WanderingTraderTrades that belong to the same group.
 * 
 * @author Flourick
 */
public class WanderingTraderGroup
{
	private final int groupId;
	private final List<WanderingTraderTrade> trades;

	public WanderingTraderGroup(int groupId, List<WanderingTraderTrade> trades)
	{
		this.groupId = groupId;
		this.trades = trades;
	}

	public int getGroupId()
	{
		return groupId;
	}

	public List<WanderingTraderTrade> getTrades()
	{
		return trades;
	}
}
