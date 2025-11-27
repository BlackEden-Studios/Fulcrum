package com.bestudios.fulcrum.api.service.messaging;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Message for sending notifications to players across servers.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Message
 */
public final class NotificationMessage extends Message {

  /** The notification of this message */
  public static final String TYPE = "NOTIFICATION";

  /** The title of the notification */
  private final String title;
  /** The content of the notification */
  private final String content;
  /** The type of notification */
  private final NotificationType notification;

  /** The type of notification */
  public enum NotificationType {
    INFO, WARNING, ERROR, SUCCESS, ACHIEVEMENT
  }

  /**
   * Constructs a new NotificationMessage instance.
   * @param notificationTitle   The title of the notification
   * @param notificationContent The content of the notification
   * @param notificationType    The type of notification
   */
  public NotificationMessage(
          @NotNull String notificationTitle,
          @NotNull String notificationContent,
          @NotNull NotificationType notificationType) {
    super(TYPE);
    this.title        = Objects.requireNonNull(notificationTitle, "Title cannot be null");
    this.content      = Objects.requireNonNull(notificationContent, "Content cannot be null");
    this.notification = Objects.requireNonNull(notificationType, "Type cannot be null");
  }

  /**
   * Gets the title of the notification.
   * @return The notification title
   */
  @NotNull @Contract(pure = true)
  public String getTitle() {
    return title;
  }

  /**
   * Gets the content of the notification.
   * @return The notification content
   */
  @NotNull @Contract(pure = true)
  public String getContent() {
    return content;
  }

  /**
   * Gets the type of notification.
   * @return The notification type
   */
  @NotNull @Contract(pure = true)
  public NotificationType getNotificationType() {
    return notification;
  }

  @Override
  protected void serializeFields(@NotNull Map<String, String> map) {
    map.put("title", title);
    map.put("content", content);
    map.put("type", notification.name());
  }

  /**
   * Creates a NotificationMessage instance from a Map using type discrimination.
   * @param map The map containing message data
   * @return NotificationMessage instance or null if invalid
   */
  @Nullable
  static NotificationMessage fromMapInternal(@NotNull Map<String, String> map) {
    String title = map.get("title");
    String content = map.get("content");
    String typeStr = map.get("type");
    // Empty fields are invalid
    if (title == null || content == null || typeStr == null) return null;

    return new NotificationMessage(title, content, NotificationType.valueOf(typeStr));
  }
}
