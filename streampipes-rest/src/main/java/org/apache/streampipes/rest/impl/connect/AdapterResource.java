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

package org.apache.streampipes.rest.impl.connect;

import org.apache.streampipes.commons.exceptions.connect.AdapterException;
import org.apache.streampipes.commons.prometheus.adapter.AdapterMetricsManager;
import org.apache.streampipes.connect.management.management.AdapterMasterManagement;
import org.apache.streampipes.connect.management.management.AdapterUpdateManagement;
import org.apache.streampipes.manager.pipeline.PipelineManager;
import org.apache.streampipes.model.client.user.Permission;
import org.apache.streampipes.model.client.user.Role;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.connect.adapter.PipelineUpdateInfo;
import org.apache.streampipes.model.message.Message;
import org.apache.streampipes.model.message.Notifications;
import org.apache.streampipes.model.monitoring.SpLogMessage;
import org.apache.streampipes.resource.management.PermissionResourceManager;
import org.apache.streampipes.resource.management.SpResourceManager;
import org.apache.streampipes.rest.security.AuthConstants;
<<<<<<< HEAD
=======
import org.apache.streampipes.rest.security.SpPermissionEvaluator;
import org.apache.streampipes.rest.shared.constants.SpMediaType;
>>>>>>> upstream/dev
import org.apache.streampipes.storage.api.IPipelineStorage;
import org.apache.streampipes.storage.management.StorageDispatcher;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/connect/master/adapters")
public class AdapterResource extends AbstractAdapterResource<AdapterMasterManagement> {

  private static final Logger LOG = LoggerFactory.getLogger(AdapterResource.class);

  public AdapterResource() {
    super(() -> new AdapterMasterManagement(
        StorageDispatcher.INSTANCE.getNoSqlStore()
                                  .getAdapterInstanceStorage(),
        new SpResourceManager().manageAdapters(),
        new SpResourceManager().manageDataStreams(),
        AdapterMetricsManager.INSTANCE.getAdapterMetrics()
    ));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("this.hasWriteAuthority()")
  public ResponseEntity<? extends Message> addAdapter(@RequestBody AdapterDescription adapterDescription) {
    var principalSid = getAuthenticatedUserSid();
    var username = getAuthenticatedUsername();
    String adapterId;
    LOG.info("User: " + username + " starts adapter " + adapterDescription.getElementId());

    try {
      adapterId = managementService.addAdapter(adapterDescription, principalSid);
    } catch (AdapterException e) {
      LOG.error("Error while starting adapter with id " + adapterDescription.getAppId(), e);
      return ok(Notifications.error(e.getMessage()));
    }

    LOG.info("Stream adapter with id " + adapterId + " successfully added");
    return ok(Notifications.success(adapterId));
  }

<<<<<<< HEAD
  @PutMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(AuthConstants.HAS_WRITE_ADAPTER_PRIVILEGE)
=======
  @PostMapping(path = "compact", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE,
      SpMediaType.YAML,
      SpMediaType.YML
  })
  @PreAuthorize("this.hasWriteAuthority()")
  public ResponseEntity<?> convertToCompactAdapter(@RequestBody AdapterDescription adapterDescription)
      throws Exception {
    return ok(new CompactAdapterManagement(List.of()).convertToCompactAdapter(adapterDescription));
  }

  @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("this.hasWriteAuthority() and hasPermission('#adapterDescription.elementId', 'WRITE')")
>>>>>>> upstream/dev
  public ResponseEntity<? extends Message> updateAdapter(@RequestBody AdapterDescription adapterDescription) {
    var updateManager = new AdapterUpdateManagement(managementService);
    try {
      updateManager.updateAdapter(adapterDescription);
    } catch (AdapterException e) {
      LOG.error("Error while updating adapter with id " + adapterDescription.getElementId(), e);
      return ok(Notifications.error(e.getMessage()));
    }

    return ok(Notifications.success(adapterDescription.getElementId()));
  }

<<<<<<< HEAD
  @PutMapping(
      path = "pipeline-migration-preflight",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
=======
  @PutMapping(path = "pipeline-migration-preflight", consumes = MediaType.APPLICATION_JSON_VALUE,
              produces = MediaType.APPLICATION_JSON_VALUE)
>>>>>>> upstream/dev
  @PreAuthorize(AuthConstants.HAS_WRITE_ADAPTER_PRIVILEGE)
  public ResponseEntity<List<PipelineUpdateInfo>> performPipelineMigrationPreflight(
      @RequestBody AdapterDescription adapterDescription) {
    var updateManager = new AdapterUpdateManagement(managementService);
    var migrations = updateManager.checkPipelineMigrations(adapterDescription);

    return ok(migrations);
  }

<<<<<<< HEAD
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(AuthConstants.HAS_READ_ADAPTER_PRIVILEGE)
  public ResponseEntity<?> getAdapter(@PathVariable("id") String adapterId) {

    try {
      AdapterDescription adapterDescription = getAdapterDescription(adapterId);

      return ok(adapterDescription);
    } catch (AdapterException e) {
      LOG.error("Error while getting adapter with id " + adapterId, e);
=======
  @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, SpMediaType.YAML, SpMediaType.YML})
  @PreAuthorize("this.hasReadAuthority()")
  public ResponseEntity<?> getAdapter(
      @PathVariable("id") String elementId,
      @RequestParam(value = "output",
                    defaultValue = "full",
                    required = false) String outputMode
  ) {

    try {
      var adapterDescription = getAdapterDescription(elementId);

      // This check is done here because the adapter permission is checked based on the corresponding data stream
      // and not based on the element id
      if (!checkAdapterReadPermission(adapterDescription)) {
        LOG.error("User is not allowed to read adapter {}", elementId);
        return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED)
                             .build();
      }

      if (outputMode.equalsIgnoreCase("compact")) {
        return ok(toCompactAdapterDescription(adapterDescription));
      } else {
        return ok(adapterDescription);
      }
    } catch (AdapterException e) {
      LOG.error("Error while getting adapter with id {}", elementId, e);
      return fail();
    } catch (Exception e) {
      LOG.error("Error while transforming adapter {}", elementId, e);
>>>>>>> upstream/dev
      return fail();
    }
  }

