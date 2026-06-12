package com.aibudgetplanner.app.sms;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SmsDedupStore {
    private final SharedPreferences preferences;
    private final String key = "recent_hashes_serialized";
    private final String delimiter = "|";
    private final String delimiterRegex = "\\|";
    private final int maxEntries = 300;

    @Inject
    public SmsDedupStore(@ApplicationContext Context context) {
        this.preferences = context.getSharedPreferences("sms_dedup_store", Context.MODE_PRIVATE);
    }

    public synchronized boolean contains(String hash) {
        return currentHashes().contains(hash);
    }

    public synchronized void add(String hash) {
        List<String> updated = new ArrayList<>(currentHashes());
        updated.remove(hash);
        updated.add(hash);

        if (updated.size() > maxEntries) {
            int removeCount = updated.size() - maxEntries;
            for (int i = 0; i < removeCount; i++) {
                updated.remove(0);
            }
        }

        preferences.edit().putString(key, String.join(delimiter, updated)).apply();
    }

    private List<String> currentHashes() {
        String raw = preferences.getString(key, "");
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        String[] parts = raw.split(delimiterRegex);
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                result.add(part);
            }
        }
        return result;
    }
}
