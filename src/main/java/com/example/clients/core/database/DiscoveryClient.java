package com.example.clients.core.database;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DiscoveryClient {

    private static final String DISCOVER_MESSAGE = "CLIZR_DISCOVER";
    private static final String HOST_RESPONSE_PREFIX = "CLIZR_HOST";

    private final int discoveryPort;
    private final int timeoutMs;

    public DiscoveryClient(int discoveryPort, int timeoutMs) {
        this.discoveryPort = discoveryPort;
        this.timeoutMs = timeoutMs;
    }

    public Optional<HostInfo> findHost() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);

            byte[] requestBytes = DISCOVER_MESSAGE.getBytes(StandardCharsets.UTF_8);

            sendBroadcastRequests(socket, requestBytes);

            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);

            socket.receive(response);

            String message = new String(
                    response.getData(),
                    0,
                    response.getLength(),
                    StandardCharsets.UTF_8
            );

            if (message.startsWith(HOST_RESPONSE_PREFIX)) {
                int dbPort = parsePort(message);
                return Optional.of(new HostInfo(response.getAddress(), dbPort));
            }

        } catch (SocketTimeoutException e) {
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private void sendBroadcastRequests(DatagramSocket socket, byte[] requestBytes) throws Exception {
        Set<String> alreadySent = new HashSet<>();

        sendToBroadcastAddress(socket, requestBytes, InetAddress.getByName("255.255.255.255"), alreadySent);

        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();

                if (broadcast == null) {
                    continue;
                }

                sendToBroadcastAddress(socket, requestBytes, broadcast, alreadySent);
            }
        }
    }

    private void sendToBroadcastAddress(
            DatagramSocket socket,
            byte[] requestBytes,
            InetAddress broadcastAddress,
            Set<String> alreadySent
    ) throws Exception {
        String ip = broadcastAddress.getHostAddress();

        if (!alreadySent.add(ip)) {
            return;
        }

        DatagramPacket packet = new DatagramPacket(
                requestBytes,
                requestBytes.length,
                broadcastAddress,
                discoveryPort
        );

        socket.send(packet);
    }

    private int parsePort(String message) {
        String[] parts = message.split(";");

        for (String part : parts) {
            if (part.startsWith("port=")) {
                return Integer.parseInt(part.substring("port=".length()));
            }
        }

        throw new IllegalArgumentException("Risposta discovery non valida: " + message);
    }
}