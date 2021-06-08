package org.mcnative.service.monitoring.listener;

import org.mcnative.actionframework.sdk.actions.server.ServerInfoAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionListener;
import org.mcnative.service.monitoring.McNativeMonitoringService;

public class ServerInfoActionListener implements MAFActionListener<ServerInfoAction> {

    private final McNativeMonitoringService monitoringService;

    public ServerInfoActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor executor, ServerInfoAction action) {
        this.monitoringService.logIncomingAction(executor, action);
        updateServerPlugins(executor, action);
        updateDatabaseDrivers(executor, action);
    }

    private void updateServerPlugins(MAFActionExecutor executor, ServerInfoAction action) {
        this.monitoringService.getStorageService().getServerPluginsCollection().delete()
                .where("ServerId", executor.getClientId().toString())
                .where("NetworkId", executor.getNetworkId().toString())
                .execute();
        for (ServerInfoAction.Plugin plugin : action.getPlugins()) {
            this.monitoringService.getStorageService().getServerPluginsCollection().insert()
                    .set("Id", plugin.getId().toString())
                    .set("ServerId", executor.getClientId().toString())
                    .set("NetworkId", executor.getNetworkId().toString())
                    .set("Name", plugin.getName())
                    .set("Version", plugin.getVersion())
                    .execute();
        }
    }

    private void updateDatabaseDrivers(MAFActionExecutor executor, ServerInfoAction action) {
        this.monitoringService.getStorageService().getServerDatabaseDriversCollection().delete()
                .where("ServerId", executor.getClientId().toString())
                .where("NetworkId", executor.getNetworkId().toString())

                .execute();
        for (String databaseDriver : action.getDatabaseDrivers()) {
            this.monitoringService.getStorageService().getServerDatabaseDriversCollection().insert()
                    .set("ServerId", executor.getClientId().toString())
                    .set("NetworkId", executor.getNetworkId().toString())
                    .set("Driver", databaseDriver)
                    .execute();
        }
    }
}
