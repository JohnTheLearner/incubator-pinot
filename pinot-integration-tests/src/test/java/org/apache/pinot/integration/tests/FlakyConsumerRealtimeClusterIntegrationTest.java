/**
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
package org.apache.pinot.integration.tests;

import java.lang.reflect.Constructor;
import java.util.Random;
import org.apache.pinot.common.data.Schema;
import org.apache.pinot.common.metadata.instance.InstanceZKMetadata;
import org.apache.pinot.common.metrics.ServerMetrics;
import org.apache.pinot.core.data.GenericRow;
import org.apache.pinot.core.realtime.stream.PartitionLevelConsumer;
import org.apache.pinot.core.realtime.stream.StreamConfig;
import org.apache.pinot.core.realtime.stream.StreamConsumerFactory;
import org.apache.pinot.core.realtime.stream.StreamLevelConsumer;
import org.apache.pinot.core.realtime.stream.StreamMetadataProvider;
import org.apache.pinot.core.realtime.impl.kafka.KafkaStarterUtils;
import org.testng.annotations.BeforeClass;


/**
 * Integration test that simulates a flaky Kafka consumer.
 */
public class FlakyConsumerRealtimeClusterIntegrationTest extends RealtimeClusterIntegrationTest {

  @BeforeClass
  @Override
  public void setUp()
      throws Exception {
    super.setUp();
  }

  @Override
  protected String getStreamConsumerFactoryClassName() {
    return FlakyStreamFactory.class.getName();
  }

  public static class FlakyStreamLevelConsumer implements StreamLevelConsumer {
    private StreamLevelConsumer _streamLevelConsumer;
    private Random _random = new Random();

    public FlakyStreamLevelConsumer(String clientId, String tableName, StreamConfig streamConfig, Schema schema,
        InstanceZKMetadata instanceZKMetadata, ServerMetrics serverMetrics) {
      try {
        final Constructor constructor = Class.forName(KafkaStarterUtils.KAFKA_STREAM_LEVEL_CONSUMER_CLASS_NAME)
            .getConstructor(String.class, String.class, StreamConfig.class, Schema.class, InstanceZKMetadata.class,
                ServerMetrics.class);
        _streamLevelConsumer = (StreamLevelConsumer) constructor
            .newInstance(clientId, tableName, streamConfig, schema, instanceZKMetadata, serverMetrics);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void start()
        throws Exception {
      _streamLevelConsumer.start();
    }

    @Override
    public GenericRow next(GenericRow destination) {
      // Return a null row every ~1/1000 rows and an exception every ~1/1000 rows
      int randomValue = _random.nextInt(1000);

      if (randomValue == 0) {
        return null;
      } else if (randomValue == 1) {
        throw new RuntimeException("Flaky stream level consumer exception");
      } else {
        return _streamLevelConsumer.next(destination);
      }
    }

    @Override
    public void commit() {
      // Fail to commit 50% of the time
      boolean failToCommit = _random.nextBoolean();

      if (failToCommit) {
        throw new RuntimeException("Flaky stream level consumer exception");
      } else {
        _streamLevelConsumer.commit();
      }
    }

    @Override
    public void shutdown()
        throws Exception {
      _streamLevelConsumer.shutdown();
    }
  }

  public static class FlakyStreamFactory extends StreamConsumerFactory {
    @Override
    public PartitionLevelConsumer createPartitionLevelConsumer(String clientId, int partition) {
      throw new UnsupportedOperationException();
    }

    @Override
    public StreamLevelConsumer createStreamLevelConsumer(String clientId, String tableName, Schema schema,
        InstanceZKMetadata instanceZKMetadata, ServerMetrics serverMetrics) {
      return new FlakyStreamLevelConsumer(clientId, tableName, _streamConfig, schema, instanceZKMetadata,
          serverMetrics);
    }

    @Override
    public StreamMetadataProvider createPartitionMetadataProvider(String clientId, int partition) {
      throw new UnsupportedOperationException();
    }

    @Override
    public StreamMetadataProvider createStreamMetadataProvider(String clientId) {
      throw new UnsupportedOperationException();
    }
  }
}
