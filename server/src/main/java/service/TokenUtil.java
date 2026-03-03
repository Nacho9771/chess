package service;

import java.util.UUID;

public final class TokenUtil {
    private TokenUtil() {
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
