/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.streampipes.connect.management.management;

import org.apache.streampipes.commons.exceptions.NoServiceEndpointsAvailableException;
import org.apache.streampipes.commons.exceptions.SepaParseException;
import org.apache.streampipes.commons.exceptions.connect.AdapterException;
import org.apache.streampipes.commons.prometheus.adapter.AdapterMetrics;
import org.apache.streampipes.connect.management.util.GroundingUtils;
import org.apache.streampipes.connect.management.util.WorkerPaths;
import org.apache.streampipes.manager.monitoring.pipeline.ExtensionsLogProvider;
import org.apache.streampipes.manager.verification.DataStreamVerifier;
import org.apache.streampipes.model.SpDataStream;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.util.ElementIdGenerator;
import org.apache.streampipes.resource.management.AdapterResourceManager;
import org.apache.streampipes.resource.management.DataStreamResourceManager;
import org.apache.streampipes.storage.api.IAdapterStorage;
import org.apache.streampipes.storage.management.StorageDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class is responsible for managing all the adapter instances which are executed on worker nodes
 */
public class AdapterMasterManagement {

  private static final Logger LOG = LoggerFactory.getLogger(AdapterMasterManagement.class);

  private final IAdapterStorage adapterInstanceStorage;
  private final AdapterMetrics adapterMetrics;
  private final AdapterResourceManager adapterResourceManager;

  private final DataStreamResourceManager dataStreamResourceManager;

  public AdapterMasterManagement(IAdapterStorage adapterStorage,
                                 AdapterResourceManager adapterResourceManager,
                                 DataStreamResourceManager dataStreamResourceManager,
                                 AdapterMetrics adapterMetrics
  ) {
    this.adapterInstanceStorage = adapterStorage;
    this.adapterMetrics = adapterMetrics;
    this.adapterResourceManager = adapterResourceManager;
    this.dataStreamResourceManager = dataStreamResourceManager;
  }

  public String addAdapter(AdapterDescription ad,
                           String principalSid)
      throws AdapterException {

    // Create elementId for adapter
    var dataStreamElementId = ElementIdGenerator.makeElementId(SpDataStream.class);
    ad.setElementId(ElementIdGenerator.makeElementId(ad));
    ad.setCreatedAt(System.currentTimeMillis());
    ad.setCorrespondingDataStreamElementId(dataStreamElementId);

    // Add EventGrounding to AdapterDescription
    var eventGrounding = GroundingUtils.createEventGrounding();
    ad.setEventGrounding(eventGrounding);

    var elementId = this.adapterResourceManager.encryptAndCreate(ad);

<<<<<<< HEAD
    // Create stream
    var storedDescription = new SourcesManagement().createAdapterDataStream(ad, dataStreamElementId);
    storedDescription.setCorrespondingAdapterId(elementId);
    installDataSource(storedDescription, principalSid, true);
    LOG.info("Install source (source URL: {} in backend", ad.getElementId());

    return ad.getElementId();
=======
    // Stream is only created if the adpater is successfully stored
    createDataStreamForAdapter(adapterDescription, adapterId, dataStreamElementId, principalSid);
  }

  private void createDataStreamForAdapter(
      AdapterDescription adapterDescription,
      String adapterId,
      String streamId,
      String principalSid
  ) throws AdapterException {
    var storedDescription = new SourcesManagement()
        .createAdapterDataStream(adapterDescription, streamId);
    storedDescription.setCorrespondingAdapterId(adapterId);
    installDataSource(storedDescription, principalSid);
    LOG.info("Install source (source URL: {} in backend", adapterDescription.getElementId());
>>>>>>> upstream/dev
  }



  public AdapterDescription getAdapter(String elementId) throws AdapterException {
    List<AdapterDescription> allAdapters = adapterInstanceStorage.getAllAdapters();

    if (allAdapters != null && elementId != null) {
      for (AdapterDescription ad : allAdapters) {
        if (elementId.equals(ad.getElementId())) {
          return ad;
        }
      }
    }

    throw new AdapterException("Could not find adapter with id: " + elementId);
  }

