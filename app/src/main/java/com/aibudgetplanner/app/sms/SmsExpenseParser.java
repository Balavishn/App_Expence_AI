package com.aibudgetplanner.app.sms;

import com.aibudgetplanner.app.domain.model.ExpenseCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SmsExpenseParser {
    private static final List<Pattern> DEBIT_PATTERNS = Arrays.asList(
            Pattern.compile("(?:debited|spent|payment of|paid|withdrawn|sent)[:\\s]*(?:inr|rs\\.?|₹)?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)\\s*(?:debited|spent|paid|withdrawn|sent)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:txn|transaction).{0,35}?(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("a/c\\s*[*xX]{2,}\\d{2,}.{0,40}?(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:purchase|pos|ecom|upi txn|imps txn|neft txn).{0,40}?(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:amount)\\s*(?:inr|rs\\.?|₹)\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?).{0,30}?(?:debited|paid|dr)", Pattern.CASE_INSENSITIVE)
    );

    private static final List<String> CREDIT_MARKERS = Arrays.asList("credited", "received", "refund", "reversed", "cashback", "deposited");

    private SmsExpenseParser() {
    }

    public static SmsExpenseCandidate parseTransaction(String message) {
        String normalized = message == null ? "" : message.trim();
        if (normalized.isBlank()) {
            return null;
        }

        String lower = normalized.toLowerCase(Locale.getDefault());
        for (String marker : CREDIT_MARKERS) {
            if (lower.contains(marker)) {
                return null;
            }
        }

        Double amount = null;
        for (Pattern pattern : DEBIT_PATTERNS) {
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find() && matcher.groupCount() >= 1) {
                String value = matcher.group(1);
                if (value != null) {
                    try {
                        amount = Double.parseDouble(value.replace(",", ""));
                        break;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (amount == null) {
            return null;
        }

        SmsEntities entities = SmsNlpEntityExtractor.extract(normalized);
        String description = extractDescription(normalized);
        ExpenseCategory category = inferCategory(normalized);
        String paymentMethod = "SMS".equals(entities.channel) ? inferPaymentMethod(normalized) : entities.channel;

        return new SmsExpenseCandidate(
                amount,
                description,
                category,
                paymentMethod,
                entities.merchant,
                entities.accountLastDigits,
                entities.transactionReference
        );
    }

    private static String extractDescription(String message) {
        String cleaned = message.replaceAll("\\s+", " ").trim();
        return cleaned.length() <= 140 ? cleaned : cleaned.substring(0, 140);
    }

    private static ExpenseCategory inferCategory(String message) {
        String text = message.toLowerCase(Locale.getDefault());
        if (containsAny(text, "restaurant", "food", "cafe", "coffee", "swiggy", "zomato", "grocery", "supermarket")) {
            return ExpenseCategory.FOOD;
        }
        if (containsAny(text, "uber", "ola", "taxi", "metro", "bus", "train", "flight", "fuel", "petrol", "diesel")) {
            return ExpenseCategory.TRAVEL;
        }
        if (containsAny(text, "amazon", "flipkart", "myntra", "shopping", "mall", "store", "retail")) {
            return ExpenseCategory.SHOPPING;
        }
        if (containsAny(text, "electric", "bill", "gas", "water", "internet", "phone", "mobile")) {
            return ExpenseCategory.BILLS;
        }
        if (containsAny(text, "netflix", "spotify", "prime", "movie", "cinema")) {
            return ExpenseCategory.ENTERTAINMENT;
        }
        if (containsAny(text, "hospital", "clinic", "pharmacy", "medicine", "medical")) {
            return ExpenseCategory.MEDICAL;
        }
        if (containsAny(text, "school", "college", "course", "education", "udemy", "coursera")) {
            return ExpenseCategory.EDUCATION;
        }
        if (containsAny(text, "sip", "mutual fund", "stock", "investment", "broker", "nse", "bse")) {
            return ExpenseCategory.INVESTMENTS;
        }
        return ExpenseCategory.OTHER;
    }

    private static String inferPaymentMethod(String message) {
        String text = message.toLowerCase(Locale.getDefault());
        if (text.contains("upi") || text.contains("vpa")) {
            return "UPI";
        }
        if (text.contains("card") || text.contains("debit card")) {
            return "Card";
        }
        if (text.contains("cash")) {
            return "Cash";
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
        if (text.contains("net banking") || text.contains("netbanking")) {
            return "Net Banking";
        }
        return "SMS";
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
