package org.mcnative.service.monitoring.util;

import io.github.cdimascio.dotenv.Dotenv;

public class Environment {

    public static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    public static String getVariable(String name) {
        String value = getVariableOrNull(name);
        if(value == null) throw new IllegalArgumentException("Can't load environment variable " + name);
        return value;
    }

    public static String getVariableOrNull(String name) {
        if (System.getenv(name) != null) {
            return System.getenv(name);
        } else if (DOTENV.get(name) != null) {
            return DOTENV.get(name);
        }
        return null;
    }

}
