package org.mcnative.service.monitoring.listener;

import org.mcnative.actionframework.sdk.actions.server.ServerShutdownAction;
import org.mcnative.actionframework.sdk.actions.server.ServerShutdownConfirmAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionListener;
import org.mcnative.service.monitoring.LogAction;
import org.mcnative.service.monitoring.McNativeMonitoringService;
import org.mcnative.service.monitoring.ServerStatus;

public class ServerShutdownActionListener implements MAFActionListener<ServerShutdownAction> {

    private final McNativeMonitoringService monitoringService;

    public ServerShutdownActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor executor, ServerShutdownAction action) {
        this.monitoringService.logIncomingAction(executor, action);
        this.monitoringService.getStorageService().addServerLogEntry(executor, LogAction.SHUTDOWN);
        this.monitoringService.getStorageService().updateServerStatus(executor, ServerStatus.OFFLINE);

        this.monitoringService.getMafConnector().sendActionOnBehalf(executor, new ServerShutdownConfirmAction());
    }
}
