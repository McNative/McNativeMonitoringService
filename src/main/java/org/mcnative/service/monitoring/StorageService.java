package org.mcnative.service.monitoring;

import net.pretronic.databasequery.api.Database;
import net.pretronic.databasequery.api.collection.DatabaseCollection;
import net.pretronic.databasequery.api.driver.DatabaseDriver;
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory;
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig;
import net.pretronic.databasequery.sql.dialect.Dialect;
import net.pretronic.databasequery.sql.driver.config.SQLDatabaseDriverConfigBuilder;
import org.mcnative.service.monitoring.util.Environment;

import java.net.InetSocketAddress;

public class StorageService {

    private final DatabaseDriver databaseDriver;
    private final Database database;

    private final DatabaseCollection networkCollection;
    private final DatabaseCollection serverCollection;
    private final DatabaseCollection serverDatabaseDriversCollection;
    private final DatabaseCollection serverPluginsCollection;

    public StorageService() {
        DatabaseDriverConfig<?> storageConfiguration = new SQLDatabaseDriverConfigBuilder()
                .setAddress(InetSocketAddress.createUnresolved(Environment.getVariable("DATABASE_HOST"),
                        Integer.parseInt(Environment.getVariable("DATABASE_PORT"))))
                .setDialect(Dialect.byName(Environment.getVariable("DATABASE_DIALECT")))
                .setUsername(Environment.getVariable("DATABASE_USERNAME"))
                .setPassword(Environment.getVariable("DATABASE_PASSWORD"))
                .build();

        this.databaseDriver = DatabaseDriverFactory.create("McNativeUsageAnalyser", storageConfiguration);
        this.databaseDriver.connect();

        this.database = databaseDriver.getDatabase(Environment.getVariable("DATABASE_NAME"));

        this.networkCollection = database.getCollection("mcnative_network");
        this.serverCollection = database.getCollection("mcnative_server");
        this.serverDatabaseDriversCollection = database.getCollection("mcnative_server_database_drivers");
        this.serverPluginsCollection = database.getCollection("mcnative_server_plugins");
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


}