package com.bestudios.fulcrum.api.service.economy;

import com.bestudios.fulcrum.api.service.Service;
import org.jetbrains.annotations.NotNull;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * EconomyService interface for managing economy-related operations.
 * Provides methods for currency information, account management,
 * balance inquiries, transactions, and shared account handling.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Service
 * @see EconomyResponse
 * @see EconomyPermission
 */
public interface EconomyService extends Service {

  /*
   * Currency Information Methods
   */

  /**
   * Returns the number of fractional digits used in the currency.
   *
   * @return The number of fractional digits.
   */
  int digitsPrecision();

  /**
   * Returns the number of fractional digits for a specific currency.
   *
   * @param currency   The currency to get the precision for.
   *
   * @return The number of fractional digits.
   */
  default int digitsPrecision(@NotNull String currency) {
    return digitsPrecision();
  }

  /**
   * Formats an amount into a human-readable string with the default currency.
   *
   * @param amount     The amount to format.
   *
   * @return The formatted string.
   */
  @NotNull
  default String format(@NotNull BigDecimal amount) {
    return format(amount, getDefaultCurrency());
  }

  /**
   * Formats an amount with a specific currency into a human-readable string.
   *
   * @param amount     The amount to format.
   * @param currency   The currency to use for formatting.
   *
   * @return The formatted string.
   */
  @NotNull
  String format(@NotNull BigDecimal amount, @NotNull String currency);

  /**
   * Checks if a currency with the specified name exists.
   *
   * @param currency The currency name to check.
   *
   * @return true if the currency exists, false otherwise.
   */
  boolean hasCurrency(@NotNull String currency);

  /**
   * Gets the default currency name.
   *
   * @return The default currency name.
   */
  @NotNull
  String getDefaultCurrency();

  /**
   * Returns the name of the default currency in singular form.
   *
   * @return The singular name of the default currency.
   */
  @NotNull
  String defaultCurrencyNameSingular();

  /**
   * Returns the name of the default currency in plural form.
   *
   * @return The plural name of the default currency.
   */
  @NotNull
  String defaultCurrencyNamePlural();

  /**
   * Returns the name of a specific currency in singular form.
   *
   * @param currency The currency to get the name for.
   *
   * @return The singular name of the currency.
   */
  String currencyNameSingular(@NotNull String currency);

  /**
   * Returns the name of a specific currency in plural form.
   *
   * @param currency The currency to get the name for.
   *
   * @return The plural name of the currency.
   */
  String currencyNamePlural(@NotNull String currency);

  /**
   * Returns a list of currencies used by the economy plugin.
   *
   * @return A collection of registered currency names.
   */
  @NotNull
  Collection<String> currencies();

  /*
   * Account Management Methods
   */

  /**
   * Creates a new account with the given UUID, name, and type.
   * 
   * @param accountID The UUID of the account to create.
   * @param name The name associated with the account.
   * @param playerAccount true if the account is a player account, false for other types. 
   *                      
   * @return true if the account was created successfully, false otherwise.
   */
  boolean createAccount(@NotNull UUID accountID, @NotNull String name, boolean playerAccount);

  /**
   * Returns a map of all UUIDs with accounts and their last-known names.
   * 
   * @return A map of UUIDs to account names.
   */
  @NotNull
  Map<UUID, String> getUUIDNameMap();

  /**
   * Returns the name associated with the given UUID.
   * 
   * @param accountID The UUID of the account.
   *                  
   * @return The name associated with the account.
   */
  @NotNull
  Optional<String> getAccountName(@NotNull UUID accountID);

  /**
   * Checks if an account exists for the given UUID.
   * 
   * @param accountID The UUID of the account to check.
   *                  
   * @return true if the account exists, false otherwise.
   */
  boolean hasAccount(@NotNull UUID accountID);

  /**
   * Renames an account.
   * 
   * @param accountID The UUID of the account to rename.
   * @param name The new name for the account.
   *             
   * @return true if the account was renamed successfully, false otherwise.
   */
  boolean renameAccount(@NotNull UUID accountID, @NotNull String name);

  /**
   * Deletes an account.
   * 
   * @param accountID The UUID of the account to delete.
   *
   * @return true if the account was deleted successfully, false otherwise.
   */
  boolean deleteAccount(@NotNull UUID accountID);

  /*
   * Account Balance Methods
   */

