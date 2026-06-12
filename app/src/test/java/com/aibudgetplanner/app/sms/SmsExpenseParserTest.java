package com.aibudgetplanner.app.sms;

import com.aibudgetplanner.app.domain.model.ExpenseCategory;

import org.junit.Assert;
import org.junit.Test;

public class SmsExpenseParserTest {

    @Test
    public void parseTransaction_detectsDebitUpiTemplate() {
        String message = "A/C XX1234 debited by INR 450.50 on UPI to SWIGGY ref 9ABCD12345";

        SmsExpenseCandidate result = SmsExpenseParser.parseTransaction(message);

        Assert.assertNotNull(result);
        Assert.assertEquals(450.50, result.amount, 0.0001);
        Assert.assertEquals(ExpenseCategory.FOOD, result.category);
        Assert.assertEquals("UPI", result.paymentMethod);
        Assert.assertEquals("SWIGGY", result.merchant.toUpperCase());
    }

    @Test
    public void parseTransaction_ignoresCreditMessage() {
        String message = "INR 1200 credited to your account via IMPS";

        SmsExpenseCandidate result = SmsExpenseParser.parseTransaction(message);

        Assert.assertNull(result);
    }
}
