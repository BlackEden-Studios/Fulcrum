package com.bestudios.fulcrum.api.event;

import com.bestudios.fulcrum.api.util.TimerInfo;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a timer update occurs in Fulcrum.
 * This event is fired by the TimerInfoUpdater when the elapsed time reaches a multiple of the update time.
 * <p></p>
 * Plugins can listen for this event to perform actionsMap when the daily timer updates,
 * such as resetting player statistics, refreshing in-game resources, or performing scheduled tasks.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see TimerInfo
 */
public class TimerUpdateEvent extends Event {

  /**
   * Static handler list for this event, as required by Bukkit.
   */
  private static final HandlerList HANDLERS_LIST = new HandlerList();

  /**
   * Required by Bukkit's event system to get the handler list for this event.
   * This method is called by event handlers to register themselves.
   *
   * @return The handler list for this event
   */
  public static HandlerList getHandlerList() {
    return HANDLERS_LIST;
  }

  /**
   * Required by Bukkit's event system to get the handlers for this event instance.
   * This method is called by the event dispatcher to deliver the event to registered handlers.
   *
   * @return The handler list for this event
   */
  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS_LIST;
  }
}
