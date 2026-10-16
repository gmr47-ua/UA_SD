package es.ua.sd.engine;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WM_WS_E {

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println(
                "Uso: WM_WS_E <ipBroker> <puertoBroker> "
                + "<ipMonitor> <puertoMonitor>"
            );
            return;
        }

        String brokerHost = args[0];
        int brokerPort;
        String monitorHost = args[2];
        int monitorPort;

        try {
            brokerPort = Integer.parseInt(args[1]);
            monitorPort = Integer.parseInt(args[3]);

            if (brokerPort < 1 || brokerPort > 65535
                    || monitorPort < 1 || monitorPort > 65535) {
                System.out.println("Los puertos deben estar entre 1 y 65535");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Los puertos deben ser números enteros");
            return;
        }

        AtomicBoolean healthy = new AtomicBoolean(true);

        Thread teclado = new Thread(() -> readKeyboard(healthy));
        teclado.setDaemon(true);
        teclado.start();

        // TODO: Inicializar el cliente Kafka usando brokerHost y brokerPort.
        // TODO: Esperar solicitudes de riego procedentes de CENTRAL.

        try (Socket socket = new Socket(monitorHost, monitorPort);
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                 socket.getOutputStream(), true)) {

            System.out.println(
                "Engine conectado al Monitor. Pulsa Enter para simular KO/OK."
            );

            String line;

            while ((line = in.readLine()) != null) {
                if (line.equals("HEALTH")) {
                    out.println(healthy.get() ? "OK" : "KO");
                }
            }

            System.out.println("El Monitor cerró la conexión.");

        } catch (ConnectException e) {
            System.out.println(
                "No se pudo conectar con el Monitor. ¿Está arrancado?"
            );
        } catch (IOException e) {
            System.out.println(
                "Error de comunicación: " + e.getMessage()
            );
        }
    }

    private static void readKeyboard(AtomicBoolean healthy) {
        BufferedReader console =
            new BufferedReader(new InputStreamReader(System.in));

        try {
            while (console.readLine() != null) {
                boolean now = !healthy.get();
                healthy.set(now);

                System.out.println(
                    "Estado del Engine: " + (now ? "OK" : "KO")
                );
            }
        } catch (IOException e) {
            System.out.println(
                "Error leyendo el teclado: " + e.getMessage()
            );
        }
    }
}