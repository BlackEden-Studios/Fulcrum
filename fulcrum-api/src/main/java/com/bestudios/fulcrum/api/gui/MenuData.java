package com.bestudios.fulcrum.api.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        Set<Integer> updates,
        AtomicBoolean ready,
        Map<Object, Runnable> observers
) {

  /**
   * Factory method to create a new MenuData instance.
   *
   * @param provider The data provider (can be any object).
   * @param title    The title of the menu.
   * @return A new MenuData instance.
   */
  public static @NotNull MenuData create(Object provider, Component title) {
    return new MenuData(
            provider,
            title,
            new ConcurrentHashMap<>(),
            new HashSet<>(),
            new AtomicBoolean(false),
            new ConcurrentHashMap<>()
    );
  }

  // --- State Management ---

  /**
   * Marks the data as ready (no longer updating).
   */
  public void markAsReady() {
    this.ready.set(true);
    notifySubscribers();
  }

  /**
   * Marks the data as busy (updating).
   */
  public void markAsBusy() {
    this.ready.set(false);
  }

  /**
   * Checks if the data is ready (no longer updating).
   * @return true if the data is ready, false otherwise
   */
  public boolean isReady() {
    return this.ready.get();
  }

  /**
  * Clears the list of updates.
  */
  public void clearUpdates() {
    this.updates.clear();
  }

  // --- Subscription ---

  /**
  * Subscribes an observer to the data.
  * @param observer The observer to subscribe.
  * @param callback The callback to invoke when the data is updated.
  */
  public void subscribe(Object observer, Runnable callback) {
    this.observers.put(observer, callback);
  }

  /**
  * Unsubscribes an observer from the data.
  * @param observer The observer to unsubscribe.
  */
  public void unsubscribe(Object observer) {
    this.observers.remove(observer);
  }

  /**
  * Notifies all subscribed observers that the data has been updated.
  */
  private void notifySubscribers() {
    for (Runnable callback : observers.values()) {
      try {
        callback.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // --- Blueprint Management ---

  /**
  * Adds an element to the menu.
  * @param slot    The slot to add the element to.
  * @param element The element to add.
  */
  public void addElement(int slot, MenuElement element) {
    this.elements.put(slot, element);
    this.updates.add(slot);
  }

  /**
  * Removes an element from the menu.
  * @param slot The slot to remove the element from.
  */
  public void removeElement(int slot) {
    this.elements.remove(slot);
    this.updates.add(slot);
  }
}