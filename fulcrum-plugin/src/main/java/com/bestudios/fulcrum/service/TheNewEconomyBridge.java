package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.service.economy.EconomyPermission;
import com.bestudios.fulcrum.api.service.economy.EconomyService;
import com.bestudios.fulcrum.api.service.economy.PermissionConverter;
import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Bridge class to expose TheNewEconomy via VaultUnlocked API and Fulcrum Service API.
 * <p></p>
 * Note: This class depends on VaultUnlocked being present and a TNE provider being registered with it.
 * This behavior is planned to change in future releases of Fulcrum.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see EconomyService
 * @see Economy
 */
public class TheNewEconomyBridge implements EconomyService, Economy {

  /** The underlying VaultUnlocked Economy provider for TNE. */
  private final Economy vaultEconomy;
  /** The plugin instance registering this bridge. */
  private final Plugin plugin;
  /** The service priority for this bridge. */
  private final ServicePriority priority;

  /**
   * Constructs a new TheNewEconomyBridge.
   *
   * @param plugin  The plugin instance registering this bridge.
   * @param priority The service priority for this bridge.
   *
   * @throws IllegalStateException if no VaultUnlocked Economy provider is found.
   */
  public TheNewEconomyBridge(Plugin plugin, ServicePriority priority) {
    this.plugin = plugin;
    this.priority = priority;
    RegisteredServiceProvider<Economy> rsp =
            Bukkit.getServicesManager().getRegistration(Economy.class);

    if (rsp == null)
      throw new IllegalStateException("No VaultUnlocked Economy provider found");

    this.vaultEconomy = rsp.getProvider();
  }

  /*
   * Service interface methods
   */

  @Override
  public ServicePriority getPriority() {
    return this.priority;
  }

  @Override
  public String getPluginName() {
    return "TheNewEconomy";
  }

  @Override
  public boolean isAvailable() {
    return Bukkit.getPluginManager().isPluginEnabled("TNE") &&
           this.vaultEconomy != null;
  }

  @Override
  public String getPluginVersion() {
    Plugin tne = Bukkit.getPluginManager().getPlugin("TNE");
    return tne != null ? tne.getDescription().getVersion() : "unknown";
  }

  /*
   * Economy interface methods
   */
  @Override
  public boolean isEnabled() {
    return this.vaultEconomy.isEnabled();
  }

  @Override
  public @NotNull String getName() {
    return this.vaultEconomy.getName();
  }

  @Override
  public boolean hasSharedAccountSupport() {
    return this.vaultEconomy.hasSharedAccountSupport();
  }

  @Override
  public boolean hasMultiCurrencySupport() {
    return this.vaultEconomy.hasMultiCurrencySupport();
  }

  @Override
  public int fractionalDigits(@NotNull String pluginName) {
    return this.vaultEconomy.fractionalDigits(pluginName);
  }

  @Override
  public int fractionalDigits(@NotNull String pluginName, @NotNull String currency) {
    return this.vaultEconomy.fractionalDigits(pluginName, currency);
  }
  
  @Override
  public @NotNull String format(@NotNull BigDecimal amount) {
    return this.vaultEconomy.format(amount);
  }

  @Override
  public @NotNull String format(@NotNull String pluginName, @NotNull BigDecimal amount) {
    return this.vaultEconomy.format(pluginName, amount);
  }

  @Override
  public @NotNull String format(@NotNull String pluginName, @NotNull BigDecimal amount, @NotNull String currency) {
    return this.vaultEconomy.format(pluginName, amount, currency);
  }

  @Override
  public @NotNull String getDefaultCurrency(@NotNull String pluginName) {
    return this.vaultEconomy.getDefaultCurrency(pluginName);
  }

  @Override
  public @NotNull String defaultCurrencyNamePlural(@NotNull String pluginName) {
    return this.vaultEconomy.defaultCurrencyNamePlural(pluginName);
  }

  @Override
  public @NotNull String defaultCurrencyNameSingular(@NotNull String pluginName) {
    return this.vaultEconomy.defaultCurrencyNameSingular(pluginName);
  }

  @Override
  public boolean createAccount(@NotNull UUID accountID, @NotNull String name) {
    return this.vaultEconomy.createAccount(accountID, name);
  }

