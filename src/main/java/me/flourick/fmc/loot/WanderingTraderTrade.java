package me.flourick.fmc.loot;

import org.bukkit.inventory.MerchantRecipe;

/**
 * Wrapper for MerchantRecipe as it's read from config.
 * 
 * @author Flourick
 */
public class WanderingTraderTrade
{
	private final int groupId;
	private final int propability;
	private final MerchantRecipe trade;

	public WanderingTraderTrade(int groupId, int propability, MerchantRecipe trade)
	{
		this.groupId = groupId;
		this.propability = propability;
		this.trade = trade;
	}

	public WanderingTraderTrade(MerchantRecipe trade)
	{
		this.trade = trade;
		this.groupId = -1;
		this.propability = 100;
	}

	public int getGroupId()
	{
		return groupId;
	}

	public int getPropability()
	{
		return propability;
	}

	public MerchantRecipe getTrade()
	{
		return trade;
	}

	@Override
	public String toString()
	{
		return "WanderingTraderTrade{" + "groupId=" + groupId + ", propability=" + propability + ", trade=(" + trade.getIngredients() + ", " + trade.getResult() + ", " + trade.getUses() + ")}";
	}
}
