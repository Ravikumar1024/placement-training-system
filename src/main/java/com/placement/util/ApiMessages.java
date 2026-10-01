package com.placement.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class ApiMessages {
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages", Locale.getDefault());

    private ApiMessages() {}

    public static String get(String key, Object... arguments) {
        return MessageFormat.format(MESSAGES.getString(key), arguments);
    }
}
