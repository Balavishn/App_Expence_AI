package com.aibudgetplanner.app.ui.java;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.repository.StatementImportRepository;
import com.aibudgetplanner.app.domain.model.StatementImportResult;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatementImportFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    Context appContext;

    @Inject
    StatementImportRepository repository;

    private TextView fileNameText;
    private TextView statusText;
    private Button pickFileButton;
    private Button clearButton;

    private ActivityResultLauncher<Intent> pickFileLauncher;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_statement_import, container, false);
        fileNameText = root.findViewById(R.id.statement_file_name);
        statusText = root.findViewById(R.id.statement_status);
        pickFileButton = root.findViewById(R.id.statement_pick_file_button);
        clearButton = root.findViewById(R.id.statement_clear_button);

        pickFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importFile(uri);
                    }
                }
        );

        pickFileButton.setOnClickListener(v -> openPicker());
        clearButton.setOnClickListener(v -> {
            fileNameText.setText("No file selected");
            statusText.setText("No import yet");
        });

        return root;
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickFileLauncher.launch(intent);
    }

    private void importFile(Uri uri) {
        pickFileButton.setEnabled(false);
        clearButton.setEnabled(false);
        statusText.setText("Importing...");

        IO_EXECUTOR.execute(() -> {
            String fileName = queryDisplayName(uri);
            if (fileName == null) {
                fileName = "statement.csv";
            }
            String mimeType = appContext.getContentResolver().getType(uri);

            String status;
            try (InputStream inputStream = appContext.getContentResolver().openInputStream(uri)) {
                if (inputStream == null) {
                    throw new IllegalStateException("Unable to read selected file");
                }
                byte[] bytes = inputStream.readAllBytes();
                StatementImportResult result = repository.importStatement(fileName, mimeType, bytes);
                status = "Imported " + result.getImportedCount()
                        + ", Duplicates " + result.getDuplicateCount()
                        + ", Parsed " + result.getTotalParsedCount()
                        + ", Auto-categorized " + result.getAutoCategorizedCount()
                        + " (" + result.getStatementType() + ")";
            } catch (Exception exception) {
                status = "Import failed: " + (exception.getMessage() == null ? "Unknown error" : exception.getMessage());
            }

            if (!isAdded()) {
                return;
            }

            final String finalFileName = fileName;
            final String finalStatus = status;
            requireActivity().runOnUiThread(() -> {
                fileNameText.setText(finalFileName);
                statusText.setText(finalStatus);
                pickFileButton.setEnabled(true);
                clearButton.setEnabled(true);
            });
        });
    }

    private String queryDisplayName(Uri uri) {
        try (android.database.Cursor cursor = appContext.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor == null) {
                return null;
            }
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex < 0 || !cursor.moveToFirst()) {
                return null;
            }
            return cursor.getString(nameIndex);
        }
    }
}
