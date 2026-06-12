package com.aibudgetplanner.app.ai;

import android.content.Context;

import com.aibudgetplanner.app.domain.model.BudgetSnapshot;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDate;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TFLiteSpendingPredictor implements SpendingPredictor {

    private final Context context;
    private Interpreter interpreter;

    @Inject
    public TFLiteSpendingPredictor(@ApplicationContext Context context) {
        this.context = context;
    }

    @Override
    public PredictionInference predictMonthEndSpend(BudgetSnapshot snapshot) {
        float[] featureVector = new float[]{
                (float) snapshot.getSalary(),
                (float) snapshot.getTotalFixedExpenses(),
                (float) snapshot.getTotalSpentThisMonth(),
                (float) snapshot.getSavingsGoal(),
                (float) snapshot.getRemainingDays(),
                (float) snapshot.getDailyBudget(),
                (float) snapshot.getAvailableBudget(),
                (float) LocalDate.now().getDayOfMonth()
        };

        Interpreter model = getInterpreter();
        if (model != null) {
            float[][] input = new float[][]{featureVector};
            float[][] output = new float[][]{new float[1]};
            try {
                model.run(input, output);
                return new PredictionInference(Math.max(output[0][0], 0.0), "TensorFlow Lite");
            } catch (Exception ignored) {
                // Falls back to heuristic when model inference fails.
            }
        }

        int daysElapsed = Math.max(LocalDate.now().getDayOfMonth(), 1);
        int remainingFutureDays = Math.max(snapshot.getRemainingDays() - 1, 0);
        double avgDaily = snapshot.getTotalSpentThisMonth() / daysElapsed;
        double heuristic = snapshot.getTotalSpentThisMonth() + (avgDaily * remainingFutureDays);
        return new PredictionInference(heuristic, "Heuristic Fallback");
    }

    private synchronized Interpreter getInterpreter() {
        if (interpreter != null) {
            return interpreter;
        }
        try {
            interpreter = new Interpreter(loadModelFile("models/spending_predictor.tflite"));
            return interpreter;
        } catch (Exception ignored) {
            return null;
        }
    }

    private MappedByteBuffer loadModelFile(String assetPath) throws Exception {
        try (var fileDescriptor = context.getAssets().openFd(assetPath);
             var inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel channel = inputStream.getChannel()) {
            return channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getDeclaredLength()
            );
        }
    }
}
