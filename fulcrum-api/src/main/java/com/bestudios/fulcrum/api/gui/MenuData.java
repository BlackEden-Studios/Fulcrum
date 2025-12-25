package com.bestudios.fulcrum.api.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds data for a specific menu.
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Menu
 */
public record MenuData(
        Object provider,
        Component title,
        Map<Integer, MenuElement> blueprints,
        AtomicBoolean ready // Thread-safe mutable flag
) {

  /**
   * Static Factory.
   * Initializes with ready = false.
   */
  public static @NotNull MenuData create(Object provider, Component title) {
    return new MenuData(provider, title, new ConcurrentHashMap<>(), new AtomicBoolean(false));
  }

  public void addBlueprint(MenuElement blueprint) {
    this.blueprints.put(blueprint.slot(), blueprint);
  }

  public void removeBlueprint(int slot) {
    this.blueprints.remove(slot);
  }

  /**
   * Marks the data as fully populated and ready for rendering.
   */
  public void markAsReady() {
    this.ready.set(true);
  }

  /**
   * Check if the data is ready to be viewed.
   */
  public boolean isReady() {
    return this.ready.get();
  }
}