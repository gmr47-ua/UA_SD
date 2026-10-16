
package es.ua.sd.shared;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class Frame {

    private Frame() {
    }

    // Tamaño máximo permitido para DATA.
    private static final int MAX_PAYLOAD_LENGTH = 4096;

    /**
     * Convierte un mensaje de texto en una trama:
     * STX + DATA + ETX + LRC.
     */
    public static byte[] encode(String data) {
        if (data == null) {
            throw new IllegalArgumentException("El mensaje no puede ser null");
        }

        byte[] payload = data.getBytes(StandardCharsets.UTF_8);

        if (payload.length > MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException("El mensaje supera el tamaño máximo permitido");
        }

        byte[] frame = new byte[payload.length + 3];

        // Primer byte: inicio de trama.
        frame[0] = Protocol.STX;

        // Copiar DATA a continuación de STX.
        System.arraycopy(payload, 0, frame, 1, payload.length);

        // ETX se coloca después de DATA.
        frame[payload.length + 1] = Protocol.ETX;

        // LRC se calcula sobre los bytes de DATA.
        frame[payload.length + 2] = lrc(payload);

        return frame;
    }

    /**
     * Calcula el LRC mediante XOR de todos los bytes de DATA.
     */
    public static byte lrc(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Los datos no pueden ser null");
        }

        byte result = 0;

        for (byte b : data) {
            result ^= b;
        }

        return result;
    }

    /**
     * Valida una trama completa y devuelve su DATA como texto.
     */
    public static String decode(byte[] frame)
            throws InvalidFrameException {

        if (frame == null || frame.length < 3) {
            throw new InvalidFrameException(
                    "Trama demasiado corta");
        }

        if (frame[0] != Protocol.STX) {
            throw new InvalidFrameException(
                    "Falta STX al principio de la trama");
        }

        int etxIndex = frame.length - 2;

        if (frame[etxIndex] != Protocol.ETX) {
            throw new InvalidFrameException(
                    "Falta ETX antes del LRC");
        }

        int payloadLength = etxIndex - 1;

        if (payloadLength > MAX_PAYLOAD_LENGTH) {
            throw new InvalidFrameException(
                    "DATA supera el tamaño máximo permitido");
        }

        byte[] payload = Arrays.copyOfRange(
                frame, 1, etxIndex);

        byte receivedLrc = frame[frame.length - 1];
        byte calculatedLrc = lrc(payload);

        if (receivedLrc != calculatedLrc) {
            throw new InvalidFrameException(
                    "LRC incorrecto: la trama está dañada");
        }

        return new String(payload, StandardCharsets.UTF_8);
    }

    /**
     * Lee una trama completa de un InputStream.
     *
     * Devuelve null si se alcanza el final del flujo antes
     * de encontrar otra trama.
     *
     * Lanza EOFException si la conexión termina a mitad
     * de una trama.
     */
    public static String readFrom(InputStream in)
            throws IOException, InvalidFrameException {

        if (in == null) {
            throw new IllegalArgumentException(
                    "El InputStream no puede ser null");
        }

        int value;

        // Buscar STX, ignorando cualquier byte anterior.
        do {
            value = in.read();

            if (value == -1) {
                return null;
            }
        } while (value != (Protocol.STX & 0xFF));

        ByteArrayOutputStream payload =
                new ByteArrayOutputStream();

        // Leer DATA hasta encontrar ETX.
        while (true) {
            value = in.read();

            if (value == -1) {
                throw new EOFException(
                        "La conexión terminó antes de ETX");
            }

            if (value == (Protocol.ETX & 0xFF)) {
                break;
            }

            payload.write(value);

            if (payload.size() > MAX_PAYLOAD_LENGTH) {
                throw new InvalidFrameException(
                        "DATA supera el tamaño máximo permitido");
            }
        }

        // El byte inmediatamente posterior a ETX es el LRC.
        int receivedLrc = in.read();

        if (receivedLrc == -1) {
            throw new EOFException(
                    "La conexión terminó antes del LRC");
        }

        byte[] data = payload.toByteArray();

        // Reconstruir la trama completa para reutilizar decode().
        byte[] frame = new byte[data.length + 3];
        frame[0] = Protocol.STX;

        System.arraycopy(data, 0, frame, 1, data.length);

        frame[data.length + 1] = Protocol.ETX;
        frame[data.length + 2] = (byte) receivedLrc;

        return decode(frame);
    }
}