  /**
   * Checks if an account supports a specific currency.
   *
   * @param accountID The UUID of the account to check.
   * @param currency The currency to check support for.
   *
   * @return true if the account supports the currency, false otherwise.
   */
  boolean accountSupportsCurrency(@NotNull UUID accountID, @NotNull String currency);

  /**
   * Gets the balance of an account with the default currency.
   *
   * @param accountID The UUID of the account.
   *
   * @return The balance of the account.
   */
  @NotNull
  BigDecimal balance(@NotNull UUID accountID);

  /**
   * Gets the balance of an account with a specific currency.
   *
   * @param accountID The UUID of the account.
   * @param currency The currency to get the balance in.
   *
   * @return The balance of the account in the specified currency.
   */
  @NotNull
  BigDecimal balance(@NotNull UUID accountID, @NotNull String currency);

  /**
   * Checks if an account has at least the specified amount.
   *
   * @param accountID The UUID of the account.
   * @param amount The amount to check.
   *
   * @return true if the account has at least the specified amount, false otherwise.
   */
  boolean has(@NotNull UUID accountID, @NotNull BigDecimal amount);

  /**
   * Checks if an account has at least the specified amount with a specific currency.
   *
   * @param accountID The UUID of the account.
   * @param currency The currency to check.
   * @param amount The amount to check.
   *
   * @return true if the account has at least the specified amount in the specified currency, false otherwise.
   */
  boolean has(@NotNull UUID accountID, @NotNull String currency, @NotNull BigDecimal amount);

  /*
   * Transaction Methods
   */

  /**
   * Withdraws an amount from an account with the default currency.
   *
   * @param accountID The UUID of the account to withdraw from.
   * @param amount The amount to withdraw.
   *
   * @return An EconomyResponse indicating the result of the transaction.
   */
  default EconomyResponse withdraw(@NotNull UUID accountID, @NotNull BigDecimal amount) {
    return withdraw(accountID, amount, getDefaultCurrency());
  }

  /**
   * Withdraws an amount with a specific currency from an account.
   *
   * @param accountID The UUID of the account to withdraw from.
   * @param amount The amount to withdraw.
   * @param currency The currency to withdraw.
   *
   * @return An EconomyResponse indicating the result of the transaction.
   */
  @NotNull
  EconomyResponse withdraw(@NotNull UUID accountID, @NotNull BigDecimal amount, @NotNull String currency);

  /**
   * Deposits an amount into an account with the default currency.
   *
   * @param accountID The UUID of the account to deposit into.
   * @param amount The amount to deposit.
   *
   * @return An EconomyResponse indicating the result of the transaction.
   */
  @NotNull
  default EconomyResponse deposit(@NotNull UUID accountID, @NotNull BigDecimal amount) {
    return deposit(accountID, amount, getDefaultCurrency());
  }

  /**
   * Deposits an amount into an account with a specific currency.
   *
   * @param accountID The UUID of the account to deposit into.
   * @param amount The amount to deposit.
   * @param currency The currency to deposit.
   *
   * @return An EconomyResponse indicating the result of the transaction.
   */
  @NotNull
  EconomyResponse deposit(@NotNull UUID accountID, @NotNull BigDecimal amount, @NotNull String currency);

  /**
   * Sets the balance of an account to a specific amount with the default currency.
   *
   * @param accountID The UUID of the account to set the balance for.
   * @param amount The amount to set the balance to.
   *               
   * @return An EconomyResponse indicating the result of the operation.
   */
  default EconomyResponse set(@NotNull UUID accountID, @NotNull BigDecimal amount) {
    return set(accountID, amount, getDefaultCurrency());
  }

  /**
   * Sets the balance of an account with a specific currency.
   *
   * @param accountID The UUID of the account to set the balance for.
   * @param amount The amount to set the balance to.
   * @param currency The currency to set the balance in.
   *
   * @return An EconomyResponse indicating the result of the operation.
   */
  default EconomyResponse set(@NotNull UUID accountID, @NotNull BigDecimal amount, @NotNull String currency) {
    BigDecimal balance = balance(accountID, currency);
    int compare = balance.compareTo(amount);
    if (compare > 0) return withdraw(accountID, balance.subtract(amount), currency);
    if (compare < 0) return deposit(accountID, amount.subtract(balance), currency);
    return new EconomyResponse(BigDecimal.ZERO, amount, true, "");
  }

