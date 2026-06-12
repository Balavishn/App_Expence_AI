package com.aibudgetplanner.app.sms;

import androidx.annotation.Nullable;

import com.aibudgetplanner.app.domain.model.ExpenseCategory;

public class SmsExpenseCandidate {
    public final double amount;
    public final String description;
    public final ExpenseCategory category;
    public final String paymentMethod;
    public final String merchant;
    @Nullable
    public final String accountLastDigits;
    @Nullable
    public final String transactionReference;

    public SmsExpenseCandidate(
            double amount,
            String description,
            ExpenseCategory category,
            String paymentMethod,
            String merchant,
            @Nullable String accountLastDigits,
            @Nullable String transactionReference
    ) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.merchant = merchant;
        this.accountLastDigits = accountLastDigits;
        this.transactionReference = transactionReference;
    }
}
