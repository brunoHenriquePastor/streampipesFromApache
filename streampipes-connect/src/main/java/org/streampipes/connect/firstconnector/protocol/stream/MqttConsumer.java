/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.streampipes.connect.firstconnector.protocol.stream;

import org.fusesource.mqtt.client.*;
import org.streampipes.messaging.InternalEventProcessor;

public class MqttConsumer implements Runnable {

  private String broker;
  private String topic;
  private InternalEventProcessor<byte[]> consumer;
  private boolean running;
  private int maxElementsToReceive = -1;
  private int messageCount = 0;

  public MqttConsumer(String broker, String topic, InternalEventProcessor<byte[]> consumer) {
    this.broker = broker;
    this.topic = topic;
    this.consumer = consumer;
  }

  public MqttConsumer(String broker, String topic, InternalEventProcessor<byte[]> consumer, int maxElementsToReceive) {
    this(broker, topic, consumer);
    this.maxElementsToReceive = maxElementsToReceive;
  }


  @Override
  public void run() {
    this.running = true;
    MQTT mqtt = new MQTT();
    try {
      mqtt.setHost(broker);
      BlockingConnection connection = mqtt.blockingConnection();
      connection.connect();
      Topic[] topics = {new Topic(topic, QoS.AT_LEAST_ONCE)};
      byte[] qoses = connection.subscribe(topics);

      while(running && ((maxElementsToReceive == -1) || (this.messageCount <= maxElementsToReceive))) {
        Message message = connection.receive();
        byte[] payload = message.getPayload();
        consumer.onEvent(payload);
        message.ack();
        this.messageCount++;
      }
      connection.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void close() {
    this.running = false;
  }

  public Integer getMessageCount() {
    return messageCount;
  }
}