  /*
   * Shared Account Methods
   */

  /**
   * Creates a shared account.
   *
   * @param accountID The UUID of the shared account to create.
   * @param name The name associated with the shared account.
   * @param owner The UUID of the owner of the shared account.
   *
   * @return true if the shared account was created successfully, false otherwise.
   */
  boolean createSharedAccount(@NotNull UUID accountID, @NotNull String name, @NotNull UUID owner);

  /**
   * Gets a list of accounts owned by the specified account.
   *
   * @param accountID The UUID of the account to check ownership for.
   *
   * @return A list of UUIDs representing accounts owned by the specified account.
   */
  default List<UUID> accountsWithOwnerOf(@NotNull UUID accountID) {
    return List.of();
  }

  /**
   * Gets a list of accounts the specified account is a member of.
   *
   * @param accountID The UUID of the account to check membership for.
   *
   * @return A list of UUIDs representing accounts the specified account is a member of.
   */
  default List<UUID> accountsWithMembershipTo(@NotNull UUID accountID) {
    return List.of();
  }

  /**
   * Gets a list of accounts the specified account has access to with specific permissions.
   *
   * @param accountID The UUID of the account to check access for.
   * @param permissions The permissions to check for access.
   *
   * @return A list of UUIDs representing accounts the specified account has access to with the given permissions.
   */
  default List<UUID> accountsWithAccessTo(@NotNull UUID accountID, EconomyPermission... permissions) {
    return List.of();
  }

  /**
   * Checks if the specified UUID is the owner of an account.
   *
   * @param accountID The UUID of the account to check ownership for.
   * @param uuid The UUID to check for ownership.
   *
   * @return true if the specified UUID is the owner of the account, false otherwise.
   */
  boolean isAccountOwner(@NotNull UUID accountID, @NotNull UUID uuid);

  /**
   * Sets the owner of an account.
   *
   * @param accountID The UUID of the account to set the owner for.
   * @param uuid The UUID of the new owner.
   *
   * @return true if the owner was set successfully, false otherwise.
   */
  boolean setOwner(@NotNull UUID accountID, @NotNull UUID uuid);

  /**
   * Checks if the specified UUID is a member of an account.
   *
   * @param accountID The UUID of the account to check membership for.
   * @param uuid The UUID to check for membership.
   *
   * @return true if the specified UUID is a member of the account, false otherwise.
   */
  boolean isAccountMember(@NotNull UUID accountID, @NotNull UUID uuid);

  /**
   * Adds a member to an account.
   *
   * @param accountID The UUID of the account to add a member to.
   * @param uuid The UUID of the member to add.
   *
   * @return true if the member was added successfully, false otherwise.
   */
  boolean addAccountMember(@NotNull UUID accountID, @NotNull UUID uuid);

  /**
   * Adds a member to an account with initial permissions.
   *
   * @param accountID The UUID of the account to add a member to.
   * @param uuid The UUID of the member to add.
   * @param initialPermissions The initial permissions to assign to the member.
   *
   * @return true if the member was added successfully, false otherwise.
   */
  boolean addAccountMember(@NotNull UUID accountID, @NotNull UUID uuid, @NotNull EconomyPermission... initialPermissions);

  /**
   * Removes a member from an account.
   *
   * @param accountID The UUID of the account to remove a member from.
   * @param uuid The UUID of the member to remove.
   *
   * @return true if the member was removed successfully, false otherwise.
   */
  boolean removeAccountMember(@NotNull UUID accountID, @NotNull UUID uuid);

  /**
   * Checks if an account has a specific permission.
   *
   * @param accountID The UUID of the account to check permissions for.
   * @param uuid The UUID of the member to check the permission for.
   * @param permission The permission to check.
   *
   * @return true if the account has the specified permission, false otherwise.
   */
  boolean hasAccountPermission(@NotNull UUID accountID, @NotNull UUID uuid, @NotNull EconomyPermission permission);

  /**
   * Updates an account permission.
   *
   * @param accountID The UUID of the account to update permissions for.
   * @param uuid The UUID of the member to update the permission for.
   * @param permission The permission to update.
   * @param value true to grant the permission, false to revoke it.
   *
   * @return true if the permission was updated successfully, false otherwise.
   */
  boolean updateAccountPermission(@NotNull UUID accountID, @NotNull UUID uuid, @NotNull EconomyPermission permission, boolean value);
}