  /**
   * First the adapter is stopped removed, then the corresponding data source is deleted
   *
   * @param elementId The elementId of the adapter instance
   * @throws AdapterException when adapter can not be stopped
   */
  public void deleteAdapter(String elementId) throws AdapterException {

    // Stop stream adapter
    try {
      stopStreamAdapter(elementId);
    } catch (AdapterException e) {
      LOG.info("Could not stop adapter: " + elementId, e);
    }

    AdapterDescription adapter = adapterInstanceStorage.getAdapter(elementId);
    // Delete adapter
    adapterResourceManager.delete(elementId);
    ExtensionsLogProvider.INSTANCE.remove(elementId);
    LOG.info("Successfully deleted adapter: " + elementId);

    // Delete data stream
    this.dataStreamResourceManager.delete(adapter.getCorrespondingDataStreamElementId());
    LOG.info("Successfully deleted data stream: " + adapter.getCorrespondingDataStreamElementId());
  }

<<<<<<< HEAD
  public List<AdapterDescription> getAllAdapterInstances() throws AdapterException {

    List<AdapterDescription> allAdapters = adapterInstanceStorage.getAllAdapters();

    if (allAdapters == null) {
      throw new AdapterException("Could not get all adapters");
    }

    return allAdapters;
=======
  public List<AdapterDescription> getAllAdapterInstances() {
    return adapterInstanceStorage.findAll();
>>>>>>> upstream/dev
  }

  public void stopStreamAdapter(String elementId) throws AdapterException {
    AdapterDescription ad = adapterInstanceStorage.getAdapter(elementId);

    WorkerRestClient.stopStreamAdapter(ad.getSelectedEndpointUrl(), ad);
    ExtensionsLogProvider.INSTANCE.reset(elementId);

    // remove the adapter from the metrics manager so that
    // no metrics for this adapter are exposed anymore
    try {
      adapterMetrics.remove(ad.getElementId(), ad.getName());
    } catch (NoSuchElementException e) {
      LOG.error("Could not remove adapter metrics for adapter {}", ad.getName());
    }
  }

  public void startStreamAdapter(String elementId) throws AdapterException {

    var ad = adapterInstanceStorage.getAdapter(elementId);

    try {
      // Find endpoint to start adapter on
      var baseUrl = WorkerPaths.findEndpointUrl(ad.getAppId());

      // Update selected endpoint URL of adapter
      ad.setSelectedEndpointUrl(baseUrl);
      adapterInstanceStorage.updateAdapter(ad);

      // Invoke adapter instance
      WorkerRestClient.invokeStreamAdapter(baseUrl, elementId);

      // register the adapter at the metrics manager so that the AdapterHealthCheck can send metrics
      adapterMetrics.register(ad.getElementId(), ad.getName());

      LOG.info("Started adapter " + elementId + " on: " + baseUrl);
    } catch (NoServiceEndpointsAvailableException | URISyntaxException e) {
      throw new AdapterException("Could not start adapter due to unavailable service endpoint", e);
    }
  }

<<<<<<< HEAD
  private void installDataSource(SpDataStream stream,
                                 String principalSid,
                                 boolean publicElement) throws AdapterException {
=======
  private void installDataSource(
      SpDataStream stream,
      String principalSid
  ) throws AdapterException {
>>>>>>> upstream/dev
    try {
      new DataStreamVerifier(stream).verifyAndAdd(principalSid, false);
    } catch (SepaParseException e) {
      LOG.error("Error while installing data source: {}", stream.getElementId(), e);
      throw new AdapterException();
    }
  }

  private IAdapterStorage getAdapterInstanceStorage() {
    return StorageDispatcher.INSTANCE.getNoSqlStore().getAdapterInstanceStorage();
  }
}
