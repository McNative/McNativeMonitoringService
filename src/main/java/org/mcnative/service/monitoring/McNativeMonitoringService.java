package org.mcnative.service.monitoring;

import com.rabbitmq.client.ConnectionFactory;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.logging.PretronicLoggerFactory;
import net.pretronic.libraries.logging.bridge.slf4j.SLF4JStaticBridge;
import org.mcnative.actionframework.sdk.actions.client.ClientConnectAction;
import org.mcnative.actionframework.sdk.actions.client.ClientDisconnectAction;
import org.mcnative.actionframework.sdk.actions.server.ServerInfoAction;
import org.mcnative.actionframework.sdk.actions.server.ServerShutdownAction;
import org.mcnative.actionframework.sdk.actions.server.ServerStartupAction;
import org.mcnative.actionframework.sdk.actions.server.ServerStatusAction;
import org.mcnative.actionframework.sdk.common.action.MAFAction;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.actionframework.service.connector.rabbitmq.MAFRabbitMQConnector;
import org.mcnative.service.monitoring.listener.*;
import org.mcnative.service.monitoring.tasks.CrashCheckTask;
import org.mcnative.service.monitoring.util.Environment;

public final class McNativeMonitoringService {

    private final MAFRabbitMQConnector mafConnector;
    private final StorageService storageService;
    private final PretronicLogger logger;

    private final Thread crashCheckTask;

    public McNativeMonitoringService(PretronicLogger logger) {
        this.logger = logger;

        this.storageService = new StorageService(logger);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Environment.getVariable("RABBITMQ_HOST"));
        factory.setUsername(Environment.getVariable("RABBITMQ_USERNAME"));
        factory.setPassword(Environment.getVariable("RABBITMQ_PASSWORD"));
        this.mafConnector = MAFRabbitMQConnector.createShared(factory, "McNativeMonitoringService", true);
        registerActionListeners();
        this.mafConnector.connect();

        this.crashCheckTask = new CrashCheckTask(this);
        startTasks();
    }

    private void registerActionListeners() {
        this.mafConnector.subscribeAction(ServerStartupAction.class, new ServerStartupActionListener(this));
        this.mafConnector.subscribeAction(ServerShutdownAction.class, new ServerShutdownActionListener(this));
        this.mafConnector.subscribeAction(ClientDisconnectAction.class, new ClientDisconnectActionListener(this));
        this.mafConnector.subscribeAction(ClientConnectAction.class, new ClientConnectActionListener(this));
        this.mafConnector.subscribeAction(ServerStatusAction.class, new ServerStatusActionListener(this));
        this.mafConnector.subscribeAction(ServerInfoAction.class, new ServerInfoActionListener(this));
    }

    protected void stop() {
        this.crashCheckTask.interrupt();
        this.mafConnector.disconnect();
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public PretronicLogger getLogger() {
        return logger;
    }

    public MAFRabbitMQConnector getMafConnector() {
        return mafConnector;
    }

    public void logIncomingAction(MAFActionExecutor executor, MAFAction action) {
        getLogger().info("Received "+action.getNamespace()+"@"+action.getName()+" from "
                + executor.getNetworkId().toString() + "@" + executor.getClientId().toString() + " " +
                DocumentFileType.JSON.getWriter().write(Document.newDocument(action), false));
    }

    private void startTasks() {
        this.crashCheckTask.start();
    }
}
