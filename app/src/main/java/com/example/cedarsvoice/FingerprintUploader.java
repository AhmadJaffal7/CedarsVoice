package com.example.cedarsvoice;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class FingerprintUploader {
    private static final int FILE_PICK_REQUEST_CODE = 1;
    private static final String TAG = "FingerprintUploader";

    public void uploadFingerprintFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, FILE_PICK_REQUEST_CODE);
    }

    public void handleFilePickResult(int requestCode, int resultCode, Intent data, Activity activity) {
        if (requestCode == FILE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            String fileName = getFileName(fileUri, activity.getContentResolver());
            byte[] fileBytes = readFileBytes(fileUri, activity.getContentResolver());
            try {
                saveFingerprintToDatabase(fileName, fileBytes);
            } catch (SQLException e) {
                Log.e(TAG, "Error saving fingerprint to database: " + e.getMessage());
            }        }
    }

    private String getFileName(Uri uri, ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        String fileName = cursor.getString(nameIndex);
        cursor.close();
        return fileName;
    }

    private byte[] readFileBytes(Uri uri, ContentResolver contentResolver) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream: " + e.getMessage());
                }
            }
        }
        return outputStream.toByteArray();
    }

    private void saveFingerprintToDatabase(String fileName, byte[] fileBytes) {
        String url = "jdbc:mysql://your_server_address/your_database_name";
        String user = "your_username";
        String password = "your_password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO fingerprints (name, fingerprint_data) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, fileName);
            Blob blob = conn.createBlob();
            blob.setBytes(1, fileBytes);
            statement.setBlob(2, blob);
            statement.executeUpdate();
            Log.d(TAG, "Fingerprint saved to database successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving fingerprint to database: " + e.getMessage());
        }
    }
}