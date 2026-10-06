package es.ua.sd.shared;

public class Protocol {
    private Protocol(){};
    public static final byte STX=0x02;
    public static final byte ETX=0x03;
    public static final byte EOT=0x04;
    public static final byte ENQ=0x05;
    public static final byte ACK=0x06;
    public static final byte NACK=0x15;
    public static final char SEPARATOR='#';

}
