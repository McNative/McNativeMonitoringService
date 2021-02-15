package org.mcnative.service.monitoring.listener;

import org.mcnative.actionframework.sdk.actions.server.ServerShutdownConfirmAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionListener;
import org.mcnative.service.monitoring.LogAction;
import org.mcnative.service.monitoring.McNativeMonitoringService;
import org.mcnative.service.monitoring.ServerStatus;

public class ServerShutdownConfirmActionListener implements MAFActionListener<ServerShutdownConfirmAction> {

    private final McNativeMonitoringService monitoringService;

    public ServerShutdownConfirmActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor executor, ServerShutdownConfirmAction action) {
        this.monitoringService.logIncomingAction(executor, action);
        this.monitoringService.getStorageService().addServerLogEntry(executor, LogAction.SHUTDOWN);
        this.monitoringService.getStorageService().updateServerStatus(executor, ServerStatus.OFFLINE);
    }
}
