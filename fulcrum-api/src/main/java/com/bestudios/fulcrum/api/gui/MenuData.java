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
        Map<Integer, MenuElement> elements,
        AtomicBoolean ready // Thread-safe mutable flag
) {

  /**
   * Static Factory.
   * Initializes with ready = false.
   */
  public static @NotNull MenuData create(Object provider, Component title) {
    return new MenuData(provider, title, new ConcurrentHashMap<>(), new AtomicBoolean(false));
  }

  /**
   * Adds an element to the data.
   * @param element The element to add
   */
  public void addElement(MenuElement element) {
    this.elements.put(element.slot(), element);
  }

  /**
   * Removes an element from the data.
   * @param slot The slot of the element to remove
   */
  public void removeElement(int slot) {
    this.elements.remove(slot);
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