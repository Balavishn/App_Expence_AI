package com.aibudgetplanner.app.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aibudgetplanner.app.data.repository.FirebaseSyncManager;
import com.aibudgetplanner.app.data.repository.PendingSyncRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class SyncWorker extends Worker {
    private final FirebaseSyncManager firebaseSyncManager;
    private final PendingSyncRepository pendingSyncRepository;

    @AssistedInject
    public SyncWorker(
            @Assisted @NonNull Context appContext,
            @Assisted @NonNull WorkerParameters workerParams,
            FirebaseSyncManager firebaseSyncManager,
            PendingSyncRepository pendingSyncRepository
    ) {
        super(appContext, workerParams);
        this.firebaseSyncManager = firebaseSyncManager;
        this.pendingSyncRepository = pendingSyncRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            pendingSyncRepository.replayPending(firebaseSyncManager);
            firebaseSyncManager.sync("local-user");
            return Result.success();
        } catch (Exception ignored) {
            return Result.retry();
        }
    }
}
