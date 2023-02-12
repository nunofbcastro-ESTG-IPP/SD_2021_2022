package estg.ipp.pt.utlis;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Constants;
import de.mkammerer.argon2.Argon2Factory;
import estg.ipp.pt.env.MyEnv;

public class Password {
    private static final int iterations = Convert.StringToInt(MyEnv.get("ITERATIONS"),10);
    private static final int memory = Convert.StringToInt(MyEnv.get("MEMORY"),65536);
    private static final int parallelism = Convert.StringToInt(MyEnv.get("PARALLELISM"),1);
    private static final int defaultSaltLength = Convert.StringToInt(MyEnv.get("SALT_LENGTH"),Argon2Constants.DEFAULT_SALT_LENGTH);
    private static final int defaultHashLength = Convert.StringToInt(MyEnv.get("HASH_LENGTH"), Argon2Constants.DEFAULT_HASH_LENGTH);
    private static final Argon2 argon2 = Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2id, defaultSaltLength, defaultHashLength);

    public static boolean VerifyPassword(String hashedPassword, String password){
        return argon2.verify(hashedPassword, password);
    }

    public static String HashPassword(String password){
        String hash;

        try {
            // Hash password
            hash = argon2.hash(iterations, memory, parallelism, password);
        } finally {
            // Wipe confidential data
            argon2.wipeArray(password.toCharArray());
        }

        return hash;
    }
}

