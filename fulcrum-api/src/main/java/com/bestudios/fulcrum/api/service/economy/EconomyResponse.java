package com.bestudios.fulcrum.api.service.economy;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class EconomyResponse {
  public final BigDecimal amount;
  public final BigDecimal balance;
  public final boolean success;
  public final String errorMessage;

  public EconomyResponse(BigDecimal amount, BigDecimal balance,
                         boolean success, String errorMessage) {
    this.amount = amount;
    this.balance = balance;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  public EconomyResponse(@NotNull net.milkbowl.vault2.economy.EconomyResponse response) {
    this.amount = new BigDecimal(String.valueOf(response.amount));
    this.balance = new BigDecimal(String.valueOf(response.balance));
    this.success = response.transactionSuccess();
    this.errorMessage = response.errorMessage;
  }

  public boolean transactionSuccess() {
    return success;
  }
}

