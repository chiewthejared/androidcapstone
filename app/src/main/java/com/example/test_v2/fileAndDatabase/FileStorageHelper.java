package com.example.test_v2.fileAndDatabase;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileStorageHelper {
    public static File saveFile(ContentResolver resolver, File directory, Uri fileUri, String fileName) {
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            try (InputStream inputStream = resolver.openInputStream(fileUri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            Log.d("FileStorageHelper", "File saved at: " + file.getAbsolutePath());
            return file;
        } catch (Exception e) {
            Log.e("FileStorageHelper", "Error saving file", e);
            return null;
        }
    }

    public static String getFileName(ContentResolver resolver, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex >= 0) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }
        return result != null ? result : "file_" + System.currentTimeMillis();
    }
}