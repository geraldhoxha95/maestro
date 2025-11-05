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

import com.netflix.maestro.flow.models.Flow;
import com.netflix.maestro.flow.models.Task;

/**
 * Interface for handling webhook notifications when tasks are completed.
 *
 * @author jun-he
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface TaskWebhookHandler {
  /**
   * Handle webhook notification when a task is completed.
   *
   * @param flow the flow containing the completed task
   * @param task the task that was completed
   */
  void onTaskCompleted(Flow flow, Task task);
}
