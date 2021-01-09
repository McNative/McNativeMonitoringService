package org.mcnative.service.monitoring.listener;

import org.mcnative.actionframework.sdk.actions.server.ServerStatusAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionListener;
import org.mcnative.service.monitoring.McNativeMonitoringService;

public class ServerStatusActionListener implements MAFActionListener<ServerStatusAction> {

    private final McNativeMonitoringService monitoringService;

    public ServerStatusActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor executor, ServerStatusAction action) {
        this.monitoringService.logIncomingAction(executor, action);
        this.monitoringService.getStorageService().getServerCollection().update()
                .set("MaximumPlayerCount", action.getMaximumPlayerCount())
                .set("Tps", toCommaSeparated(action.getRecentTps()))
                .set("UsedMemory", action.getUsedMemory())
                .set("CpuUsage", action.getCpuUsage())
                .where("Id", executor.getClientId().toString())
                .where("NetworkId", executor.getNetworkId().toString())
                .execute();
    }

    private static String toCommaSeparated(float[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if(i > 0) builder.append(",");
            builder.append(values[i]);
        }
        return builder.toString();
    }
}
