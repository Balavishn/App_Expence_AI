package com.aibudgetplanner.app.data.repository;

import com.aibudgetplanner.app.data.local.dao.ExpenseDao;
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao;
import com.aibudgetplanner.app.data.local.dao.UserProfileDao;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;
import com.aibudgetplanner.app.data.sync.SyncScheduler;
import com.aibudgetplanner.app.domain.model.BudgetSnapshot;
import com.aibudgetplanner.app.domain.usecase.BudgetEngineUseCase;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import kotlin.jvm.functions.Function3;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowKt;

@Singleton
public class BudgetRepository {
    private final UserProfileDao userProfileDao;
    private final FixedExpenseDao fixedExpenseDao;
    private final ExpenseDao expenseDao;
    private final BudgetEngineUseCase budgetEngineUseCase;
    private final PendingSyncRepository pendingSyncRepository;
    private final SyncScheduler syncScheduler;

    @Inject
    public BudgetRepository(
            UserProfileDao userProfileDao,
            FixedExpenseDao fixedExpenseDao,
            ExpenseDao expenseDao,
            BudgetEngineUseCase budgetEngineUseCase,
            PendingSyncRepository pendingSyncRepository,
            SyncScheduler syncScheduler
    ) {
        this.userProfileDao = userProfileDao;
        this.fixedExpenseDao = fixedExpenseDao;
        this.expenseDao = expenseDao;
        this.budgetEngineUseCase = budgetEngineUseCase;
        this.pendingSyncRepository = pendingSyncRepository;
        this.syncScheduler = syncScheduler;
    }

    public Flow<BudgetSnapshot> observeBudgetSnapshot(String userId) {
        long fromDate = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return FlowKt.combine(
                userProfileDao.observeProfile(),
                fixedExpenseDao.observeTotalByUser(userId),
                expenseDao.observeSpentFrom(userId, fromDate),
                new Function3<UserProfileEntity, Double, Double, BudgetSnapshot>() {
                    @Override
                    public BudgetSnapshot invoke(UserProfileEntity profile, Double fixedTotal, Double spentThisMonth) {
                        if (profile == null) {
                            return null;
                        }
                        return toBudgetSnapshot(profile, fixedTotal, spentThisMonth);
                    }
                }
        );
    }

    public void upsertProfile(UserProfileEntity profile) {
        UserProfileEntity updated = new UserProfileEntity(
                profile.userId,
                profile.salary,
                profile.salaryDate,
                profile.monthlySavingsGoal,
                profile.currency,
                profile.createdDate,
                profile.financialGoals,
                System.currentTimeMillis()
        );
        userProfileDao.upsert(updated);
        pendingSyncRepository.enqueueProfileUpsert(updated);
        syncScheduler.requestImmediateSync();
    }

    public Flow<List<ExpenseEntity>> observeExpenses(String userId) {
        return expenseDao.observeByUser(userId);
    }

    public Flow<List<FixedExpenseEntity>> observeFixedExpenses(String userId) {
        return fixedExpenseDao.observeByUser(userId);
    }

    public void addExpense(ExpenseEntity expense) {
        ExpenseEntity updated = new ExpenseEntity(
                expense.expenseId,
                expense.userId,
                expense.date,
                expense.category,
                expense.amount,
                expense.description,
                expense.paymentMethod,
                System.currentTimeMillis()
        );
        long insertedId = expenseDao.insert(updated);
        pendingSyncRepository.enqueueExpenseUpsert(
                new ExpenseEntity(
                        insertedId,
                        updated.userId,
                        updated.date,
                        updated.category,
                        updated.amount,
                        updated.description,
                        updated.paymentMethod,
                        updated.updatedAt
                )
        );
        syncScheduler.requestImmediateSync();
    }

    public void updateExpense(ExpenseEntity expense) {
        ExpenseEntity updated = new ExpenseEntity(
                expense.expenseId,
                expense.userId,
                expense.date,
                expense.category,
                expense.amount,
                expense.description,
                expense.paymentMethod,
                System.currentTimeMillis()
        );
        expenseDao.update(updated);
        pendingSyncRepository.enqueueExpenseUpsert(updated);
        syncScheduler.requestImmediateSync();
    }

    public void deleteExpense(ExpenseEntity expense) {
        expenseDao.delete(expense);
        pendingSyncRepository.enqueueExpenseDelete(expense);
        syncScheduler.requestImmediateSync();
    }

    public void addFixedExpense(FixedExpenseEntity expense) {
        FixedExpenseEntity updated = new FixedExpenseEntity(
                expense.expenseId,
                expense.userId,
                expense.name,
                expense.category,
                expense.amount,
                expense.dueDate,
                expense.isRecurring,
                System.currentTimeMillis()
        );
        long insertedId = fixedExpenseDao.insert(updated);
        pendingSyncRepository.enqueueFixedExpenseUpsert(
                new FixedExpenseEntity(
                        insertedId,
                        updated.userId,
                        updated.name,
                        updated.category,
                        updated.amount,
                        updated.dueDate,
                        updated.isRecurring,
                        updated.updatedAt
                )
        );
        syncScheduler.requestImmediateSync();
    }

    public void updateFixedExpense(FixedExpenseEntity expense) {
        FixedExpenseEntity updated = new FixedExpenseEntity(
                expense.expenseId,
                expense.userId,
                expense.name,
                expense.category,
                expense.amount,
                expense.dueDate,
                expense.isRecurring,
                System.currentTimeMillis()
        );
        fixedExpenseDao.update(updated);
        pendingSyncRepository.enqueueFixedExpenseUpsert(updated);
        syncScheduler.requestImmediateSync();
    }

    public void deleteFixedExpense(FixedExpenseEntity expense) {
        fixedExpenseDao.delete(expense);
        pendingSyncRepository.enqueueFixedExpenseDelete(expense);
        syncScheduler.requestImmediateSync();
    }

    private BudgetSnapshot toBudgetSnapshot(UserProfileEntity profile, double fixedTotal, double spentThisMonth) {
        LocalDate today = LocalDate.now();
        int daysInMonth = today.lengthOfMonth();
        int remainingDays = Math.max(daysInMonth - today.getDayOfMonth() + 1, 1);

        double availableBudget = budgetEngineUseCase.calculateAvailableBudget(
                profile.salary,
                fixedTotal,
                profile.monthlySavingsGoal
        );
        double remainingBudget = budgetEngineUseCase.calculateRemainingBudget(availableBudget, spentThisMonth);
        double dailyBudget = budgetEngineUseCase.calculateDailyBudget(availableBudget, spentThisMonth, remainingDays);
        double savingsProgress = budgetEngineUseCase.calculateSavingsProgress(profile.salary, profile.monthlySavingsGoal);

        return new BudgetSnapshot(
                profile.salary,
                profile.monthlySavingsGoal,
                fixedTotal,
                spentThisMonth,
                availableBudget,
                remainingBudget,
                dailyBudget,
                remainingDays,
                savingsProgress
        );
    }
}
