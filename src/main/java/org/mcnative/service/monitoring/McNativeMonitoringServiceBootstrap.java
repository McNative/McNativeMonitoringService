package org.mcnative.service.monitoring;

import io.sentry.Sentry;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.logging.PretronicLoggerFactory;
import net.pretronic.libraries.logging.bridge.slf4j.SLF4JStaticBridge;
import net.pretronic.libraries.logging.io.LoggingPrintStream;
import org.mcnative.service.monitoring.util.Environment;

public final class McNativeMonitoringServiceBootstrap {

    public static void main(String[] args) {
        boolean development = Environment.getVariable("ENVIRONMENT","development").equalsIgnoreCase("development");

        PretronicLogger logger = PretronicLoggerFactory.getLogger("McNativeMonitoringService");
        SLF4JStaticBridge.setLogger(logger);
        LoggingPrintStream.hook(logger);

        String dsn = Environment.getVariableOrNull("SENTRY_DSN");
        if(dsn != null && !development){
            Sentry.init(options -> options.setDsn(dsn));
        }

        McNativeMonitoringService service = new McNativeMonitoringService(logger);
        Runtime.getRuntime().addShutdownHook(new Thread(service::stop));
    }
}
