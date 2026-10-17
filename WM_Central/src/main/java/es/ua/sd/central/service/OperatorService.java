package es.ua.sd.central.service;
import es.ua.sd.central.db.OperatorDao;
import es.ua.sd.central.model.Operator;
import java.util.Optional;
import es.ua.sd.shared.EmailValidator;
import es.ua.sd.shared.OperatorAlreadyExistsException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;

/**
 * OperatorService
 */
public class OperatorService {

    private OperatorDao operatorDao;

    public OperatorService(OperatorDao operador){
        this.operatorDao = operador;
    }

    /**
    * Comprueba los campos pasados por argumentos, hasea el paswword, inseta el {@link Operator}
    * en la bD y devuelve el {@link Operator}
    * en caso de exito
    * @param ID
    * @param name
    * @param email
    * @param password
    * @return
    * @throws SQLException
 */
    Operator register(String ID,String name, String email, String password) throws SQLException{

        //No se permiten campos vacios
        if(ID.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()){
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }

        //Nombre demasiado corto
        if (name.length() < 5) {
            throw new IllegalArgumentException("Nombre tiene que ser mínimo 5 carecteres");
        }

        //Comprobar si la contaseña cumple requerimientos basicos
        // Mínimo 8 chars, al menos 1 dígito, 1 minúscula, 1 mayúscula
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        boolean valido = password.matches(regex);
        if (!valido) {
            throw new IllegalArgumentException("La contraseña debe de tener como mínimo 8 caracteres, al menos 1 dígito, 1 minúscula, 1 mayúscula");
        }

        //Comprobar si el formato del email es válido
        if (!EmailValidator.isValid(email)) {
            throw new IllegalArgumentException("Introduzca un formato de email valido");
        }

        //Comprobamos si ya existe el email
        if(operatorDao.findByEmail(email).isPresent()){
            throw new OperatorAlreadyExistsException("Ya existe este operario en el sistema");
        }

        //Comprobamos si ya existe el ID
        if (operatorDao.findByID(Integer.parseInt(ID)).isPresent()) {
            throw new OperatorAlreadyExistsException("Ya existe este operario en el sistema");
        }

        //Haseamos el email
        String passwordHash = hash(password);
        //Insertamos el nuevo Operator en uestra tabla
        Operator operador = new Operator(Integer.parseInt(ID), name, email, passwordHash);
        operatorDao.insert(name, email, passwordHash);
        return operador;

    }

    /**
     * Comprueba las credenciales de un operario.
     * @return el operario si el email existe y la contraseña es correcta; vacío en cualquier otro caso
     */
    public Optional<Operator> authenticate(String email, String password) throws SQLException{
        if (email == null || password == null) {
            return Optional.empty();
        }

        Optional<Operator> found = operatorDao.findByEmail(email.trim().toLowerCase());   // ADAPTAR
        if (found.isEmpty()) {
            return Optional.empty();
        }

        Operator operator = found.get();
        return verify(password, operator.passwordHash()) ? found : Optional.empty();       // ADAPTAR
    }

    /**
     * Busca un operario por su ID.
     * @return el operario, o vacío si el ID es nulo, está en blanco o no existe
     */
    public Optional<Operator> findById(String id) throws SQLException{
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return operatorDao.findByID(Integer.parseInt(id));   // ADAPTAR
    }

    /** Atajo para validaciones: ¿existe un operario con este ID? */
    public boolean exists(String id) throws SQLException{
        return findById(id).isPresent();
    }

    // parte privada sobre hasear la contraseña
        private static final int SALT_BYTES = 16;
        private static final int ITERATIONS = 120_000;
        private static final int KEY_BITS = 256;
        private static final SecureRandom random = new SecureRandom();

        /**
        * Verificar si la contraseña pasada y la almacenada en la BD son iguales
        * @param password
        * @param storedHash
        * @return
        */
        private boolean verify(String password, String storedHash) {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;   // formato corrupto: se trata como credenciales inválidas
            }

            try {
                int iterations = Integer.parseInt(parts[0]);
                byte[] salt = Base64.getDecoder().decode(parts[1]);
                byte[] expected = Base64.getDecoder().decode(parts[2]);

                byte[] actual = derive(password, salt, iterations, expected.length * 8);
                return MessageDigest.isEqual(expected, actual);
            } catch (IllegalArgumentException e) {   // incluye NumberFormatException y Base64 inválido
                return false;
            }
        }

        /**
        * Modulo encargado de encriptar la contraseña
        * @param password
        * @param salt
        * @param iterations
        * @param keyBits
        * @return
        */
        private byte[] derive(String password, byte[] salt, int iterations, int keyBits) {
            try {
                PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec).getEncoded();
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
            }
        }

        /**
        * Modulo que hasea la contraseña para almacenarla en la BD
        * @param password
        * @return
        */
        private String hash(String password) {
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);
            byte[] hash = derive(password, salt, ITERATIONS, KEY_BITS);
            return ITERATIONS + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);
        }
}
