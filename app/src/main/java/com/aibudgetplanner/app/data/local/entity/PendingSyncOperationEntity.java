package com.aibudgetplanner.app.data.local.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_sync_operation")
public class PendingSyncOperationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String operationType;
    public String payloadJson;
    public long createdAt;
    public int retryCount;
    @Nullable
    public String lastError;

    public PendingSyncOperationEntity(
            long id,
            String operationType,
            String payloadJson,
            long createdAt,
            int retryCount,
            @Nullable String lastError
    ) {
        this.id = id;
        this.operationType = operationType;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
        this.retryCount = retryCount;
        this.lastError = lastError;
    }
}
