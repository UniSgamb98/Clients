package com.example.clients.core.database;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public final class DiscoveryServer implements Runnable, AutoCloseable {

    static final String DISCOVER_MESSAGE = "CLIZR_DISCOVER";
    static final String HOST_RESPONSE_PREFIX = "CLIZR_HOST";

    private final int discoveryPort;
    private final int dbPort;
    private volatile boolean running = true;
    private DatagramSocket socket;

    public DiscoveryServer(int discoveryPort, int dbPort) {
        this.discoveryPort = discoveryPort;
        this.dbPort = dbPort;
    }

    @Override
    public void run() {
        try (DatagramSocket datagramSocket = new DatagramSocket(discoveryPort, InetAddress.getByName("0.0.0.0"))) {
            socket = datagramSocket;
            byte[] buffer = new byte[512];

            while (running) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(request);

                String message = new String(request.getData(), 0, request.getLength(), StandardCharsets.UTF_8);

                if (DISCOVER_MESSAGE.equals(message)) {
                    sendResponse(datagramSocket, request);
                }
            }
        } catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(DatagramSocket datagramSocket, DatagramPacket request) throws Exception {
        byte[] response = (HOST_RESPONSE_PREFIX + ";port=" + dbPort).getBytes(StandardCharsets.UTF_8);
        DatagramPacket responsePacket = new DatagramPacket(
                response,
                response.length,
                request.getAddress(),
                request.getPort()
        );
        datagramSocket.send(responsePacket);
    }

    @Override
    public void close() {
        running = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
