package org.mcnative.service.monitoring;

import java.util.UUID;

public class ServerInfo {

    private final UUID id;
    private final ServerStatus status;
    private final long lastStatusUpdate;

    public ServerInfo(UUID id, ServerStatus status, long lastStatusUpdate) {
        this.id = id;
        this.status = status;
        this.lastStatusUpdate = lastStatusUpdate;
    }

    public UUID getId() {
        return id;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public long getLastStatusUpdate() {
        return lastStatusUpdate;
    }
}
