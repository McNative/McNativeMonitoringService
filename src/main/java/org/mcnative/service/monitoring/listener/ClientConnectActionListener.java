package org.mcnative.service.monitoring.listener;

import org.mcnative.actionframework.sdk.actions.client.ClientConnectAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionListener;
import org.mcnative.service.monitoring.McNativeMonitoringService;
import org.mcnative.service.monitoring.ServerInfo;
import org.mcnative.service.monitoring.ServerStatus;

import java.util.concurrent.TimeUnit;

public class ClientConnectActionListener implements MAFActionListener<ClientConnectAction> {

    private final McNativeMonitoringService monitoringService;

    public ClientConnectActionListener(McNativeMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void onActionReceive(MAFActionExecutor executor, ClientConnectAction action) {
        this.monitoringService.logIncomingAction(executor, action);
        ServerInfo info = this.monitoringService.getStorageService().getServerStatus(executor);
        if(info != null && info.getStatus() == ServerStatus.NO_HEARTBEAT
                && info.getLastStatusUpdate() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))) {
            this.monitoringService.getStorageService().updateServerStatus(executor, ServerStatus.ONLINE);
        }
    }
}
