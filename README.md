## PDF-Maker-and-Viewer

txt형식의 파일과 jpg,png형식의 파일을 PDF형식의 파일로 변환.

**사용한 라이브러리 :**  
 - itextpdf version 5  
 - Commons-io  
 - github의 barteksc 의 android-pdf-viewer
 
 ### 다음과 같은 기능들을 수행합니다.
 
 **저장소 접근**  
 PDF 생성을 제외한 모든 기능에는 다음과 같은 기능이 포함되어있습니다.  
 아래 코드는 실제 작성된 코드입니다.
 ```java
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
 ```
 모든 기능에는 권한에 대한 과정이 포함되어있습니다.  
 필자는 저장소로 접근하여 선택한 파일의 확장자만을 추출하여 확장자별로 이후 메소드를 진행했습니다.  
 
 **저장폴더 생성**  
 모든 기능의 결과로 생성된 파일들은 해당 폴더로 저장됩니다.
 ```java
 File root = new File(Environment.getExternalStorageDirectory(),"PDF Folder 2021");
 if ( !root.exists()) root.mkdir();
 ```
 
 **PDF 생성**  
 사용자가 직접 타이핑하여 PDF 파일을 생성합니다.  
 아래 코드는 실제 작성된 코드입니다.
 ```java
    // 생성될 파일의 저장주소와 이름을 담고있는 file 객체 생성.
    File file = new File(root, filename + ".pdf");
    Document document = new Document();
      try {
        // 한글 깨짐 현상을 해결하기 위해 폰트를 직접 지정했습니다.
        BaseFont bfont = BaseFont.createFont(fontpath,BaseFont.IDENTITY_H,BaseFont.EMBEDDED );
        Font font = new Font(bfont);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        String mText = et_writetext.getText().toString();
        document.add(new Paragraph(mText,font));
        document.close();
     }catch (Exception e){           
      }
 ```
 폰트의 저장위치는 다음과 같습니다.
```java
String fontpath = "res/font/malgun.ttf";
```
파일 이름은 AlertDialog를 사용하여 그곳에서 정하도록했습니다.
```java
 // 파일 이름을 정하는과정을 다이얼로그로 정했습니다.
 AlertDialog.Builder dialog = new AlertDialog.Builder(WritePdfActivity.this);
 dialog.setTitle("파일 이름");
```
**ConveretPDF [TXT]**  
txt형식의 텍스트 파일을 PDF형식의 파일로 변환합니다.  
아래 코드는 실제 작성된 코드입니다.
```java
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
                // 하지만 읽지못할뿐 읽는과정은 그대로 진행되기 때문에 문단과 문단 사이에 과도한 \n가 추가됨.
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
```
위 코드중 해당 readline의 주석처럼 문제점이 있습니다.  
readline()은 텍스트행을 읽지만 "\n"을 만날경우 종료됩니다.즉, 문단과 문단사이에 존재하는 \n을 읽지못합니다.  
때문에 결과물이 시작적으로 복잡해보이고 한눈에 들어오지 않아 reanline()다음에 "\n"을 입력하는 과정을 추가했습니다.  

**ConvertPDF [IMG]**  
jpg,png형식의 이미지 파일을 PDF형식의 파일로 변환합니다.  
아래 코드는 실제 작성된 코드입니다.
```java
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
  }catch (FileNotFoundException e) {
    e.printStackTrace();
    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    Log.e("Result_Error",e.getMessage());
  }catch (IOException e) {
    e.printStackTrace();
    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    Log.e("Result_Error",e.getMessage());
  }
// Document 종료.
pdfDocument.close();
```
코드 중간에 bitmap.getWidth 에서 null을 반환하여 오류가 나는 경우가 있었습니다.  
해당 오류는 Manifest <Application 밑에 android:requestLegacyExternalStorage="true" 를 추가하여 해결했습니다.  
하지만 이러한 방법이 옳은 방법인지는 모르겠습니다.  

**PDF 뷰어**  
PDFViewer를 추가했습니다.  
아래 코드는 실제 작성된 코드입니다.
```java
File file = new File(myPath);

// dependencies밑 'com.github.barteksc:android-pdf-viewer:2.8.2' 추가.
pdfviewer.fromFile(file)
    .defaultPage(0)
    .enableAnnotationRendering(true)
    .scrollHandle(new DefaultScrollHandle(this))
    .spacing(2)
    .load();
```

**UI**  
기본적인 UI는 다음과 같습니다.  
<a href="#"><img src="https://github.com/theiogh/PDF-Maker-and-Viewer/blob/master/PDF%20Maker%20and%20Viewer.png" width="400px" alt="PDF Maker and Viewer_UI"></a>










