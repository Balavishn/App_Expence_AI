package com.aibudgetplanner.app.ui.java;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.aibudgetplanner.app.MainActivity;
import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.security.AppLockRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppLockFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    AppLockRepository appLockRepository;

    private EditText pinInput;
    private Button pinButton;
    private Button biometricButton;
    private TextView errorLabel;
    private TextView subtitleLabel;

    private boolean hasPin = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_app_lock, container, false);
        pinInput = root.findViewById(R.id.app_lock_pin_input);
        pinButton = root.findViewById(R.id.app_lock_pin_button);
        biometricButton = root.findViewById(R.id.app_lock_biometric_button);
        errorLabel = root.findViewById(R.id.app_lock_error_label);
        subtitleLabel = root.findViewById(R.id.app_lock_subtitle);

        pinButton.setOnClickListener(v -> submitPin());
        biometricButton.setOnClickListener(v -> showBiometricPrompt());

        loadState();
        return root;
    }

    private void loadState() {
        IO_EXECUTOR.execute(() -> {
            boolean lockEnabled = appLockRepository.isLockEnabled();
            boolean storedPin = appLockRepository.hasPin();

            if (!isAdded()) {
                return;
            }

            requireActivity().runOnUiThread(() -> {
                if (!lockEnabled) {
                    unlockAndContinue();
                    return;
                }

                hasPin = storedPin;
                subtitleLabel.setText(hasPin ? "Use biometric authentication or PIN" : "Set a PIN to secure this app");
                pinButton.setText(hasPin ? "Unlock with PIN" : "Save PIN and Unlock");

                boolean showBiometric = hasPin && canUseBiometric(requireContext());
                biometricButton.setVisibility(showBiometric ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void submitPin() {
        String pin = pinInput.getText() == null ? "" : pinInput.getText().toString().trim();
        if (TextUtils.isEmpty(pin) || pin.length() < 4) {
            errorLabel.setText("PIN must be at least 4 digits");
            return;
        }

        pinButton.setEnabled(false);
        errorLabel.setText("");

        IO_EXECUTOR.execute(() -> {
            boolean valid;
            if (!hasPin) {
                appLockRepository.savePin(pin);
                valid = true;
            } else {
                valid = appLockRepository.verifyPin(pin);
            }

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                pinButton.setEnabled(true);
                if (valid) {
                    unlockAndContinue();
                } else {
                    errorLabel.setText("Invalid PIN");
                }
            });
        });
    }

    private void showBiometricPrompt() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        java.util.concurrent.Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt prompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                unlockAndContinue();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                errorLabel.setText(errString);
            }
        });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock AI Budget Planner")
                .setSubtitle("Authenticate to continue")
                .setNegativeButtonText("Cancel")
                .build();

        prompt.authenticate(info);
    }

    private void unlockAndContinue() {
        if (!isAdded()) {
            return;
        }
        MainActivity activity = (MainActivity) requireActivity();
        activity.onAppUnlocked();
    }

    private static boolean canUseBiometric(Context context) {
        BiometricManager manager = BiometricManager.from(context);
        int result = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }
}
