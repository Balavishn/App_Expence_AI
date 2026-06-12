package com.aibudgetplanner.app.sms;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SmsNlpEntityExtractor {
    private static final Pattern[] MERCHANT_PATTERNS = new Pattern[]{
            Pattern.compile("(?:at|to|towards|merchant)\\s+([A-Za-z0-9&\\-._ ]{2,40})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:on)\\s+([A-Za-z0-9&\\-._ ]{2,40})\\s+(?:txn|transaction|ref|avl)", Pattern.CASE_INSENSITIVE)
    };

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("(?:a/c|acct|account)\\s*(?:no\\.?|number)?\\s*[:xX*]*\\s*(\\d{2,6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("(?:utr|ref(?:erence)?|txn(?: id)?)\\s*[:#-]?\\s*([A-Za-z0-9]{6,20})", Pattern.CASE_INSENSITIVE);

    private SmsNlpEntityExtractor() {
    }

    public static SmsEntities extract(String message) {
        String normalized = message.replaceAll("\\s+", " ").trim();

        String merchant = null;
        for (Pattern pattern : MERCHANT_PATTERNS) {
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find() && matcher.groupCount() >= 1) {
                String candidate = matcher.group(1);
                if (candidate != null) {
                    candidate = trimPunctuation(candidate.trim());
                    if (candidate.length() >= 2) {
                        merchant = candidate;
                        break;
                    }
                }
            }
        }
        if (merchant == null) {
            merchant = "Unknown Merchant";
        }

        String accountLastDigits = null;
        Matcher accountMatcher = ACCOUNT_PATTERN.matcher(normalized);
        if (accountMatcher.find() && accountMatcher.groupCount() >= 1) {
            accountLastDigits = accountMatcher.group(1);
        }

        String transactionReference = null;
        Matcher refMatcher = REFERENCE_PATTERN.matcher(normalized);
        if (refMatcher.find() && refMatcher.groupCount() >= 1) {
            transactionReference = refMatcher.group(1);
        }

        String channel = inferChannel(normalized);

        if (merchant.length() > 40) {
            merchant = merchant.substring(0, 40);
        }

        return new SmsEntities(merchant, accountLastDigits, transactionReference, channel);
    }

    private static String inferChannel(String message) {
        String text = message.toLowerCase(Locale.getDefault());
        if (text.contains("upi") || text.contains("vpa")) {
            return "UPI";
        }
        if (text.contains("debit card") || text.contains("credit card") || text.contains("card")) {
            return "Card";
        }
        if (text.contains("imps")) {
            return "IMPS";
        }
        if (text.contains("neft")) {
            return "NEFT";
        }
        if (text.contains("rtgs")) {
            return "RTGS";
        }
        if (text.contains("atm")) {
            return "ATM";
        }
        if (text.contains("net banking") || text.contains("netbanking")) {
            return "Net Banking";
        }
        return "SMS";
    }

    private static String trimPunctuation(String value) {
        return value.replaceAll("^[\\s.,;:]+|[\\s.,;:]+$", "");
    }
}
