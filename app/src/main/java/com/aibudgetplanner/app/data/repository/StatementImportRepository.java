package com.aibudgetplanner.app.data.repository;

import com.aibudgetplanner.app.domain.model.StatementImportResult;

import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
public class StatementImportRepository {
    private final OkHttpClient client = new OkHttpClient();
    private final String baseUrl = "http://10.0.2.2:8000";

    @Inject
    public StatementImportRepository() {
    }

    public StatementImportResult importStatement(String fileName, String mimeType, byte[] bytes) throws IOException {
        MediaType mediaType = mimeType == null ? null : MediaType.parse(mimeType);
        RequestBody fileBody = RequestBody.create(bytes, mediaType);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/statements/import")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Import failed: " + response.code());
            }

            String payload = response.body() != null ? response.body().string() : "";
            JSONObject json = new JSONObject(payload);
            return new StatementImportResult(
                    json.getString("statement_type"),
                    json.getInt("imported_count"),
                    json.getInt("duplicate_count"),
                    json.getInt("total_parsed_count"),
                    json.getInt("auto_categorized_count")
            );
        }
    }
}
