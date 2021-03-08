package com.example.convertpdf;

/*
    사용한 라이브러리 :
    itextpdf version 5.
    Commons-io
    github의 barteksc 의 android-pdf-viewer.
*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_CODE = 1000;

    ImageButton writepdf,Txt_convert,Image_convert,openfile;
    String mfilename = " ";
    String myPath = " ";
    String fontpath = "res/font/malgun.ttf";

    Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/");
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("PDF Maker");

        writepdf = findViewById(R.id.writepdf);
        Txt_convert = findViewById(R.id.Txt_convert);
        Image_convert = findViewById(R.id.Image_convert);
        openfile = findViewById(R.id.openfile);


        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        // 사용자가 직접 타이핑으로 PDF 파일을 작성할 수 있게 하는 구문의 시작.
        // 화면 전환이 필요하여 class 를 따로 생성함.
        writepdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,WritePdfActivity.class);
                startActivity(intent);
            }
        });

        // Txt 형식의 파일을 PDF 형식의 파일로 변환이 가능토록 하는 구문.
        // version 체크 와 권한 요청, 해당 Imagebutton 클릭 시 모든유형의 파일을 볼수있는 화면 제공.
        // 모든 ImageButton 동일함.
        Txt_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SDK version이 23보다 낮은지 체크.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    // SDK version이 23보다 높고 권한이 거부되었는지 체크.
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        // 거부되었다면 권한 요청.
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions,STORAGE_CODE);
                    }else{
                        // 거부되지않았다면 본래의 기능을 실행.
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setDataAndType(uri, "*/*");
                        startActivityForResult(intent, 120);
                    }
                }else {
                    // SDK version이 23보다 낮더라도 마찬가지로 본래의 기능 실행.
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setDataAndType(uri, "*/*");
                    startActivityForResult(intent, 120);
                }
            }
        });

        // Image 형식의 파일을 PDF 형식의 파일로 변환이 가능토록 하는 구문.
        Image_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions,STORAGE_CODE);
                    }else{
                        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setDataAndType(uri, "*/*");
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 120);
                    }
                }else {
                    //Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setDataAndType(uri, "*/*");
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 120);
                }
            }
        });

        // PDF Viewer
        // 화면 전환을 위해 class를 따로 생성함.
        openfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivity에서 Pdfview로 화면 전환을 위한 Intent.
                Intent intent = new Intent(MainActivity.this,PdfView.class);
                startActivity(intent);
            }
        });


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
            myPath = cursor.getString(columnIndex);
            Log.e("myPath",myPath);
            mfilename = FilenameUtils.getBaseName(myPath);
            Log.e("mfilename",mfilename);
            cursor.close();
            String Extension = FilenameUtils.getExtension(myPath);
            Log.e("extension",Extension);

            switch (Extension){
                case "txt" : TextToPDF(); break;
                case "png" : ImageToPDf(); break;
                case "jpg" : ImageToPDf(); break;
            }

        }
    }

    private void TextToPDF() {

        // 최종적으로 파일을 저장할 주소를 지정하는 구문입니다.
        // 최상위 디렉토리의 "PDF Folder 2021"폴더를 저장주소로 지정하고
        // 해당 폴더가 없다면 생성합니다.
        File root = new File(Environment.getExternalStorageDirectory(),"PDF Folder 2021");
        if ( !root.exists() ){
            root.mkdir();
        }
        File file = new File(root, mfilename + ".pdf");

        Document document = new Document();
        try {
            // 한글 깨짐 현상을 막기 위해 폰트를 적용했습니다.
            // fontpath 는 폰트파일의 위치입니다.
            // itextdpf의 API를 사용했습니다. version 5.
            BaseFont bfont = BaseFont.createFont(fontpath,BaseFont.IDENTITY_H,BaseFont.EMBEDDED );
            Font font = new Font(bfont);
            PdfWriter.getInstance(document, new FileOutputStream(file));

            document.open();

            // 기존 파일의 내용을 읽습니다.
            BufferedReader br  =  new BufferedReader(new InputStreamReader(new FileInputStream(myPath),"euc-kr"));

            String line =null;
            // readLine : 텍스트 행을 읽습니다. 더이상 읽을 행이 없을 경우 null.
            while ((line = br.readLine()) != null){
                // 폰트를 적용하고 읽은 내용을 새로운 파일에 씁니다.
                document.add(new Paragraph(line,font));
                // readline 이 읽지 못하는 줄바꿈에 대한 방안으로 각 행을 읽은 후 \n 추가.
                // 하지만 읽지못할뿐 읽는과정은 그대로 진행되기 때문에 문장과 문장 사이에 과도한 \n가 추가됨.
                document.add(new Paragraph("\n"));
            }

            // 종료
            document.close();

            Toast.makeText(this, file +"가(이) 저장되었습니다.", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Result_Error",e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void ImageToPDf() {

        // 저장 폴더의 유무 처리.
        File root = new File(Environment.getExternalStorageDirectory(),"PDF Folder 2021");
        if ( !root.exists() ){
            // 없을 시 생성.
            root.mkdir();
        }
        File file = new File(root,mfilename+".pdf");

        Bitmap bitmap = BitmapFactory.decodeFile(myPath);


        PdfDocument pdfDocument = new PdfDocument();
        // bitmap.getWidth 에서 null을 반환하여 오류가 생깁니다.
        // Manifest <Application 밑에 android:requestLegacyExternalStorage="true" 를 추가해야합니다.
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(),bitmap.getHeight(),1).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawPaint(paint);

        // bitmap.getWidth 에서 null을 반환하여 오류가 생깁니다.
        //Manifest <Application 밑에 android:requestLegacyExternalStorage="true" 를 추가해야합니다.
        bitmap = Bitmap.createScaledBitmap(bitmap,canvas.getWidth(),canvas.getHeight(),true);
        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap,0,0,null);
        pdfDocument.finishPage(page);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            pdfDocument.writeTo(fileOutputStream);
            Toast.makeText(this, file +"가(이) 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Result_Error",e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Result_Error",e.getMessage());
        }

        // Document 종료.
        pdfDocument.close();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                else Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}