
package es.ua.sd.monitor;

public class WM_WS_M {

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println(
                "Uso: WM_WS_M <puertoMonitor> <ipCentral> <puertoCentral> <id_WS>"
            );
            return;
        }

        int puertoMonitor;
        String ipCentral = args[1];
        int puertoCentral;
        String idWS = args[3];

        try {
            puertoMonitor = Integer.parseInt(args[0]);
            puertoCentral = Integer.parseInt(args[2]);

            if (puertoMonitor < 1 || puertoMonitor > 65535
                    || puertoCentral < 1 || puertoCentral > 65535) {
                System.out.println(
                    "Los puertos deben estar entre 1 y 65535"
                );
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Los puertos deben ser números enteros");
            return;
        }

        System.out.println("Puerto del Monitor: " + puertoMonitor);
        System.out.println("IP de CENTRAL: " + ipCentral);
        System.out.println("Puerto de CENTRAL: " + puertoCentral);
        System.out.println("ID de WS: " + idWS);

        // TODO: arrancar el servidor de sockets para WM_WS_E.
        // TODO: conectar con CENTRAL y autenticar la estación.
    }
}
