package com.aibudgetplanner.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;

import com.aibudgetplanner.app.sms.SmsExpenseProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SmsTransactionReceiver extends BroadcastReceiver {
    private static final ExecutorService RECEIVER_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    SmsExpenseProcessor smsExpenseProcessor;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        android.telephony.SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        StringBuilder bodyBuilder = new StringBuilder();
        for (android.telephony.SmsMessage message : messages) {
            if (message.getMessageBody() != null) {
                if (bodyBuilder.length() > 0) {
                    bodyBuilder.append(' ');
                }
                bodyBuilder.append(message.getMessageBody());
            }
        }

        String body = bodyBuilder.toString().trim();
        if (body.isBlank()) {
            return;
        }

        RECEIVER_EXECUTOR.execute(() -> smsExpenseProcessor.handleSms(body));
    }
}