  /**
   * Checks if the current user has the permission to read the adapter
   */
  private boolean checkAdapterReadPermission(AdapterDescription adapterDescription) {
    var spPermissionEvaluator = new SpPermissionEvaluator();
    var authentication = SecurityContextHolder.getContext()
                                              .getAuthentication();
    return spPermissionEvaluator.hasPermission(
        authentication,
        adapterDescription.getCorrespondingDataStreamElementId(),
        "READ"
    );
  }

  @PostMapping(path = "/{id}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("this.hasWriteAuthority() and hasPermission('#elementId', 'WRITE')")
  public ResponseEntity<?> stopAdapter(@PathVariable("id") String elementId) {
    try {
      managementService.stopStreamAdapter(elementId);
      return ok(Notifications.success("Adapter started"));
    } catch (AdapterException e) {
<<<<<<< HEAD
      LOG.error("Could not stop adapter with id " + adapterId, e);
=======
      LOG.error("Could not stop adapter with id {}", elementId, e);
>>>>>>> upstream/dev
      return serverError(SpLogMessage.from(e));
    }
  }

  @PostMapping(path = "/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("this.hasWriteAuthority() and hasPermission('#elementId', 'WRITE')")
  public ResponseEntity<?> startAdapter(@PathVariable("id") String elementId) {
    try {
      managementService.startStreamAdapter(elementId);
      return ok(Notifications.success("Adapter stopped"));
    } catch (AdapterException e) {
<<<<<<< HEAD
      LOG.error("Could not start adapter with id " + adapterId, e);
=======
      LOG.error("Could not start adapter with id {}", elementId, e);
>>>>>>> upstream/dev
      return serverError(SpLogMessage.from(e));
    }
  }

  @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
<<<<<<< HEAD
  @PreAuthorize(AuthConstants.HAS_DELETE_ADAPTER_PRIVILEGE)
  public ResponseEntity<?> deleteAdapter(@PathVariable("id") String elementId,
                                         @RequestParam(value = "deleteAssociatedPipelines", defaultValue = "false")
                                         boolean deleteAssociatedPipelines) {
    List<String> pipelinesUsingAdapter = getPipelinesUsingAdapter(elementId);
    IPipelineStorage pipelineStorageAPI = StorageDispatcher.INSTANCE.getNoSqlStore().getPipelineStorageAPI();
=======
  @PreAuthorize("this.hasWriteAuthority() and hasPermission('#elementId', 'WRITE')")
  public ResponseEntity<?> deleteAdapter(
      @PathVariable("id") String elementId,
      @RequestParam(value = "deleteAssociatedPipelines", defaultValue = "false")
      boolean deleteAssociatedPipelines
  ) {
    List<String> pipelinesUsingAdapter = getPipelinesUsingAdapter(elementId);
    IPipelineStorage pipelineStorageAPI = StorageDispatcher.INSTANCE.getNoSqlStore()
                                                                    .getPipelineStorageAPI();
>>>>>>> upstream/dev

    if (pipelinesUsingAdapter.isEmpty()) {
      try {
        managementService.deleteAdapter(elementId);
        return ok(Notifications.success("Adapter with id: " + elementId + " is deleted."));
      } catch (AdapterException e) {
        LOG.error("Error while deleting adapter with id " + elementId, e);
        return ok(Notifications.error(e.getMessage()));
      }
    } else if (!deleteAssociatedPipelines) {
<<<<<<< HEAD
      List<String> namesOfPipelinesUsingAdapter =
          pipelinesUsingAdapter.stream().map(pipelineId -> pipelineStorageAPI.getPipeline(pipelineId).getName())
              .collect(
                  Collectors.toList());
      return ResponseEntity.status(HttpStatus.SC_CONFLICT).body(String.join(", ", namesOfPipelinesUsingAdapter));
    } else {
      PermissionResourceManager permissionResourceManager = new PermissionResourceManager();
      // find out the names of pipelines that have an owner and the owner is not the current user
      List<String> namesOfPipelinesNotOwnedByUser = pipelinesUsingAdapter.stream().filter(pipelineId ->
              !permissionResourceManager.findForObjectId(pipelineId).stream().findFirst().map(Permission::getOwnerSid)
                  // if a pipeline has no owner, pretend the owner is the user so the user can delete it
                  .orElse(this.getAuthenticatedUserSid()).equals(this.getAuthenticatedUserSid()))
          .map(pipelineId -> pipelineStorageAPI.getPipeline(pipelineId).getName()).collect(Collectors.toList());
      boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
          .anyMatch(r -> r.getAuthority().equals(
              Role.ROLE_ADMIN.name()));
=======
      List<String> namesOfPipelinesUsingAdapter = pipelinesUsingAdapter
          .stream()
          .map(pipelineId -> pipelineStorageAPI.getElementById(
                                                   pipelineId)
                                               .getName())
          .collect(Collectors.toList());
      return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                           .body(String.join(", ", namesOfPipelinesUsingAdapter));
    } else {
      PermissionResourceManager permissionResourceManager = new PermissionResourceManager();
      // find out the names of pipelines that have an owner and the owner is not the current user
      List<String> namesOfPipelinesNotOwnedByUser = pipelinesUsingAdapter
          .stream()
          .filter(pipelineId ->
                      !permissionResourceManager.findForObjectId(
                                                    pipelineId)
                                                .stream()
                                                .findFirst()
                                                .map(
                                                    Permission::getOwnerSid)
                                                // if a pipeline has no owner, pretend the owner
                                                // is the user so the user can delete it
                                                .orElse(
                                                    this.getAuthenticatedUserSid())
                                                .equals(
                                                    this.getAuthenticatedUserSid()))
          .map(pipelineId -> pipelineStorageAPI.getElementById(
                                                   pipelineId)
                                               .getName())
          .collect(Collectors.toList());
      boolean isAdmin = SecurityContextHolder.getContext()
                                             .getAuthentication()
                                             .getAuthorities()
                                             .stream()
                                             .anyMatch(r -> r.getAuthority()
                                                             .equals(
                                                                 DefaultRole.ROLE_ADMIN.name()));
>>>>>>> upstream/dev
      // if the user is admin or owns all pipelines using this adapter,
      // the user can delete all associated pipelines and this adapter
      if (isAdmin || namesOfPipelinesNotOwnedByUser.isEmpty()) {
        try {
          for (String pipelineId : pipelinesUsingAdapter) {
            PipelineManager.stopPipeline(pipelineId, false);
            PipelineManager.deletePipeline(pipelineId);
          }
          managementService.deleteAdapter(elementId);
<<<<<<< HEAD
          return ok(Notifications.success(
              "Adapter with id: " + elementId + " and all pipelines using the adapter are deleted."));
        } catch (Exception e) {
          LOG.error("Error while deleting adapter with id " + elementId + " and all pipelines using the adapter", e);
=======
          return ok(Notifications.success("Adapter with id: " + elementId
                                              + " and all pipelines using the adapter are deleted."));
        } catch (Exception e) {
          LOG.error(
              "Error while deleting adapter with id "
                  + elementId + " and all pipelines using the adapter", e
          );
>>>>>>> upstream/dev
          return ok(Notifications.error(e.getMessage()));
        }
      } else {
        // otherwise, hint the user the names of pipelines using the adapter but not owned by the user
<<<<<<< HEAD
        return ResponseEntity.status(HttpStatus.SC_CONFLICT).body(String.join(", ", namesOfPipelinesNotOwnedByUser));
=======
        return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                             .body(String.join(", ", namesOfPipelinesNotOwnedByUser));
>>>>>>> upstream/dev
      }
    }
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
<<<<<<< HEAD
  @PreAuthorize(AuthConstants.HAS_READ_ADAPTER_PRIVILEGE)
  public ResponseEntity<?> getAllAdapters() {
    try {
      return ok(managementService.getAllAdapterInstances());
    } catch (AdapterException e) {
      LOG.error("Error while getting all adapters", e);
      return ResponseEntity.status(500).build();
    }
=======
  @PreAuthorize("this.hasReadAuthority()")
  @PostFilter("hasPermission(filterObject.correspondingDataStreamElementId, 'READ')")
  public List<AdapterDescription> getAllAdapters() {
    return managementService.getAllAdapterInstances();
>>>>>>> upstream/dev
  }

  private AdapterDescription getAdapterDescription(String elementId) throws AdapterException {
    return managementService.getAdapter(elementId);
  }

  private List<String> getPipelinesUsingAdapter(String adapterId) {
<<<<<<< HEAD
    return StorageDispatcher.INSTANCE
        .getNoSqlStore()
        .getPipelineStorageAPI()
        .getPipelinesUsingAdapter(adapterId);
=======
    return StorageDispatcher.INSTANCE.getNoSqlStore()
                                     .getPipelineStorageAPI()
                                     .getPipelinesUsingAdapter(adapterId);
>>>>>>> upstream/dev
  }

}
