package com.aibudgetplanner.app;

import android.app.Application;

import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import com.aibudgetplanner.app.data.sync.SyncScheduler;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AIBudgetApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Inject
    SyncScheduler syncScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        syncScheduler.schedulePeriodicSync();
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}
