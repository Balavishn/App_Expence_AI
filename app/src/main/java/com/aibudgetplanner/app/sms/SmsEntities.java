package com.aibudgetplanner.app.sms;

import androidx.annotation.Nullable;

public class SmsEntities {
    public final String merchant;
    @Nullable
    public final String accountLastDigits;
    @Nullable
    public final String transactionReference;
    public final String channel;

    public SmsEntities(
            String merchant,
            @Nullable String accountLastDigits,
            @Nullable String transactionReference,
            String channel
    ) {
        this.merchant = merchant;
        this.accountLastDigits = accountLastDigits;
        this.transactionReference = transactionReference;
        this.channel = channel;
    }
}
