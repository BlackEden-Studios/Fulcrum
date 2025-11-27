package com.bestudios.fulcrum.api.service.economy;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a response from an economy transaction.
 */
public class EconomyResponse {

  /** The amount that was updated */
  public final BigDecimal amount;
  /** The balance after the transaction */
  public final BigDecimal balance;
  /** Whether the transaction was successful */
  public final boolean result;
  /** The error message, if any */
  public final String error;

  /**
   * Creates a new economy response.
   * @param update             The amount that was updated.
   * @param newBalance         The balance after the transaction.
   * @param transactionSuccess The result of the transaction.
   * @param errorMessage       The error message.
   */
  @Contract(pure = true)
  public EconomyResponse(
          @NotNull BigDecimal update,
          @NotNull BigDecimal newBalance,
          boolean transactionSuccess,
          @NotNull String errorMessage
  ) {
    this.amount = Objects.requireNonNull(update, "Update cannot be null");
    this.balance = Objects.requireNonNull(newBalance, "New balance cannot be null");
    this.result = transactionSuccess;
    this.error = Objects.requireNonNull(errorMessage, "Error message cannot be null");
  }

  /**
   * Creates a new economy response from a Vault API response.
   * @param response The Vault API response.
   */
  @Contract(pure = true)
  public EconomyResponse(@NotNull net.milkbowl.vault2.economy.EconomyResponse response) {
    this.amount = new BigDecimal(String.valueOf(response.amount));
    this.balance = new BigDecimal(String.valueOf(response.balance));
    this.result = response.transactionSuccess();
    this.error = response.errorMessage;
  }

  /**
   * Checks if the transaction was successful.
   * @return true if the transaction was successful, false otherwise.
   */
  public boolean transactionSuccess() {
    return result;
  }
}

