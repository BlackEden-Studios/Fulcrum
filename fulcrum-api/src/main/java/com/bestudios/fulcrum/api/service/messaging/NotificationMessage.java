package com.bestudios.fulcrum.api.service.messaging;

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

  public static final String TYPE = "NOTIFICATION";

  private final String title;
  private final String content;
  private final NotificationType notificationType;

  public enum NotificationType {
    INFO, WARNING, ERROR, SUCCESS, ACHIEVEMENT
  }

  public NotificationMessage(
          @NotNull String title,
          @NotNull String content,
          @NotNull NotificationType notificationType) {
    super(TYPE);
    this.title = Objects.requireNonNull(title, "Title cannot be null");
    this.content = Objects.requireNonNull(content, "Content cannot be null");
    this.notificationType = Objects.requireNonNull(notificationType, "Type cannot be null");
  }

  @NotNull
  public String getTitle() {
    return title;
  }

  @NotNull
  public String getContent() {
    return content;
  }

  @NotNull
  public NotificationType getNotificationType() {
    return notificationType;
  }

  @Override
  protected void serializeFields(@NotNull Map<String, String> map) {
    map.put("title", title);
    map.put("content", content);
    map.put("notificationType", notificationType.name());
  }

  @Nullable
  static NotificationMessage fromMapInternal(@NotNull Map<String, String> map) {
    String title = map.get("title");
    String content = map.get("content");
    String typeStr = map.get("notificationType");

    if (title == null || content == null || typeStr == null) {
      return null;
    }

    try {
      NotificationType type = NotificationType.valueOf(typeStr);
      return new NotificationMessage(title, content, type);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
