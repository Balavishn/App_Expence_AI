package com.aibudgetplanner.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense")
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    public long expenseId;

    @NonNull
    public String userId;

    public long date;
    @NonNull
    public String category;
    public double amount;
    @NonNull
    public String description;
    @NonNull
    public String paymentMethod;
    public long updatedAt;

    public ExpenseEntity(
            long expenseId,
            @NonNull String userId,
            long date,
            @NonNull String category,
            double amount,
            @NonNull String description,
            @NonNull String paymentMethod,
            long updatedAt
    ) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.paymentMethod = paymentMethod;
        this.updatedAt = updatedAt;
    }
}
