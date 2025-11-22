package com.bestudios.fulcrum.api.service.economy;

import net.milkbowl.vault2.economy.AccountPermission;
import org.jetbrains.annotations.NotNull;

public class PermissionConverter {

  public static EconomyPermission convert(@NotNull AccountPermission permission) {
    return switch (permission) {
      case DEPOSIT -> EconomyPermission.DEPOSIT;
      case WITHDRAW -> EconomyPermission.WITHDRAW;
      case BALANCE -> EconomyPermission.BALANCE;
      case TRANSFER_OWNERSHIP -> EconomyPermission.TRANSFER_OWNERSHIP;
      case INVITE_MEMBER -> EconomyPermission.INVITE_MEMBER;
      case REMOVE_MEMBER -> EconomyPermission.REMOVE_MEMBER;
      case CHANGE_MEMBER_PERMISSION -> EconomyPermission.CHANGE_MEMBER_PERMISSION;
      case OWNER -> EconomyPermission.OWNER;
      case DELETE -> EconomyPermission.DELETE;
    };
  }

  public  static AccountPermission convert(@NotNull EconomyPermission permission) {
    return switch (permission) {
      case DEPOSIT -> AccountPermission.DEPOSIT;
      case WITHDRAW -> AccountPermission.WITHDRAW;
      case BALANCE -> AccountPermission.BALANCE;
      case TRANSFER_OWNERSHIP -> AccountPermission.TRANSFER_OWNERSHIP;
      case INVITE_MEMBER -> AccountPermission.INVITE_MEMBER;
      case REMOVE_MEMBER -> AccountPermission.REMOVE_MEMBER;
      case CHANGE_MEMBER_PERMISSION -> AccountPermission.CHANGE_MEMBER_PERMISSION;
      case OWNER -> AccountPermission.OWNER;
      case DELETE -> AccountPermission.DELETE;
    };
  }
}
