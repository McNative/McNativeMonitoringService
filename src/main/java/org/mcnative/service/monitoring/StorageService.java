package org.mcnative.service.monitoring;

import net.pretronic.databasequery.api.Database;
import net.pretronic.databasequery.api.collection.DatabaseCollection;
import net.pretronic.databasequery.api.driver.DatabaseDriver;
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory;
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig;
import net.pretronic.databasequery.api.query.result.QueryResultEntry;
import net.pretronic.databasequery.api.query.type.UpdateQuery;
import net.pretronic.databasequery.sql.dialect.Dialect;
import net.pretronic.databasequery.sql.driver.config.SQLDatabaseDriverConfigBuilder;
import net.pretronic.libraries.logging.PretronicLogger;
import org.mcnative.actionframework.sdk.common.action.MAFActionExecutor;
import org.mcnative.service.monitoring.util.Environment;

import java.net.InetSocketAddress;
import java.sql.Timestamp;

public class StorageService {

    private final DatabaseDriver databaseDriver;
    private final Database database;

    private final DatabaseCollection networkCollection;
    private final DatabaseCollection serverCollection;
    private final DatabaseCollection serverDatabaseDriversCollection;
    private final DatabaseCollection serverPluginsCollection;
    private final DatabaseCollection serverEventsCollection;

    public StorageService(PretronicLogger logger) {
        DatabaseDriverConfig<?> storageConfiguration = new SQLDatabaseDriverConfigBuilder()
                .setAddress(InetSocketAddress.createUnresolved(Environment.getVariable("DATABASE_HOST"),
                        Integer.parseInt(Environment.getVariable("DATABASE_PORT"))))
                .setDialect(Dialect.byName(Environment.getVariable("DATABASE_DIALECT")))
                .setUsername(Environment.getVariable("DATABASE_USERNAME"))
                .setPassword(Environment.getVariable("DATABASE_PASSWORD"))
                .build();

        this.databaseDriver = DatabaseDriverFactory.create("McNativeMonitoringService", storageConfiguration, logger);
        this.databaseDriver.connect();

        this.database = databaseDriver.getDatabase(Environment.getVariable("DATABASE_NAME"));

        this.networkCollection = database.getCollection("mcnative_network");
        this.serverCollection = database.getCollection("mcnative_server");
        this.serverDatabaseDriversCollection = database.getCollection("mcnative_server_database_drivers");
        this.serverPluginsCollection = database.getCollection("mcnative_server_plugins");
        this.serverEventsCollection = database.getCollection("mcnative_server_events");
    }

    public DatabaseCollection getNetworkCollection() {
        return networkCollection;
    }

    public DatabaseCollection getServerCollection() {
        return serverCollection;
    }

    public DatabaseCollection getServerDatabaseDriversCollection() {
        return serverDatabaseDriversCollection;
    }

    public DatabaseCollection getServerPluginsCollection() {
        return serverPluginsCollection;
    }

    public DatabaseCollection getServerEventsCollection() {
        return serverEventsCollection;
    }

    public void addServerLogEntry(MAFActionExecutor executor, LogAction action) {
        this.serverEventsCollection.insert()
                .set("NetworkId", executor.getNetworkId().toString())
                .set("ServerId", executor.getClientId().toString())
                .set("Time", new Timestamp(System.currentTimeMillis()))
                .set("Action", action)
                .execute();
    }

    public ServerInfo getServerStatus(MAFActionExecutor executor) {
        QueryResultEntry resultEntry = this.serverCollection.find()
                .where("NetworkId", executor.getNetworkId().toString())
                .where("Id", executor.getClientId().toString())
                .execute().firstOrNull();
        if(resultEntry == null) return null;
        return new ServerInfo(executor.getClientId(),
                ServerStatus.valueOf(resultEntry.getString("Status")),
                ((Timestamp)resultEntry.getObject("LastStatusUpdate")).getTime());
    }

    public void updateServerStatus(MAFActionExecutor executor, ServerStatus status) {
        UpdateQuery query = this.serverCollection.update()
                .set("Status", status)
                .set("LastStatusUpdate", new Timestamp(System.currentTimeMillis()));
        if(status == ServerStatus.OFFLINE) {
            query.set("Tps", "0.0,0.0,0.0")
                    .set("UsedMemory", 0)
                    .set("CpuUsage", 0);
        }
        query.where("Id", executor.getClientId().toString())
                .where("NetworkId", executor.getNetworkId().toString())
                .execute();
    }
}