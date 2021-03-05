package com.example.convertpdf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;

public class WritePdfActivity extends AppCompatActivity {

    private static final int STORAGE_CODE = 1000;
    EditText et_writetext;
    Button btn_savepdf;
    String fontpath = "res/font/malgun.ttf";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_pdf);

        et_writetext = findViewById(R.id.et_writetext);
        btn_savepdf = findViewById(R.id.btn_savepdf);


        btn_savepdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions,STORAGE_CODE);
                    }else{
                        SavePdf();
                    }
                }else {
                    SavePdf();
                }
            }
        });

    }
    private void SavePdf() {
        // 파일 이름을 정하는과정을 다이얼로그로 정했습니다.
        AlertDialog.Builder dialog = new AlertDialog.Builder(WritePdfActivity.this);
        dialog.setTitle("파일 이름");

        // 파일 이름이 들어갈 EditText 입니다.
        final EditText et = new EditText(WritePdfActivity.this);
        dialog.setView(et);
        dialog.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String filename = et.getText().toString();

                // 최종적으로 파일을 저장할 주소를 지정하는 구문입니다.
                // 최상위 디렉토리의 "PDF Folder 2021"폴더를 저장주소로 지정하고
                // 해당 폴더가 없다면 생성합니다.
                File root = new File(Environment.getExternalStorageDirectory(),"PDF Folder 2021");
                if ( !root.exists() ){
                    root.mkdir();
                }
                // 생성될 파일의 저장주소와 이름을 담고있습니다.
                File file = new File(root, filename + ".pdf");

                // itextdpf의 API를 사용했습니다. version 5.
                Document document = new Document();
                try {
                    // 한글 깨짐 현상을 위해 폰트를 직접 지정했습니다.
                    BaseFont bfont = BaseFont.createFont(fontpath,BaseFont.IDENTITY_H,BaseFont.EMBEDDED );
                    Font font = new Font(bfont);

                    PdfWriter.getInstance(document, new FileOutputStream(file));
                    document.open();

                    String mText = et_writetext.getText().toString();

                    document.add(new Paragraph(mText,font));

                    document.close();

                }
                catch (Exception e){
                }
                dialog.dismiss();
                Intent intent = new Intent(WritePdfActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        dialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) SavePdf();
                else Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                }
        }
    }



}