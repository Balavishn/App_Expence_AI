package com.aibudgetplanner.app.data.sync;

public final class SyncOperationType {
    public static final String UPSERT_PROFILE = "UPSERT_PROFILE";
    public static final String UPSERT_EXPENSE = "UPSERT_EXPENSE";
    public static final String DELETE_EXPENSE = "DELETE_EXPENSE";
    public static final String UPSERT_FIXED_EXPENSE = "UPSERT_FIXED_EXPENSE";
    public static final String DELETE_FIXED_EXPENSE = "DELETE_FIXED_EXPENSE";

    private SyncOperationType() {
    }
}
