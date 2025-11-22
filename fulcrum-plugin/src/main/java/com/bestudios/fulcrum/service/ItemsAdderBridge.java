package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.service.customitem.CustomItemsService;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Bridge class for integrating with the ItemsAdder plugin.
 * <p>
 * This class implements the CustomItemsService interface to provide
 * methods for interacting with custom items and blocks created by ItemsAdder.
 * </p>
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see CustomItemsService
 * @see CustomStack
 * @see CustomBlock
 */
public class ItemsAdderBridge implements CustomItemsService {

  /** The plugin instance */
  private final Plugin plugin;
  /** The service priority for this integration */
  private final ServicePriority priority;
  /** Whether the integration is enabled */
  private boolean enabled;
  /** The name of the plugin */
  private final String pluginName = "ItemsAdder";
  /** The version of the plugin */
  private final String pluginVersion;

  /**
   * Constructor for the ItemsAdderBridge.
   *
   * @param plugin   The plugin instance
   * @param priority The service priority for this integration
   */
  public ItemsAdderBridge(Plugin plugin, ServicePriority priority) {
    this.plugin = plugin;
    this.priority = priority;
    this.pluginVersion = plugin.getServer().getPluginManager().getPlugin("ItemsAdder").getDescription().getVersion();
    this.enabled = false;
    // Register event listener for ItemsAdder load event
    plugin.getServer().getPluginManager().registerEvents(new Listener() {
      @EventHandler
      public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
        enabled = true;
        // Placeholder for event handling logic
      }
    }, plugin);
  }

  /**
   * Determines if the provided ItemStack is a custom item created with ItemsAdder.
   *
   * @param itemStack The Bukkit ItemStack to check
   * @return true if it is a custom item, false otherwise
   */
  @Override
  public boolean isCustomItem(@NotNull final ItemStack itemStack) {
    return CustomStack.byItemStack(itemStack) != null;
  }

  /**
   * Determines if the provided ItemStack is a custom item created with ItemsAdder.
   *
   * @param itemName The Custom ItemStack name to check
   * @return true if it is a custom item, false otherwise
   */
  @Override
  public boolean isCustomItem(@NotNull final String itemName) {
    return CustomStack.isInRegistry(itemName);
  }

  /**
   * Determines if the provided Block is a custom block created with ItemsAdder.
   *
   * @param block The Bukkit Block to check
   * @return true if it is a custom block, false otherwise
   */
  @Override
  public boolean isCustomBlock(@NotNull final Block block) {
    return CustomBlock.byAlreadyPlaced(block) != null;
  }

  /**
   * Determines if the provided Block is a custom block in the ItemsAdder's registry.
   *
   * @param blockName the name of the custom block to check
   * @return true if it is a custom block, false otherwise
   */
  @Override
  public boolean isCustomBlock(@NotNull final String blockName) {
    return CustomBlock.isInRegistry(blockName);
  }

  /**
   * Gets the namespace ID of the given ItemStack in the format {@code namespace:id}
   * either from ItemsAdder custom items or from vanilla Minecraft items.
   *
   * @param item The ItemStack to get the name for
   * @return String representing namespace and ID of the item
   */
  @Override @NotNull
  public String getItemNamespaceID(@NotNull final ItemStack item) {
    CustomStack customItem = CustomStack.byItemStack(item);
    if (customItem != null) return customItem.getNamespacedID();
    else return item.getType().getKey().toString();
  }

  /**
   * Gets the namespace ID of the given Block in the format {@code namespace:id}
   * either from ItemsAdder custom blocks or from vanilla Minecraft blocks.
   *
   * @param block The block to get the name for
   * @return String representing namespace and ID of the block
   */
  @Override @NotNull
  public String getBlockNamespaceID(@NotNull final Block block) {
    CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
    if (customBlock != null) return customBlock.getNamespacedID();
    return block.getType().getKey().toString();
  }

  /**
   * Gets the ItemStack associated with a custom item, if it exists.
   *
   * @param itemNamespaceID The ItemStack to check and potentially convert
   * @return CustomStack if it is a custom item, Bukkit ItemStack otherwise
   */
  @Override @Nullable
  public ItemStack getItemIfCustom(@NotNull final String itemNamespaceID) {
    CustomStack stack = CustomStack.getInstance(itemNamespaceID);
    if (stack != null) return stack.getItemStack();
    return null;
  }

  /**
   * Gets the ItemStack associated with a custom block.
   *
   * @param block The custom block to get the ItemStack for
   * @return The ItemStack associated with the custom block
   */
  @Override @Nullable
  public ItemStack getItemIfCustomBlock(@NotNull final Block block) {
    return CustomBlock.byAlreadyPlaced(block).getItemStack();
  }

  /**
   * Gets the ItemStack associated with a custom block by its namespace ID.
   *
   * @param blockNamespaceID The namespace ID of the custom block
   * @return The ItemStack associated with the custom block, or null if not found
   */
  @Override @Nullable
  public ItemStack getItemIfCustomBlock(@NotNull final String blockNamespaceID) {
    CustomBlock customBlock = CustomBlock.getInstance(blockNamespaceID);
    if (customBlock != null) return customBlock.getItemStack();
    return null;
  }

  /**
   * Performs a custom action on a CustomStack item.
   *
   * @param itemStack The name of the custom item
   * @param action    The action to perform on the CustomStack
   */
  @Override
  public void customItemAction(@NotNull final ItemStack itemStack, @NotNull Consumer<ItemStack> action) {
    CustomStack customItem = CustomStack.byItemStack(itemStack);
    if (customItem == null) return;
    action.accept(customItem.getItemStack());
  }

  /**
   * Places a custom block at the specified location.
   *
   * @param customBlockString the name of the custom block to place
   * @param location          the world location where the block should be placed
   */
  @Override
  public boolean placeCustomBlock(@NotNull final String customBlockString, Location location) {
    CustomBlock customBlock = CustomBlock.getInstance(customBlockString);
    if (customBlock == null) return false;
    customBlock.place(location);
    return true;
  }

  /**
   * Places a custom block at the specified location.
   *
   * @param itemStack the ItemStack representing the custom block to place
   * @param location  the world location where the block should be placed
   */
  @Override
  public boolean placeCustomBlock(@NotNull final ItemStack itemStack, Location location) {
    CustomBlock customBlock = CustomBlock.byItemStack(itemStack);
    if (customBlock == null) return false;
    customBlock.place(location);
    return true;
  }

  /**
   * Places a custom block at the specified location.
   *
   * @param block    the custom block to place
   * @param location the world location where the block should be placed
   */
  @Override
  public boolean placeCustomBlock(@NotNull final Block block, @NotNull Location location) {
    CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
    if (customBlock == null) return false;
    customBlock.place(location);
    return true;
  }

  @Override
  public ServicePriority getPriority() {
    return this.priority;
  }

  @Override
  public String getPluginName() {
    return "ItemsAdder";
  }

  @Override
  public boolean isAvailable() {
    return this.enabled;
  }

  @Override
  public String getPluginVersion() {
    return this.pluginVersion;
  }
}
