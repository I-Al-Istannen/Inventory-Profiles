package me.ialistannen.inventory_profiles.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

/**
 * The money hook with VAULT
 */
public class VaultMoneyHook implements MoneyHook {

	private Economy economy;
	private String errorMessage;
	
	/**
	 * Creates a new instance
	 */
	public VaultMoneyHook() {
		Vault plugin = (Vault) Bukkit.getPluginManager().getPlugin("Vault");
		if(plugin == null) {
			errorMessage = "Vault not found!";
			return;
		}
		
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		
		if(rsp == null) {
			errorMessage += " && No economy service provider found!";
			return;
		}
		
		economy = rsp.getProvider();
		
		if(economy == null) {
			errorMessage += "  && Economy is null";
		}
	}
	
	@Override
	public void setMoney(Player player, double amount) {
		removeMoney(player, getBalance(player));
		addMoney(player, amount);
	}
	
	@Override
	public void addMoney(Player player, double amount) {
		economy.depositPlayer(player, amount);
	}
	
	@Override
	public void removeMoney(Player player, double amount) {
		economy.withdrawPlayer(player, amount);
	}
	
	@Override
	public double getBalance(Player player) {
		return economy.getBalance(player);
	}

	@Override
	public boolean isWorking() {
		return economy != null;
	}

	@Override
	public String getName() {
		return economy.getName();
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
