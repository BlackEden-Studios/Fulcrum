package com.bestudios.fulcrum.api.gui;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
        AtomicBoolean ready,
        List<Runnable> observers
) {

  public static @NotNull MenuData create(Object provider, Component title) {
    return new MenuData(
            provider,
            title,
            new ConcurrentHashMap<>(),
            new AtomicBoolean(false),
            new CopyOnWriteArrayList<>()
    );
  }

  // --- State Management ---

  public void markAsReady() {
    this.ready.set(true);
    notifySubscribers();
  }

  /**
   * New: Marks the data as busy (updating).
   * Notifies the menu to potentially show a loading screen.
   */
  public void markAsBusy() {
    this.ready.set(false);
  }

  public boolean isReady() {
    return this.ready.get();
  }

  // --- Subscription ---

  public void subscribe(Runnable callback) {
    this.observers.add(callback);
  }

  public void unsubscribe(Runnable callback) {
    this.observers.remove(callback);
  }

  private void notifySubscribers() {
    for (Runnable callback : observers) {
      try {
        callback.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // --- Blueprint Management ---

  public void addElement(int slot, MenuElement element) {
    this.elements.put(slot, element);
  }

  public void removeElement(int slot) {
    this.elements.remove(slot);
  }
}