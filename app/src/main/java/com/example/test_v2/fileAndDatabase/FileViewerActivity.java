package com.example.test_v2.fileAndDatabase;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.test_v2.R;

import java.io.File;
import java.io.IOException;

public class FileViewerActivity extends Activity {
    private ImageView fileDisplay;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        fileDisplay = findViewById(R.id.file_display);
        Button backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> finish()); // ✅ Back Button Functionality

        String filePath = getIntent().getStringExtra("filePath");
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png")) {
                    displayImage(file);
                } else if (filePath.endsWith(".pdf")) {
                    displayPDF(file);
                }
            } else {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayImage(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        fileDisplay.setImageBitmap(bitmap);
    }

    private void displayPDF(File file) {
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            currentPage = pdfRenderer.openPage(0);

            Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            fileDisplay.setImageBitmap(bitmap);
        } catch (IOException e) {
            Toast.makeText(this, "Error displaying PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (currentPage != null) {
                currentPage.close();
            }
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}