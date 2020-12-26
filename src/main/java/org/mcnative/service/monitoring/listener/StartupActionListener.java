package org.mcnative.service.monitoring.listener;

import org.mcnative.actionframework.common.action.MAFActionExecutor;
import org.mcnative.actionframework.common.action.MAFActionListener;
import org.mcnative.actionframework.common.action.server.ServerStartupAction;
import org.mcnative.service.monitoring.McNativeMonitoringService;

public class StartupActionListener implements MAFActionListener<ServerStartupAction> {

    private final McNativeMonitoringService monitoringService;

    public StartupActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor mafActionExecutor, ServerStartupAction action) {
        String networkId = mafActionExecutor.getNetworkIdShort();
        String serverId = mafActionExecutor.getClientIdShort();

        createOrUpdateServer(serverId, networkId, action);
    }

    private void createOrUpdateServer(String serverId, String networkId, ServerStartupAction action) {
        boolean exists = !this.monitoringService.getStorageService().getServerCollection().find().where("Id", serverId).execute().isEmpty();
        if(exists) {
            this.monitoringService.getStorageService().getServerCollection().update()
                    .set("Address", action.getAddress().getHostName())
                    .set("Port", action.getAddress().getPort())
                    .set("ProtocolVersion", action.getProtocolVersion())
                    .set("JoinAbleProtocolVersions", convertJoinAbleProtocolVersionsToCommaSeparated(action.getJoinableProtocolVersions()))
                    .set("NetworkTechnology", null)//@Todo add
                    .set("Platform", null)
                    .set("PlatformVersion", action.getPlatformVersion())
                    .set("McNativeBuildNumber", action.getMcnativeBuildNumber())
                    .set("PlatformProxy", null)
                    .where("Id", serverId)
                    .where("NetworkId", networkId).execute();

        } else {
            this.monitoringService.getStorageService().getServerCollection().insert()
                    .set("Id", serverId)
                    .set("NetworkId", networkId)
                    .set("Address", action.getAddress().getHostName())
                    .set("Port", action.getAddress().getPort())
                    .set("ProtocolVersion", action.getProtocolVersion())
                    .set("JoinAbleProtocolVersions", convertJoinAbleProtocolVersionsToCommaSeparated(action.getJoinableProtocolVersions()))
                    .set("NetworkTechnology", "")//@Todo add
                    .set("Platform", "null")
                    .set("PlatformVersion", action.getPlatformVersion())
                    .set("McNativeBuildNumber", action.getMcnativeBuildNumber())
                    .set("PlatformProxy", "")
                    .execute();

        }

        updateServerPlugins(serverId, action);
        updateDatabaseDrivers(serverId, action);
    }

    private void updateServerPlugins(String serverId, ServerStartupAction action) {
        this.monitoringService.getStorageService().getServerPluginsCollection().delete()
                .where("ServerId", serverId)
                .execute();
        for (ServerStartupAction.Plugin plugin : action.getPlugins()) {
            this.monitoringService.getStorageService().getServerPluginsCollection().insert()
                    .set("Id", plugin.getUniqueId())
                    .set("ServerId", serverId)
                    .set("Name", plugin.getName())
                    .set("Version", plugin.getVersion())
                    .execute();
        }
    }

    private void updateDatabaseDrivers(String serverId, ServerStartupAction action) {
        this.monitoringService.getStorageService().getServerDatabaseDriversCollection().delete()
                .where("ServerId", serverId)
                .execute();
        for (String databaseDriver : action.getDatabaseDrivers()) {
            this.monitoringService.getStorageService().getServerDatabaseDriversCollection().insert()
                    .set("ServerId", serverId)
                    .set("Driver", databaseDriver)
                    .execute();
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

            builder.deleteCharAt(builder.length() - 1);

            return builder.toString();
        } else {
            return "";
        }
    }
}
