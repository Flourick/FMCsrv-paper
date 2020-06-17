package flour.fmc.loot;

import flour.fmc.FMC;
import flour.fmc.utils.CConfig;
import flour.fmc.utils.IModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
/**
 * Loot module
 * 
 * @author Flourick
 */
public class Loot implements IModule
{
	private boolean isEnabled = false;
	private final FMC fmc;
	
	private final CConfig lootConfig;
	
	private Map<Integer, WanderingTraderGroup> customTrades;
	
	public Loot(FMC fmc)
	{
		this.fmc = fmc;
		this.lootConfig = new CConfig(fmc, "loot.yml");
		
		// Creates default config if not already present
		lootConfig.saveDefaultConfig();
	}

	@Override
	public boolean onEnable()
	{
		handleWanderingTrader();
		
		// adds one shulker shell drop to shulker
		if(lootConfig.getConfig().getBoolean("double-shulker-shells")) {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onEntitySpawn(EntityDeathEvent event)
				{
					if(event.getEntityType()== EntityType.SHULKER) {
						event.getDrops().add(new ItemStack(Material.SHULKER_SHELL, 1));
					}
				}
			}, fmc);
		}

		return isEnabled = true;
	}

	@Override
	public void onDisable()
	{
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
		return "Loot";
	}
	
	private void handleWanderingTrader()
	{
		boolean noWanderingTrader = lootConfig.getConfig().getBoolean("wandering-trader.no-wandering-trader-spawn");
		boolean enableCustomTrades = lootConfig.getConfig().getBoolean("wandering-trader.custom-trades.enabled");
		boolean removeVanillaTrades = lootConfig.getConfig().getBoolean("wandering-trader.custom-trades.remove-vanilla-trades");
		
		if(noWanderingTrader) {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority=EventPriority.LOWEST)
				public void onEntitySpawn(EntitySpawnEvent event)
				{
					if(event.getEntityType()== EntityType.WANDERING_TRADER || event.getEntityType()== EntityType.TRADER_LLAMA) {
						event.setCancelled(true);
					}
				}
			}, fmc);
		}
		else if(enableCustomTrades){
			customTrades = new HashMap<>();

			ConfigurationSection sec = lootConfig.getConfig().getConfigurationSection("wandering-trader.custom-trades.trades");
			if(sec == null) {
				fmc.getLogger().log(Level.WARNING, "[Loot] No custom trades supplied!");
			}
			else {
				Set<String> trades = sec.getKeys(false);
				for(String trade : trades) {
					int groupId = lootConfig.getConfig().getInt("wandering-trader.custom-trades.trades." + trade + ".group-id");
					int chance = lootConfig.getConfig().getInt("wandering-trader.custom-trades.trades." + trade + ".propability-in-group");

					String firstIngName = lootConfig.getConfig().getString("wandering-trader.custom-trades.trades." + trade + ".first-ingredient.name");
					int firstIngCount = lootConfig.getConfig().getInt("wandering-trader.custom-trades.trades." + trade + ".first-ingredient.count");

					String secondIngName = lootConfig.getConfig().getString("wandering-trader.custom-trades.trades." + trade + ".second-ingredient.name");
					int secondIngCount = lootConfig.getConfig().getInt("wandering-trader.custom-trades.trades." + trade + ".second-ingredient.count");

					String outputName = lootConfig.getConfig().getString("wandering-trader.custom-trades.trades." + trade + ".output.name");
					int outputCount = lootConfig.getConfig().getInt("wandering-trader.custom-trades.trades." + trade + ".output.count");

					int maxUses = lootConfig.getConfig().getInt("wandering-trader.custom-trades.trades." + trade + ".max-uses");
					boolean dropXp = lootConfig.getConfig().getBoolean("wandering-trader.custom-trades.trades." + trade + ".drop-xp");

					if(!checkCustomTrade(groupId, chance, firstIngName, firstIngCount, secondIngName, secondIngCount, outputName, outputCount, maxUses)) {
						fmc.getLogger().log(Level.WARNING, "[Loot] Invalid Wandering Trader trade: ''{0}''", trade);
						continue;
					}

					MerchantRecipe trd = new MerchantRecipe(new ItemStack(Material.matchMaterial(outputName), outputCount), 0, maxUses, dropXp);
					trd.addIngredient(new ItemStack(Material.matchMaterial(firstIngName), firstIngCount));
					if(secondIngName != null) {
						trd.addIngredient(new ItemStack(Material.matchMaterial(secondIngName), secondIngCount));
					}

					WanderingTraderTrade wtt = new WanderingTraderTrade(groupId, chance, trd);

					if(customTrades.containsKey(groupId)) {
						customTrades.get(groupId).getTrades().add(wtt);
					}
					else {
						customTrades.put(groupId, new WanderingTraderGroup(groupId, new ArrayList<>(Arrays.asList(wtt))));
					}
				}
			}

			int wrongGroup;
			
			if((wrongGroup = checkPropabilitiesInGroups()) == -1) {
				fmc.getServer().getPluginManager().registerEvents(new Listener() {
					@EventHandler
					public void onEntitySpawn(EntitySpawnEvent event)
					{
						if(event.getEntityType()== EntityType.WANDERING_TRADER) {
							WanderingTrader wt = (WanderingTrader) event.getEntity();

							List<MerchantRecipe> trades = new ArrayList<>();

							if(!removeVanillaTrades) {
								trades.addAll(wt.getRecipes());
							}
							
							// adding custom trades
							for(Entry<Integer, WanderingTraderGroup> tradeGroup : customTrades.entrySet()) {
								if(tradeGroup.getValue().getGroupId() == 0) {
									// default group, adding all trades in it
									trades.addAll(tradeGroup.getValue().getTrades().stream().map(val -> val.getTrade()).collect(Collectors.toList()));
									continue;
								}
								
								MerchantRecipe chosenOne = null;
;								
								int cutoff = (int) Math.round(Math.random() * 100);
								int cumult = 0;
								for(WanderingTraderTrade td : tradeGroup.getValue().getTrades()) {
									cumult += td.getPropability();
									
									if(cutoff <= cumult) {
										chosenOne = td.getTrade();
										break;
									}
								}
								
								trades.add(chosenOne);
							}

							wt.setRecipes(trades);
						}
					}
				}, fmc);
			}
			else {
				fmc.getLogger().log(Level.WARNING, "[Loot] Wrong propabilities in group {0}!", wrongGroup);
				fmc.getLogger().log(Level.WARNING, "[Loot] Disabled custom trades, please fix and reload/restart your server");
			}
		}
	}
	
	private int checkPropabilitiesInGroups()
	{
		for(Entry<Integer, WanderingTraderGroup> tradeGroup : customTrades.entrySet()) {
			if(tradeGroup.getKey() == 0) {
				continue;
			}
			
			int curPropability = 0;
			
			curPropability = tradeGroup.getValue().getTrades().stream().map((trade) -> trade.getPropability()).reduce(curPropability, Integer::sum);
			
			if(curPropability != 100) {
				return tradeGroup.getKey();
			}
		}
		
		return -1;
	}
	
	private boolean checkCustomTrade(int groupId, int propability, String firstIngName, int firstIngCount, String secondIngName, int secondIngCount, String outputName, int outputCount, int maxUses)
	{
		if(groupId == 0) {
			// zero is default group, no need to check propability
		}
		else if(groupId < 1 || propability < 1 || propability > 99) {
			return false;
		}
		
		if(firstIngName == null || Material.matchMaterial(firstIngName) == null || !Material.matchMaterial(firstIngName).isItem() || firstIngCount < 1) {
			return false;
		}
		
		if(secondIngName != null) {
			if(Material.matchMaterial(secondIngName) == null || !Material.matchMaterial(secondIngName).isItem() || secondIngCount < 1) {
				return false;
			}
		}
		
		if(outputName == null || Material.matchMaterial(outputName) == null || !Material.matchMaterial(outputName).isItem() || outputCount < 1) {
			return false;
		}
		
		if(maxUses < 1) {
			return false;
		}
		
		return true;
	}
}