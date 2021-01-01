package org.mcnative.service.monitoring.listener;

import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import org.mcnative.actionframework.sdk.actions.server.ServerStartupAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionListener;
import org.mcnative.service.monitoring.LogAction;
import org.mcnative.service.monitoring.McNativeMonitoringService;
import org.mcnative.service.monitoring.ServerStatus;

import java.sql.Timestamp;

public class ServerStartupActionListener implements MAFActionListener<ServerStartupAction> {

    private final McNativeMonitoringService monitoringService;

    public ServerStartupActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor executor, ServerStartupAction action) {
        this.monitoringService.logIncomingAction(executor, action);

        createOrUpdateServer(executor, action);
    }

    private void createOrUpdateServer(MAFActionExecutor executor, ServerStartupAction action) {
        QueryResultEntry resultEntry = this.monitoringService.getStorageService().getServerCollection().find()
                .where("Id", executor.getClientId().toString())
                .execute().firstOrNull();
        boolean exists = resultEntry != null;
        if(exists) {
            this.monitoringService.getStorageService().getServerCollection().update()
                    .set("Name", action.getName())
                    .set("Address", action.getAddress().getHostName())
                    .set("Port", action.getAddress().getPort())
                    .set("ServerGroup", action.getServerGroup())
                    .set("PlatformName", action.getPlatformName())
                    .set("PlatformVersion", action.getPlatformVersion())
                    .set("PlatformProxy", action.isPlatformProxy())
                    .set("NetworkTechnology", action.getNetworkTechnology())
                    .set("ProtocolVersion", action.getProtocolVersion())
                    .set("JoinAbleProtocolVersions", convertJoinAbleProtocolVersionsToCommaSeparated(action.getJoinAbleProtocolVersions()))
                    .set("McNativeBuildNumber", action.getMcnativeBuildNumber())
                    .set("OperatingSystem", action.getOperatingSystem())
                    .set("OperatingSystemArchitecture", action.getOsArchitecture())
                    .set("JavaVersion", action.getJavaVersion())
                    .set("DeviceId", action.getDeviceId())
                    .set("MaximumMemory", action.getMaximumMemory())
                    .set("AvailableCores", action.getAvailableCores())
                    .set("Status", ServerStatus.ONLINE)
                    .set("LastStatusUpdate", new Timestamp(System.currentTimeMillis()))
                    .where("Id", executor.getClientId().toString())
                    .where("NetworkId", executor.getNetworkId().toString()).execute();
            if(!resultEntry.getString("Status").equals(ServerStatus.NO_HEARTBEAT.toString())) {
                this.monitoringService.getStorageService().addServerLogEntry(executor, LogAction.STARTUP);
            }
        } else {
            this.monitoringService.getStorageService().getServerCollection().insert()
                    .set("Id", executor.getClientId().toString())
                    .set("NetworkId", executor.getNetworkId().toString())
                    .set("Name", action.getName())
                    .set("Address", action.getAddress().getHostName())
                    .set("Port", action.getAddress().getPort())
                    .set("ServerGroup", action.getServerGroup())
                    .set("PlatformName", action.getPlatformName())
                    .set("PlatformVersion", action.getPlatformVersion())
                    .set("PlatformProxy", action.isPlatformProxy())
                    .set("NetworkTechnology", action.getNetworkTechnology())
                    .set("ProtocolVersion", action.getProtocolVersion())
                    .set("JoinAbleProtocolVersions", convertJoinAbleProtocolVersionsToCommaSeparated(action.getJoinAbleProtocolVersions()))
                    .set("McNativeBuildNumber", action.getMcnativeBuildNumber())
                    .set("OperatingSystem", action.getOperatingSystem())
                    .set("OperatingSystemArchitecture", action.getOsArchitecture())
                    .set("JavaVersion", action.getJavaVersion())
                    .set("DeviceId", action.getDeviceId())
                    .set("MaximumMemory", action.getMaximumMemory())
                    .set("AvailableCores", action.getAvailableCores())
                    .set("Status", ServerStatus.ONLINE)
                    .set("LastStatusUpdate", new Timestamp(System.currentTimeMillis()))
                    .execute();
            this.monitoringService.getStorageService().addServerLogEntry(executor, LogAction.STARTUP);
        }
    }



    private String convertJoinAbleProtocolVersionsToCommaSeparated(int[] protocolVersions) {
        if (protocolVersions.length > 0) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < protocolVersions.length; i++) {
                if(i > 0) {
                    builder.append(",");
                }
                builder.append(protocolVersions[i]);
            }

            return builder.toString();
        } else {
            return "";
        }
    }
}
