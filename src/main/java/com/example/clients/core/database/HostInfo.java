package com.example.clients.core.database;

import java.net.InetAddress;

public class HostInfo {

    private final InetAddress address;
    private final int dbPort;

    public HostInfo(InetAddress address, int dbPort) {
        this.address = address;
        this.dbPort = dbPort;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getIp() {
        return address.getHostAddress();
    }
}