package com.aibudgetplanner.app.di;

import android.content.Context;

import androidx.room.Room;

import com.aibudgetplanner.app.ai.SpendingPredictor;
import com.aibudgetplanner.app.ai.TFLiteSpendingPredictor;
import com.aibudgetplanner.app.data.local.AppDatabase;
import com.aibudgetplanner.app.data.local.dao.ExpenseDao;
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao;
import com.aibudgetplanner.app.data.local.dao.PendingSyncOperationDao;
import com.aibudgetplanner.app.data.local.dao.UserProfileDao;
import com.aibudgetplanner.app.data.repository.BudgetRepository;
import com.aibudgetplanner.app.data.repository.PendingSyncRepository;
import com.aibudgetplanner.app.data.sync.SyncScheduler;
import com.aibudgetplanner.app.domain.usecase.BudgetEngineUseCase;
import com.aibudgetplanner.app.security.DatabasePassphraseProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import net.sqlcipher.database.SupportFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class AppModule {
    private AppModule() {
    }

    @Provides
    @Singleton
    static AppDatabase provideDatabase(
            @ApplicationContext Context context,
            DatabasePassphraseProvider databasePassphraseProvider
    ) {
        byte[] passphrase = databasePassphraseProvider.providePassphrase();
        SupportFactory factory = new SupportFactory(passphrase);
        return Room.databaseBuilder(context, AppDatabase.class, "ai_budget_planner.db")
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    static FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    static UserProfileDao provideUserProfileDao(AppDatabase database) {
        return database.userProfileDao();
    }

    @Provides
    static FixedExpenseDao provideFixedExpenseDao(AppDatabase database) {
        return database.fixedExpenseDao();
    }

    @Provides
    static ExpenseDao provideExpenseDao(AppDatabase database) {
        return database.expenseDao();
    }

    @Provides
    static PendingSyncOperationDao providePendingSyncOperationDao(AppDatabase database) {
        return database.pendingSyncOperationDao();
    }

    @Provides
    @Singleton
    static SpendingPredictor provideSpendingPredictor(TFLiteSpendingPredictor predictor) {
        return predictor;
    }

    @Provides
    @Singleton
    static BudgetRepository provideBudgetRepository(
            UserProfileDao userProfileDao,
            FixedExpenseDao fixedExpenseDao,
            ExpenseDao expenseDao,
            BudgetEngineUseCase budgetEngineUseCase,
            PendingSyncRepository pendingSyncRepository,
            SyncScheduler syncScheduler
    ) {
        return new BudgetRepository(
                userProfileDao,
                fixedExpenseDao,
                expenseDao,
                budgetEngineUseCase,
                pendingSyncRepository,
                syncScheduler
        );
    }
}
