package com.netflix.maestro.flow.properties;

import java.time.Duration;
import lombok.Data;

/** Configuration properties for task webhook notifications. */
@Data
@SuppressWarnings("checkstyle:MagicNumber")
public class TaskWebhookProperties {
  /** Whether task webhook notifications are enabled. */
  private boolean enabled;

  /** Webhook URL to send task completion notifications. */
  private String url;

  /** HTTP timeout for webhook requests. */
  private Duration timeout = Duration.ofSeconds(30);

  /** Whether to send webhooks for all task statuses or only terminal ones. */
  private boolean onlyTerminal = true;
}
