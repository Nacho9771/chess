package service;

import java.util.UUID;

public final class TokenUtil {

    private TokenUtil() {}

    // UUID-based tokens are sufficient for simple session auth.
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
