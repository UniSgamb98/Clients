package com.example.clients.core.database;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class DiscoveryServer implements Runnable, AutoCloseable {

    private static final String DISCOVER_MESSAGE = "CLIZR_DISCOVER";
    private static final String HOST_RESPONSE_PREFIX = "CLIZR_HOST";

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
            this.socket = datagramSocket;

            byte[] buffer = new byte[512];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);

                String message = new String(
                        packet.getData(),
                        0,
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                if (DISCOVER_MESSAGE.equals(message)) {
                    String response = HOST_RESPONSE_PREFIX + ";port=" + dbPort;
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket responsePacket = new DatagramPacket(
                            responseBytes,
                            responseBytes.length,
                            packet.getAddress(),
                            packet.getPort()
                    );

                    datagramSocket.send(responsePacket);
                }
            }

        } catch (SocketException e) {
            if (running) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        running = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}