  @Override
  public boolean createAccount(@NotNull UUID accountID, @NotNull String name, @NotNull String worldName) {
    return this.vaultEconomy.createAccount(accountID, name, worldName);
  }

  @Override
  public boolean createAccount(@NotNull UUID accountID, @NotNull String name, @NotNull String worldName, boolean player) {
    return this.vaultEconomy.createAccount(accountID, name, worldName, player);
  }

  @Override
  public boolean hasAccount(@NotNull UUID accountID, @NotNull String worldName) {
    return this.vaultEconomy.hasAccount(accountID, worldName);
  }

  @Override
  public boolean renameAccount(@NotNull String plugin, @NotNull UUID accountID, @NotNull String name) {
    return this.vaultEconomy.renameAccount(plugin, accountID, name);
  }

  @Override
  public boolean deleteAccount(@NotNull String plugin, @NotNull UUID accountID) {
    return this.vaultEconomy.deleteAccount(plugin, accountID);
  }

  @Override
  public boolean accountSupportsCurrency(@NotNull String plugin, @NotNull UUID accountID, @NotNull String currency) {
    return this.vaultEconomy.accountSupportsCurrency(plugin, accountID, currency);
  }

  @Override
  public boolean accountSupportsCurrency(@NotNull String plugin, @NotNull UUID accountID, @NotNull String currency, @NotNull String world) {
    return this.vaultEconomy.accountSupportsCurrency(plugin, accountID, currency, world);
  }

  @Override
  public @NotNull BigDecimal getBalance(@NotNull String pluginName, @NotNull UUID accountID) {
    return this.vaultEconomy.balance(pluginName, accountID);
  }

