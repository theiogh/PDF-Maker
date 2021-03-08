package com.example.convertpdf;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PaintFlagsDrawFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class PdfView extends AppCompatActivity {

    Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/");
    private static final int STORAGE_CODE = 1000;
    String myPath = " ";
    PDFView pdfviewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        pdfviewer = findViewById(R.id.pdfviewer);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,STORAGE_CODE);
            }else{
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setDataAndType(uri, "*/*");
                startActivityForResult(intent, 120);
            }
        }else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setDataAndType(uri, "*/*");
            startActivityForResult(intent, 120);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 120 && resultCode == RESULT_OK && data != null) {

            Uri selectedImageUri = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImageUri, filePath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePath[0]);
            Log.e("columnIndex", String.valueOf(columnIndex));
            myPath = cursor.getString(columnIndex);
            Log.e("myPath",myPath);
            cursor.close();

            File file = new File(myPath);

            pdfviewer.fromFile(file)
                     .defaultPage(0)
                     .enableAnnotationRendering(true)
                     .scrollHandle(new DefaultScrollHandle(this))
                     .spacing(2)
                     .load();
        }
    }
}


