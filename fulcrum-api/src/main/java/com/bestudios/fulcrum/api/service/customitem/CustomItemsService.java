package com.bestudios.fulcrum.api.service.customitem;

import com.bestudios.fulcrum.api.service.Service;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Service interface for interacting with custom items and blocks
 * <p></p>
 * This interface provides methods to check for custom items and blocks,
 * retrieve their associated ItemStacks, and perform actions on them.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Service
 * @see ItemStack
 * @see Block
 */
public interface CustomItemsService extends Service {

  /**
   * Determines if the provided ItemStack is a custom item.
   *
   * @param itemStack The Bukkit ItemStack to check
   *
   * @return true if it is a custom item, false otherwise
   */
  boolean isCustomItem(@NotNull final ItemStack itemStack);

  /**
   * Determines if the provided ItemStack is a custom item.
   *
   * @param itemNamespaceID The Custom ItemStack name to check
   *
   * @return true if it is a custom item, false otherwise
   */
  boolean isCustomItem(@NotNull final String itemNamespaceID);

  /**
   * Determines if the provided Block is a custom block.
   *
   * @param block The Bukkit Block to check
   *
   * @return true if it is a custom block, false otherwise
   */
  boolean isCustomBlock(@NotNull final Block block);

  /**
   * Determines if the provided Block is a custom block.
   *
   * @param blockNamespaceID the name of the custom block to check
   *
   * @return true if it is a custom block, false otherwise
   */
  boolean isCustomBlock(@NotNull final String blockNamespaceID);

  /**
   * Gets a regular Bukkit ItemStack and returns its namespace ID
   *
   * @param item The ItemStack to get the namespace ID for
   *
   * @return String representing namespace and ID
   */
  @NotNull
  default String getItemNamespaceID(@NotNull final ItemStack item) {
    return item.getType().getKey().toString();
  }

  /**
   * Gets the Namespace ID associated with a block.
   *
   * @param block The name of the block to get the Namespace ID for
   *
   * @return The Namespace ID of the block
   */
  @NotNull
  default String getBlockNamespaceID(@NotNull final Block block) {
    return block.getType().getKey().toString();
  }

  /**
   * Gets a regular Bukkit ItemStack and returns its namespaced ID
   *
   * @param item The ItemStack to get the name for
   *
   * @return String representing namespace and ID
   */
  @NotNull
  default String getItemName(@NotNull final ItemStack item) {
    String s = getItemNamespaceID(item);
    return s.substring(s.indexOf(':')+1);
  }

  /**
   * Gets a regular Bukkit ItemStack and returns its custom stack
   * otherwise returns the ItemStack provided as the argument.
   *
   * @param item The ItemStack to check and potentially convert
   *
   * @return stack object of custom item, Bukkit ItemStack otherwise
   */
  @Nullable
  default ItemStack getItemIfCustom(@NotNull final ItemStack item) {
    return getItemIfCustom(getItemNamespaceID(item));
  }

  /**
   * Gets a namespace ID and returns its custom stack
   * otherwise returns the ItemStack provided as the argument.
   *
   * @param itemNamespaceID The name of the custom item
   *
   * @return CustomStack of custom item, null otherwise
   */
  @Nullable
  default ItemStack getItemIfCustom(@NotNull final String itemNamespaceID) { return null; }

  /**
   * Gets the ItemStack associated with a custom block.
   *
   * @param block The custom block to get the ItemStack for
   *
   * @return The ItemStack associated with the custom block
   */
  @Nullable
  default ItemStack getItemIfCustomBlock(@NotNull final Block block) { return null; }

  /**
   * Gets the ItemStack associated with a custom block.
   *
   * @param blockNamespaceID The name of the custom block to get the ItemStack for
   *
   * @return The ItemStack associated with the custom block
   */
  @Nullable
  default ItemStack getItemIfCustomBlock(@NotNull final String blockNamespaceID) { return null; }

  /**
   * Performs a custom action on a custom item.
   *
   * @param itemNamespaceID The name of the custom item
   * @param action          The action to perform on the custom item
   */
  default void customItemAction(@NotNull final String itemNamespaceID, Consumer<ItemStack> action) {
    ItemStack item = getItemIfCustom(itemNamespaceID);
    if (item == null) return;
    customItemAction(item, action);
  }

  /**
   * Performs a custom action on a custom item.
   *
   * @param itemStack The ItemStack to check and potentially perform action on
   * @param action    The action to perform on the custom item
   */
  void customItemAction(@NotNull final ItemStack itemStack, Consumer<ItemStack> action);

  /**
   * Places a custom block at the specified location.
   *
   * @param customBlockNamespaceID The name of the custom block to place
   * @param location               The world location where the block should be placed
   */
  boolean placeCustomBlock(@NotNull final String customBlockNamespaceID, Location location);

  /**
   * Places a custom block at the specified location.
   *
   * @param customBlockItem The ItemStack of the custom block to place
   * @param location        The world location where the block should be placed
   */
  boolean placeCustomBlock(@NotNull final ItemStack customBlockItem, Location location);

  /**
   * Places a custom block at the specified location.
   *
   * @param block    The Block to be placed as a custom block
   * @param location The world location where the block should be placed
   */
  boolean placeCustomBlock(@NotNull final Block block, Location location);
}
