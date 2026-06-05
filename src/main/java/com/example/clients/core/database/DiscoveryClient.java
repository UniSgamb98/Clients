package com.example.clients.core.database;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class DiscoveryClient {

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

            byte[] request = DiscoveryServer.DISCOVER_MESSAGE.getBytes(StandardCharsets.UTF_8);
            sendBroadcasts(socket, request);

            DatagramPacket response = new DatagramPacket(new byte[512], 512);
            socket.receive(response);

            String message = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);

            if (message.startsWith(DiscoveryServer.HOST_RESPONSE_PREFIX)) {
                return Optional.of(new HostInfo(response.getAddress(), parsePort(message)));
            }
        } catch (SocketTimeoutException e) {
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private void sendBroadcasts(DatagramSocket socket, byte[] request) throws Exception {
        Set<String> sentAddresses = new HashSet<>();
        send(socket, request, InetAddress.getByName("255.255.255.255"), sentAddresses);

        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();

                if (broadcast != null) {
                    send(socket, request, broadcast, sentAddresses);
                }
            }
        }
    }

    private void send(DatagramSocket socket, byte[] request, InetAddress address, Set<String> sentAddresses) throws Exception {
        if (!sentAddresses.add(address.getHostAddress())) {
            return;
        }

        socket.send(new DatagramPacket(request, request.length, address, discoveryPort));
    }

    private int parsePort(String message) {
        for (String part : message.split(";")) {
            if (part.startsWith("port=")) {
                return Integer.parseInt(part.substring("port=".length()));
            }
        }

        throw new IllegalArgumentException("Risposta discovery non valida: " + message);
    }
}
