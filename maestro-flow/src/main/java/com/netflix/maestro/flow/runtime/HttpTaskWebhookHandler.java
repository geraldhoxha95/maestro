/*
 * Copyright 2024 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.maestro.flow.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.maestro.flow.models.Flow;
import com.netflix.maestro.flow.models.Task;
import com.netflix.maestro.metrics.MaestroMetrics;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP-based webhook handler that sends task completion notifications to configured endpoints.
 *
 * @author jun-he
 */
@Slf4j
@SuppressWarnings("checkstyle:MagicNumber")
public class HttpTaskWebhookHandler implements TaskWebhookHandler {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final MaestroMetrics metrics;
  private final String webhookUrl;
  private final Duration timeout;

  private static final String WEBHOOK_FAILED_METRIC = "webhook_failed";

  public HttpTaskWebhookHandler(
      ObjectMapper objectMapper, MaestroMetrics metrics, String webhookUrl, Duration timeout) {
    this.objectMapper = objectMapper;
    this.metrics = metrics;
    this.webhookUrl = webhookUrl;
    this.timeout = timeout;
    this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
  }

  @Override
  public void onTaskCompleted(Flow flow, Task task) {
    if (webhookUrl == null || webhookUrl.isBlank()) {
      LOG.debug(
          "No webhook URL configured, skipping webhook notification for task {}",
          task.referenceTaskName());
      return;
    }

    try {
      // Create webhook payload
      TaskWebhookPayload payload = createWebhookPayload(flow, task);
      String payloadJson = objectMapper.writeValueAsString(payload);

      // Build HTTP request
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(webhookUrl))
              .header("Content-Type", "application/json")
              .header("User-Agent", "Maestro-Task-Webhook")
              .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
              .timeout(timeout)
              .build();

      // Send webhook asynchronously
      CompletableFuture<HttpResponse<String>> future =
          httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

      future.whenComplete(
          (response, throwable) -> {
            if (throwable != null) {
              LOG.warn(
                  "Failed to send webhook for task {}: {}",
                  task.referenceTaskName(),
                  throwable.getMessage());
              metrics.counter(WEBHOOK_FAILED_METRIC, getClass());
            } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
              LOG.debug("Successfully sent webhook for task {}", task.referenceTaskName());
              metrics.counter("webhook_success", getClass());
            } else {
              LOG.warn(
                  "Webhook for task {} returned status code: {}",
                  task.referenceTaskName(),
                  response.statusCode());
              metrics.counter(WEBHOOK_FAILED_METRIC, getClass());
            }
          });
    } catch (Exception e) {
      LOG.warn("Error preparing webhook for task {}: {}", task.referenceTaskName(), e.getMessage());
      metrics.counter("webhook_error", getClass());
    }
  }

  private TaskWebhookPayload createWebhookPayload(Flow flow, Task task) {
    return TaskWebhookPayload.builder()
        .flowId(flow.getReference())
        .groupId(flow.getGroupId())
        .taskId(task.getTaskId())
        .taskReferenceName(task.referenceTaskName())
        .taskType(task.getTaskType())
        .status(task.getStatus().name())
        .successful(task.getStatus().isSuccessful())
        .terminal(task.getStatus().isTerminal())
        .reasonForIncompletion(task.getReasonForIncompletion())
        .startTime(task.getStartTime())
        .endTime(task.getEndTime())
        .outputData(task.getOutputData())
        .build();
  }

  /** Data class for webhook payload. */
  @lombok.Builder
  @lombok.Data
  public static class TaskWebhookPayload {
    private final String flowId;
    private final long groupId;
    private final String taskId;
    private final String taskReferenceName;
    private final String taskType;
    private final String status;
    private final boolean successful;
    private final boolean terminal;
    private final String reasonForIncompletion;
    private final Long startTime;
    private final Long endTime;
    private final Map<String, Object> outputData;
  }
}
