/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.notification.formatter;

import org.apache.pinot.thirdeye.datalayer.dto.AlertConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.AnomalyFunctionDTO;
import org.joda.time.DateTime;


/**
 * This class holds information about the anomaly detection context
 * which can be rendered into the alert(email, jira) content.
 */
public class ADContentFormatterContext {
  private AnomalyFunctionDTO anomalyFunctionSpec;
  private AlertConfigDTO alertConfig;
  private DateTime start; // anomaly search region starts
  private DateTime end; // anomaly search region ends

  public AnomalyFunctionDTO getAnomalyFunctionSpec() {
    return anomalyFunctionSpec;
  }

  public void setAnomalyFunctionSpec(AnomalyFunctionDTO anomalyFunctionSpec) {
    this.anomalyFunctionSpec = anomalyFunctionSpec;
  }

  public AlertConfigDTO getAlertConfig() {
    return alertConfig;
  }

  public void setAlertConfig(AlertConfigDTO alertConfig) {
    this.alertConfig = alertConfig;
  }

  public DateTime getStart() {
    return start;
  }

  public void setStart(DateTime start) {
    this.start = start;
  }

  public DateTime getEnd() {
    return end;
  }

  public void setEnd(DateTime end) {
    this.end = end;
  }
}
