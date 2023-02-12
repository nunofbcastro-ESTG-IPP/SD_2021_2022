package estg.ipp.pt.env;

import io.github.cdimascio.dotenv.Dotenv;

public class MyEnv {
    private static Dotenv dotenv;

    static {
        dotenv = Dotenv.configure()
                .filename(".env")
                .load();
    }

    public static String get(String key) {
        return dotenv.get(key);
    }
}

