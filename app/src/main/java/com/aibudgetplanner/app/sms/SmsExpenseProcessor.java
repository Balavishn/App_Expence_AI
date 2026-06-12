package com.aibudgetplanner.app.sms;

import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.repository.BudgetRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SmsExpenseProcessor {
    private final BudgetRepository budgetRepository;
    private final SmsDedupStore smsDedupStore;

    @Inject
    public SmsExpenseProcessor(BudgetRepository budgetRepository, SmsDedupStore smsDedupStore) {
        this.budgetRepository = budgetRepository;
        this.smsDedupStore = smsDedupStore;
    }

    public boolean handleSms(String message) {
        SmsExpenseCandidate candidate = SmsExpenseParser.parseTransaction(message);
        if (candidate == null) {
            return false;
        }

        String hash = Integer.toString(message.trim().toLowerCase().hashCode());
        if (smsDedupStore.contains(hash)) {
            return false;
        }

        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append(candidate.merchant).append(" | ").append(candidate.description);
        if (candidate.accountLastDigits != null) {
            descriptionBuilder.append(" | A/C xx").append(candidate.accountLastDigits);
        }
        if (candidate.transactionReference != null) {
            descriptionBuilder.append(" | Ref ").append(candidate.transactionReference);
        }

        budgetRepository.addExpense(
                new ExpenseEntity(
                        0,
                        "local-user",
                        System.currentTimeMillis(),
                        candidate.category.name(),
                        candidate.amount,
                        descriptionBuilder.toString(),
                        candidate.paymentMethod,
                        System.currentTimeMillis()
                )
        );

        smsDedupStore.add(hash);
        return true;
    }
}
