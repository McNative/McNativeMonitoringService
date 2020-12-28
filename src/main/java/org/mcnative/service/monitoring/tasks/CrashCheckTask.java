package org.mcnative.service.monitoring.tasks;

import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import org.mcnative.actionframework.sdk.actions.server.ServerShutdownConfirmAction;
import org.mcnative.actionframework.sdk.common.action.DefaultMAFActionExecutor;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.service.monitoring.LogAction;
import org.mcnative.service.monitoring.McNativeMonitoringService;
import org.mcnative.service.monitoring.ServerStatus;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CrashCheckTask extends Thread {

    private final McNativeMonitoringService service;

    public CrashCheckTask(McNativeMonitoringService service) {
        this.service = service;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            for (QueryResultEntry resultEntry : this.service.getStorageService().getServerCollection().find()
                    .where("Status", ServerStatus.NO_HEARTBEAT)
                    .whereLower("LastStatusUpdate", new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)))
                    .execute()) {

                String networkId = resultEntry.getString("NetworkId");
                String serverId = resultEntry.getString("Id");
                MAFActionExecutor executor = new DefaultMAFActionExecutor(UUID.fromString(networkId), UUID.fromString(serverId));

                this.service.getStorageService().addServerLogEntry(executor, LogAction.CRASH);
                this.service.getStorageService().updateServerStatus(executor, ServerStatus.OFFLINE);

                this.service.getMafConnector().sendActionOnBehalf(executor, new ServerShutdownConfirmAction());
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(20));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
