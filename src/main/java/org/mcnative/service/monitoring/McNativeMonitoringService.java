package org.mcnative.service.monitoring;

import com.rabbitmq.client.ConnectionFactory;
import org.mcnative.actionframework.common.action.server.ServerStartupAction;
import org.mcnative.actionframework.connector.rabbitmq.MAFRabbitMQConnector;
import org.mcnative.service.monitoring.listener.StartupActionListener;
import org.mcnative.service.monitoring.util.Environment;

public final class McNativeMonitoringService {

    private final MAFRabbitMQConnector mafConnector;
    private final StorageService storageService;

    public McNativeMonitoringService() {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(Environment.getVariable("RABBITMQ_HOST"));
        this.mafConnector = MAFRabbitMQConnector.createShared(factory, "McNativeMonitoringService", true);
        registerActionListeners();

        this.storageService = new StorageService();
    }

    private void registerActionListeners() {
        this.mafConnector.subscribeGlobalAction(ServerStartupAction.class, new StartupActionListener(this));
    }

    protected void stop() {

    }

    public StorageService getStorageService() {
        return storageService;
    }
}
