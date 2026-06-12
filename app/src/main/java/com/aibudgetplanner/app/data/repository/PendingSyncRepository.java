package com.aibudgetplanner.app.data.repository;

import com.aibudgetplanner.app.data.local.dao.PendingSyncOperationDao;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.PendingSyncOperationEntity;
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;
import com.aibudgetplanner.app.data.sync.SyncOperationType;

import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PendingSyncRepository {
    private final PendingSyncOperationDao pendingSyncOperationDao;

    @Inject
    public PendingSyncRepository(PendingSyncOperationDao pendingSyncOperationDao) {
        this.pendingSyncOperationDao = pendingSyncOperationDao;
    }

    public void enqueueProfileUpsert(UserProfileEntity profile) {
        enqueue(
                SyncOperationType.UPSERT_PROFILE,
                new JSONObject()
                        .put("userId", profile.userId)
                        .put("salary", profile.salary)
                        .put("salaryDate", profile.salaryDate)
                        .put("monthlySavingsGoal", profile.monthlySavingsGoal)
                        .put("currency", profile.currency)
                        .put("createdDate", profile.createdDate)
                        .put("financialGoals", profile.financialGoals)
                        .put("updatedAt", profile.updatedAt)
                        .toString()
        );
    }

    public void enqueueExpenseUpsert(ExpenseEntity expense) {
        enqueue(
                SyncOperationType.UPSERT_EXPENSE,
                new JSONObject()
                        .put("expenseId", expense.expenseId)
                        .put("userId", expense.userId)
                        .put("date", expense.date)
                        .put("category", expense.category)
                        .put("amount", expense.amount)
                        .put("description", expense.description)
                        .put("paymentMethod", expense.paymentMethod)
                        .put("updatedAt", expense.updatedAt)
                        .toString()
        );
    }

    public void enqueueExpenseDelete(ExpenseEntity expense) {
        enqueue(
                SyncOperationType.DELETE_EXPENSE,
                new JSONObject()
                        .put("expenseId", expense.expenseId)
                        .put("userId", expense.userId)
                        .toString()
        );
    }

    public void enqueueFixedExpenseUpsert(FixedExpenseEntity expense) {
        enqueue(
                SyncOperationType.UPSERT_FIXED_EXPENSE,
                new JSONObject()
                        .put("expenseId", expense.expenseId)
                        .put("userId", expense.userId)
                        .put("name", expense.name)
                        .put("category", expense.category)
                        .put("amount", expense.amount)
                        .put("dueDate", expense.dueDate)
                        .put("isRecurring", expense.isRecurring)
                        .put("updatedAt", expense.updatedAt)
                        .toString()
        );
    }

    public void enqueueFixedExpenseDelete(FixedExpenseEntity expense) {
        enqueue(
                SyncOperationType.DELETE_FIXED_EXPENSE,
                new JSONObject()
                        .put("expenseId", expense.expenseId)
                        .put("userId", expense.userId)
                        .toString()
        );
    }

    public int replayPending(FirebaseSyncManager firebaseSyncManager) {
        return replayPending(firebaseSyncManager, 200);
    }

    public int replayPending(FirebaseSyncManager firebaseSyncManager, int batchSize) {
        List<PendingSyncOperationEntity> pending = pendingSyncOperationDao.getPending(batchSize);
        int replayed = 0;
        for (PendingSyncOperationEntity operation : pending) {
            try {
                replayOperation(firebaseSyncManager, operation);
                pendingSyncOperationDao.deleteById(operation.id);
                replayed += 1;
            } catch (Exception ignored) {
                pendingSyncOperationDao.update(
                        new PendingSyncOperationEntity(
                                operation.id,
                                operation.operationType,
                                operation.payloadJson,
                                operation.createdAt,
                                operation.retryCount + 1,
                                "Replay failed"
                        )
                );
            }
        }
        return replayed;
    }

    private void replayOperation(FirebaseSyncManager firebaseSyncManager, PendingSyncOperationEntity operation)
            throws Exception {
        JSONObject payload = new JSONObject(operation.payloadJson);
        switch (operation.operationType) {
            case SyncOperationType.UPSERT_PROFILE:
                firebaseSyncManager.upsertRemoteProfile(
                        payload.getString("userId"),
                        new UserProfileEntity(
                                payload.getString("userId"),
                                payload.getDouble("salary"),
                                payload.getInt("salaryDate"),
                                payload.getDouble("monthlySavingsGoal"),
                                payload.getString("currency"),
                                payload.getLong("createdDate"),
                                payload.optString("financialGoals"),
                                payload.getLong("updatedAt")
                        )
                );
                break;
            case SyncOperationType.UPSERT_EXPENSE:
                firebaseSyncManager.upsertRemoteExpense(
                        payload.getString("userId"),
                        new ExpenseEntity(
                                payload.getLong("expenseId"),
                                payload.getString("userId"),
                                payload.getLong("date"),
                                payload.getString("category"),
                                payload.getDouble("amount"),
                                payload.getString("description"),
                                payload.getString("paymentMethod"),
                                payload.getLong("updatedAt")
                        )
                );
                break;
            case SyncOperationType.DELETE_EXPENSE:
                firebaseSyncManager.deleteRemoteExpense(
                        payload.getString("userId"),
                        payload.getLong("expenseId")
                );
                break;
            case SyncOperationType.UPSERT_FIXED_EXPENSE:
                firebaseSyncManager.upsertRemoteFixedExpense(
                        payload.getString("userId"),
                        new FixedExpenseEntity(
                                payload.getLong("expenseId"),
                                payload.getString("userId"),
                                payload.getString("name"),
                                payload.getString("category"),
                                payload.getDouble("amount"),
                                payload.getInt("dueDate"),
                                payload.getBoolean("isRecurring"),
                                payload.getLong("updatedAt")
                        )
                );
                break;
            case SyncOperationType.DELETE_FIXED_EXPENSE:
                firebaseSyncManager.deleteRemoteFixedExpense(
                        payload.getString("userId"),
                        payload.getLong("expenseId")
                );
                break;
            default:
                break;
        }
    }

    private void enqueue(String operationType, String payloadJson) {
        pendingSyncOperationDao.insert(
                new PendingSyncOperationEntity(
                        0,
                        operationType,
                        payloadJson,
                        System.currentTimeMillis(),
                        0,
                        null
                )
        );
    }
}
