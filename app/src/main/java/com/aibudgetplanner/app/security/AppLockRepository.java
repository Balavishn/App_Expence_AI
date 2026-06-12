package com.aibudgetplanner.app.security;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class AppLockRepository {
    private final SharedPreferences preferences;
    private final String pinHashKey = "pin_hash";
    private final String lockEnabledKey = "lock_enabled";

    @Inject
    public AppLockRepository(@ApplicationContext Context context) {
        this.preferences = context.getSharedPreferences("app_lock_preferences", Context.MODE_PRIVATE);
    }

    public boolean hasPin() {
        String value = preferences.getString(pinHashKey, "");
        return value != null && !value.isBlank();
    }

    public boolean isLockEnabled() {
        return preferences.getBoolean(lockEnabledKey, true);
    }

    public void savePin(String pin) {
        preferences.edit()
                .putString(pinHashKey, hash(pin))
                .putBoolean(lockEnabledKey, true)
                .apply();
    }

    public boolean verifyPin(String pin) {
        String value = preferences.getString(pinHashKey, "");
        return value != null && !value.isBlank() && value.equals(hash(pin));
    }

    public void setLockEnabled(boolean enabled) {
        preferences.edit().putBoolean(lockEnabledKey, enabled).apply();
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash PIN", exception);
        }
    }
}
