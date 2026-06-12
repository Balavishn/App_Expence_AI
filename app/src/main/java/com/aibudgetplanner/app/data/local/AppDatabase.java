package com.aibudgetplanner.app.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.aibudgetplanner.app.data.local.dao.ExpenseDao;
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao;
import com.aibudgetplanner.app.data.local.dao.PendingSyncOperationDao;
import com.aibudgetplanner.app.data.local.dao.UserProfileDao;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.PendingSyncOperationEntity;
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;

@Database(
        entities = {
                UserProfileEntity.class,
                FixedExpenseEntity.class,
                ExpenseEntity.class,
                PendingSyncOperationEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserProfileDao userProfileDao();

    public abstract FixedExpenseDao fixedExpenseDao();

    public abstract ExpenseDao expenseDao();

    public abstract PendingSyncOperationDao pendingSyncOperationDao();
}
