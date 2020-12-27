package org.mcnative.service.monitoring.listener;

import org.mcnative.actionframework.sdk.actions.client.ClientDisconnectAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionListener;
import org.mcnative.service.monitoring.McNativeMonitoringService;
import org.mcnative.service.monitoring.ServerInfo;
import org.mcnative.service.monitoring.ServerStatus;

public class ClientDisconnectActionListener implements MAFActionListener<ClientDisconnectAction> {

    private final McNativeMonitoringService monitoringService;

    public ClientDisconnectActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor executor, ClientDisconnectAction action) {
        this.monitoringService.logIncomingAction(executor, action);
        ServerInfo info = this.monitoringService.getStorageService().getServerStatus(executor);
        if(info != null && info.getStatus() != ServerStatus.OFFLINE) {
            this.monitoringService.getStorageService().updateServerStatus(executor, ServerStatus.NO_HEARTBEAT);
        }
    }
}
