package com.aibudgetplanner.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fixed_expense")
public class FixedExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    public long expenseId;

    @NonNull
    public String userId;
    @NonNull
    public String name;
    @NonNull
    public String category;
    public double amount;
    public int dueDate;
    public boolean isRecurring;
    public long updatedAt;

    public FixedExpenseEntity(
            long expenseId,
            @NonNull String userId,
            @NonNull String name,
            @NonNull String category,
            double amount,
            int dueDate,
            boolean isRecurring,
            long updatedAt
    ) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.dueDate = dueDate;
        this.isRecurring = isRecurring;
        this.updatedAt = updatedAt;
    }
}
