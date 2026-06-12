package com.aibudgetplanner.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfileEntity {
    @PrimaryKey
    @NonNull
    public String userId;

    public double salary;
    public int salaryDate;
    public double monthlySavingsGoal;
    @NonNull
    public String currency;
    public long createdDate;
    @NonNull
    public String financialGoals;
    public long updatedAt;

    public UserProfileEntity(
            @NonNull String userId,
            double salary,
            int salaryDate,
            double monthlySavingsGoal,
            @NonNull String currency,
            long createdDate,
            @NonNull String financialGoals,
            long updatedAt
    ) {
        this.userId = userId;
        this.salary = salary;
        this.salaryDate = salaryDate;
        this.monthlySavingsGoal = monthlySavingsGoal;
        this.currency = currency;
        this.createdDate = createdDate;
        this.financialGoals = financialGoals;
        this.updatedAt = updatedAt;
    }
}