  @Override
  public @NotNull BigDecimal getBalance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world) {
    return this.vaultEconomy.balance(pluginName, accountID, world);
  }

  @Override
  public @NotNull BigDecimal getBalance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency) {
    return this.vaultEconomy.balance(pluginName, accountID, currency, world);
  }

  @Override
  public @NotNull BigDecimal balance(@NotNull String pluginName, @NotNull UUID accountID) {
    return this.vaultEconomy.balance(pluginName, accountID);
  }

  @Override
  public @NotNull BigDecimal balance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world) {
    return this.vaultEconomy.balance(pluginName, accountID, world);
  }

  @Override
  public @NotNull BigDecimal balance(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String world, @NotNull String currency) {
    return this.vaultEconomy.balance(pluginName, accountID, world, currency);
  }

  @Override
  public boolean has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount) {
    return this.vaultEconomy.has(pluginName, accountID, amount);
  }

  @Override
  public boolean has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull BigDecimal amount) {
    return this.vaultEconomy.has(pluginName, accountID, worldName, amount);
  }

  @Override
  public boolean has(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull String currency, @NotNull BigDecimal amount) {
    return this.vaultEconomy.has(pluginName, accountID, worldName, currency, amount);
  }

  @Override
  public EconomyResponse set(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount) {
    return this.vaultEconomy.set(pluginName, accountID, amount);
  }

  @Override
  public EconomyResponse set(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull BigDecimal amount) {
    return this.vaultEconomy.set(pluginName, accountID, worldName, amount);
  }

  @Override
  public EconomyResponse set(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull String currency, @NotNull BigDecimal amount) {
    return this.vaultEconomy.set(pluginName, accountID, worldName, currency, amount);
  }

  @Override
  public @NotNull EconomyResponse withdraw(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount) {
    return this.vaultEconomy.withdraw(pluginName, accountID, amount);
  }

  @Override
  public @NotNull EconomyResponse withdraw(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull BigDecimal amount) {
    return this.vaultEconomy.withdraw(pluginName, accountID, worldName, amount);
  }

  @Override
  public @NotNull EconomyResponse withdraw(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull String currency, @NotNull BigDecimal amount) {
    return this.vaultEconomy.withdraw(pluginName, accountID, worldName, currency, amount);
  }

  @Override
  public @NotNull EconomyResponse deposit(@NotNull String pluginName, @NotNull UUID accountID, @NotNull BigDecimal amount) {
    return this.vaultEconomy.deposit(pluginName, accountID, amount);
  }

  @Override
  public @NotNull EconomyResponse deposit(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull BigDecimal amount) {
    return this.vaultEconomy.deposit(pluginName, accountID, worldName, amount);
  }

  @Override
  public @NotNull EconomyResponse deposit(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String worldName, @NotNull String currency, @NotNull BigDecimal amount) {
    return this.vaultEconomy.deposit(pluginName, accountID, worldName, currency, amount);
  }

  @Override
  public boolean createSharedAccount(@NotNull String pluginName, @NotNull UUID accountID, @NotNull String name, @NotNull UUID owner) {
    return this.vaultEconomy.createSharedAccount(pluginName, accountID, name, owner);
  }

  @Override
  public List<String> accountsOwnedBy(@NotNull String pluginName, @NotNull UUID accountID) {
    return this.vaultEconomy.accountsOwnedBy(pluginName, accountID);
  }

  @Override
  public List<String> accountsMemberOf(@NotNull String pluginName, @NotNull UUID accountID) {
    return this.vaultEconomy.accountsMemberOf(pluginName, accountID);
  }

  @Override
  public List<String> accountsAccessTo(@NotNull String pluginName, @NotNull UUID accountID, @NotNull AccountPermission... permissions) {
    return this.vaultEconomy.accountsAccessTo(pluginName, accountID, permissions);
  }

  @Override
  public boolean isAccountOwner(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.isAccountOwner(pluginName, accountID, uuid);
  }

  @Override
  public boolean setOwner(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.setOwner(pluginName, accountID, uuid);
  }

  @Override
  public boolean isAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.isAccountOwner(pluginName, accountID, uuid);
  }

  @Override
  public boolean addAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.isAccountOwner(pluginName, accountID, uuid);
  }

  @Override
  public boolean addAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid, @NotNull AccountPermission... initialPermissions) {
    return this.vaultEconomy.isAccountMember(pluginName, accountID, uuid);
  }

  @Override
  public boolean removeAccountMember(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.isAccountOwner(pluginName, accountID, uuid);
  }

  @Override
  public boolean hasAccountPermission(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid, @NotNull AccountPermission permission) {
    return this.vaultEconomy.hasAccountPermission(pluginName, accountID, uuid, permission);
  }

  @Override
  public boolean updateAccountPermission(@NotNull String pluginName, @NotNull UUID accountID, @NotNull UUID uuid, @NotNull AccountPermission permission, boolean value) {
    return this.vaultEconomy.updateAccountPermission(pluginName, accountID, uuid, permission, value);
  }

  /*
   * EconomyService interface methods
   */

  @Override
  public int digitsPrecision() {
    return this.vaultEconomy.fractionalDigits(this.getPluginName(), this.getDefaultCurrency());
  }

  @Override
  public @NotNull String format(@NotNull BigDecimal amount, @NotNull String currency) {
    return this.vaultEconomy.format(getPluginName(), amount, currency);
  }

  @Override
  public boolean hasCurrency(@NotNull String currency) {
    return this.vaultEconomy.currencies().contains(currency);
  }

  @Override
  public @NotNull String getDefaultCurrency() {
    return this.vaultEconomy.getDefaultCurrency(this.getPluginName());
  }

  @Override
  public @NotNull String defaultCurrencyNameSingular() {
    return this.vaultEconomy.defaultCurrencyNameSingular(this.getPluginName());
  }

  @Override
  public @NotNull String defaultCurrencyNamePlural() {
    return this.vaultEconomy.defaultCurrencyNamePlural(this.getPluginName());
  }

  @Override
  public String currencyNameSingular(@NotNull String currency) {
    return currency;
  }

  @Override
  public String currencyNamePlural(@NotNull String currency) {
    return currency;
  }

  @Override
  public @NotNull Collection<String> currencies() {
    return this.vaultEconomy.currencies();
  }

  @Override
  public boolean createAccount(@NotNull UUID accountID, @NotNull String name, boolean playerAccount) {
    return this.vaultEconomy.createAccount(accountID, name, playerAccount);
  }

  @Override
  public @NotNull Map<UUID, String> getUUIDNameMap() {
    return Map.of();
  }

  @Override
  public @NotNull Optional<String> getAccountName(@NotNull UUID accountID) {
    return this.vaultEconomy.getAccountName(accountID);
  }

  @Override
  public boolean hasAccount(@NotNull UUID accountID) {
    return this.vaultEconomy.hasAccount(accountID);
  }

  @Override
  public boolean renameAccount(@NotNull UUID accountID, @NotNull String name) {
    return this.vaultEconomy.renameAccount(this.getPluginName(), accountID, name);
  }

  @Override
  public boolean deleteAccount(@NotNull UUID accountID) {
    return this.vaultEconomy.deleteAccount(this.getPluginName(), accountID);
  }

  @Override
  public boolean accountSupportsCurrency(@NotNull UUID accountID, @NotNull String currency) {
    return this.vaultEconomy.accountSupportsCurrency(this.getPluginName(), accountID, currency);
  }

  @Override
  public @NotNull BigDecimal balance(@NotNull UUID accountID) {
    return this.vaultEconomy.balance(this.getPluginName(), accountID);
  }

  @Override
  public @NotNull BigDecimal balance(@NotNull UUID accountID, @NotNull String currency) {
    return this.vaultEconomy.balance(this.getPluginName(), accountID, currency);
  }

  @Override
  public boolean has(@NotNull UUID accountID, @NotNull BigDecimal amount) {
    return this.vaultEconomy.has(this.getPluginName(), accountID, amount);
  }

  @Override
  public boolean has(@NotNull UUID accountID, @NotNull String currency, @NotNull BigDecimal amount) {
    return this.vaultEconomy.has(this.getPluginName(), accountID, currency, amount);
  }

  @Override
  public com.bestudios.fulcrum.api.service.economy.EconomyResponse withdraw(@NotNull UUID accountID, @NotNull BigDecimal amount, @NotNull String currency) {
    return new com.bestudios.fulcrum.api.service.economy.EconomyResponse(this.vaultEconomy.withdraw(this.getPluginName(), accountID, currency, amount));
  }

  @Override
  public com.bestudios.fulcrum.api.service.economy.EconomyResponse deposit(@NotNull UUID accountID, @NotNull BigDecimal amount, @NotNull String currency) {
    return new com.bestudios.fulcrum.api.service.economy.EconomyResponse(this.vaultEconomy.deposit(this.getPluginName(), accountID, currency, amount));
  }

  @Override
  public boolean createSharedAccount(@NotNull UUID accountID, @NotNull String name, @NotNull UUID owner) {
    return this.vaultEconomy.createSharedAccount(this.getPluginName(), accountID, name, owner);
  }

  @Override
  public boolean isAccountOwner(@NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.isAccountOwner(this.getPluginName(), accountID, uuid);
  }

  @Override
  public boolean setOwner(@NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.setOwner(this.getPluginName(), accountID, uuid);
  }

  @Override
  public boolean isAccountMember(@NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.isAccountMember(this.getPluginName(), accountID, uuid);
  }

  @Override
  public boolean addAccountMember(@NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.addAccountMember(this.getPluginName(), accountID, uuid);
  }

  @Override
  public boolean addAccountMember(@NotNull UUID accountID, @NotNull UUID uuid, @NotNull EconomyPermission... initialPermissions) {
    AccountPermission[] perms = new AccountPermission[initialPermissions.length];
    for (int i = 0; i < initialPermissions.length; i++)
      perms[i] = PermissionConverter.convert(initialPermissions[i]);
    return this.vaultEconomy.addAccountMember(this.getPluginName(),accountID, uuid, perms);
  }

  @Override
  public boolean removeAccountMember(@NotNull UUID accountID, @NotNull UUID uuid) {
    return this.vaultEconomy.removeAccountMember(this.getPluginName(), accountID, uuid);
  }

  @Override
  public boolean hasAccountPermission(@NotNull UUID accountID, @NotNull UUID uuid, @NotNull EconomyPermission permission) {
    return this.vaultEconomy.hasAccountPermission(getPluginName(), accountID, uuid, PermissionConverter.convert(permission));
  }

  @Override
  public boolean updateAccountPermission(@NotNull UUID accountID, @NotNull UUID uuid, @NotNull EconomyPermission permission, boolean value) {
    return this.vaultEconomy.updateAccountPermission(getPluginName(), accountID, uuid, PermissionConverter.convert(permission), value);
  }

}

