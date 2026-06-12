package com.aibudgetplanner.app.ui.java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.repository.FirebaseSyncManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    FirebaseSyncManager firebaseSyncManager;

    private Button syncNowButton;
    private TextView statusText;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        syncNowButton = root.findViewById(R.id.profile_sync_button);
        statusText = root.findViewById(R.id.profile_sync_status);

        syncNowButton.setOnClickListener(v -> syncNow());
        return root;
    }

    private void syncNow() {
        syncNowButton.setEnabled(false);
        statusText.setText("Syncing...");

        IO_EXECUTOR.execute(() -> {
            String status;
            try {
                FirebaseSyncManager.SyncSummary summary = firebaseSyncManager.sync("local-user");
                status = "Uploaded " + summary.uploaded + ", Downloaded " + summary.downloaded
                        + ", Conflicts " + summary.conflictsResolved + " (" + summary.strategy + ")";
            } catch (Exception exception) {
                status = "Sync failed: " + (exception.getMessage() == null ? "Unknown error" : exception.getMessage());
            }

            if (!isAdded()) {
                return;
            }
            final String finalStatus = status;
            requireActivity().runOnUiThread(() -> {
                statusText.setText(finalStatus);
                syncNowButton.setEnabled(true);
            });
        });
    }
}
