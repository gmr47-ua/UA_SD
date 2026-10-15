package es.ua.sd.central;
import java.io.*;
import java.net.*;



public class Main {

    public static void main(String[] args) throws IOException{

        //Validar los argumentos
        if (args.length != 3) {
            System.out.println("Error cantidad incorrecta de argumentos.");
            System.out.println("Ejemplo de uso:");
            System.out.println("./WM_Central puerto_escucha ip_broker puerto_broker");
            System.exit(1);
        }

        //Primero creamos el socket que va a conectar con los monitores
        int port = Integer.parseInt(args[0]);
        ServerSocket servidor = new ServerSocket(port);

        //Despues vamos a crear el Sring para conectar con el bootstrap server de kafka
        String BOOTSTRAPSERVER = new String(args[1] + ":" + args[2]);


        //Despues vamos a crear la conexion con la BD




    }
}
