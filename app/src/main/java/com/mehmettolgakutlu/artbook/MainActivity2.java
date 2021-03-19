package com.mehmettolgakutlu.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {

    Bitmap selectedImage;
    ImageView imageView;
    EditText artNameText, painterNameText, yearText;
    Button button;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView);
        artNameText = findViewById(R.id.artNameText);
        painterNameText = findViewById(R.id.painterNameText);
        yearText = findViewById(R.id.yearText);
        button = findViewById(R.id.button);

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info"); // Main activity deki infoyu getir.

        if (info.matches("new")) { // Yeni bir şey eklenmeye calışıyor ise bu komut değil ise else bloğu çalışır.

            artNameText.setText("");
            painterNameText.setText("");
            yearText.setText("");
            button.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage); // Drawble içindeki resimler decode edilir.
            imageView.setImageBitmap(selectImage);

        } else {
            int artId = intent.getIntExtra("artId",1); // Yanlış bir işlem gerçekleşirse ilk resmi gösterir
            button.setVisibility(View.INVISIBLE); // Eski sanat gösteriliyorsa butonu görünmez yap.

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[]{String.valueOf(artId)}); // artId stringe çevrilir ve filtreleme olarak buraya verir.

                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {

                    artNameText.setText(cursor.getString(artNameIx));
                    painterNameText.setText(cursor.getString(painterNameIx));
                    yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length); // Byte dizisini görsel hale getiriyoruz.
                    imageView.setImageBitmap(bitmap);

                }

                cursor.close();

            } catch (Exception e) {

            }
        }

    }


    public void selectImage(View view){ // Buraya tıklandığında depolama iznini soruyoruz 1 defalığına.

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { // İzin var mı yok mu kontrol et komutu.

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        } else { // İzin verilmiş ise ne yapılacağı.

            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // Galeriye git komutu
            startActivityForResult(intentToGallery,2); // Sonucu bize getiren komut.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // İzin istendiğinde sonucunda ne olacağı.

        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Grantresaultun içinde eleman var mı önce o kontrol ediliyor.
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2); // Galeriye yönlendirme aktivitesi başlatıyoruz.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) { // Resim seçildi ve geriye null dönmüyor ise yapılacak işlemler...

            Uri imageData = data.getData(); // URI adresini getiriyor

            try {

                if (Build.VERSION.SDK_INT >= 28) { // SDK version kontrolü.

                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData); // URI'ı bitmap'a çevirmek için kullanılır.
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);

                } else {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage); // Version 28 den küçükse else'i uygula. Büyükse if çalışsın.
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view){ // Save işlemleri...

        String artName = artNameText.getText().toString();
        String painterName = painterNameText.getText().toString();
        String year = yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // Görseli alıp veriye çevirmek işlemleri.
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream); // Görselin şeklini, kalitesini belirtip çeviriyoruz.
        byte[] byteArray = outputStream.toByteArray();

        try {

            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null); // Arts isminde veri tabanı oluşturuyoruz.
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)"); // Veritabanımıza tablo oluşturuyoruz.

            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        } catch (Exception e) {

        }


        Intent intent = new Intent(MainActivity2.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Daha önceki tüm aktiviteler kapatılır.
        startActivity(intent);

        // finish(); // Aktiviteyi tamamen bitirme komutu.

    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) { // Seçilen image'a maximum boyutunu vermek için kullanılan fonksiyon.

        int width = image.getWidth();
        int height = image.getHeight(); // Bitmap'lerin genişlik ve yüksekliklerini alma...

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) { // Sonuç 1 den büyükse genişliği daha büyük olduğu için remin yatay olduğu anlaşılır.
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true); // Hangi görseli küçülteceğini soruyor.
    }


}