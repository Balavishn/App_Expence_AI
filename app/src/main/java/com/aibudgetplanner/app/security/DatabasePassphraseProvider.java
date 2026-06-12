package com.aibudgetplanner.app.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.SecureRandom;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import net.sqlcipher.database.SQLiteDatabase;

@Singleton
public class DatabasePassphraseProvider {
    private final Context context;
    private final String keyName = "db_passphrase";

    @Inject
    public DatabasePassphraseProvider(@ApplicationContext Context context) {
        this.context = context;
    }

    public byte[] providePassphrase() {
        SharedPreferences sharedPreferences = securePreferences();
        String encoded = sharedPreferences.getString(keyName, null);
        if (encoded == null) {
            encoded = generateAndStorePassphrase(sharedPreferences);
        }

        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        String passphraseText = Base64.encodeToString(decoded, Base64.NO_WRAP);
        return SQLiteDatabase.getBytes(passphraseText.toCharArray());
    }

    private String generateAndStorePassphrase(SharedPreferences sharedPreferences) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String encoded = Base64.encodeToString(randomBytes, Base64.NO_WRAP);
        sharedPreferences.edit().putString(keyName, encoded).apply();
        return encoded;
    }

    private SharedPreferences securePreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    "secure_storage",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize secure preferences", exception);
        }
    }
}
