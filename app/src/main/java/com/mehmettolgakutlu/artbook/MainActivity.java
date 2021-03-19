package com.mehmettolgakutlu.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        nameArray = new ArrayList<String>();
        idArray = new ArrayList<Integer>();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,nameArray); //Sadece string göstereceğimiz zaman kullanabildiğimiz layout.
        listView.setAdapter(arrayAdapter); // Listview'de string gösteriyoruz.

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // ListView'a click özelliği tanımlıyoruz.
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // Hangi positiona tıklandıysa yapılacak işlemler altına yazılır.
                Intent intent = new Intent(MainActivity.this,MainActivity2.class); // Hangi activty den hangisine gidileceği...
                intent.putExtra("artId",idArray.get(position)); // Yollacak ilk bilgi id bilgisi olacak
                intent.putExtra("info","old"); // Eski yani kayıtlı olan sanatı açmak için kullanılan method.
                startActivity(intent); // Activity yi çalıştır.

            }
        });

        getData();
    }

    // Menü methodları.
    //Verileri 2. activty'nin içinden çekiyoruz.

    public void getData() {

        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null); // Yoksa yeni database oluştur varsa burada aç.

            Cursor cursor = database.rawQuery("SELECT * FROM arts",null); // İmleç. Bu içleç kullanılarak database'de sorgulama yapılabiliyor.
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext()) { // Verileri sırasıyla getiriyoruz.
                nameArray.add(cursor.getString(nameIx));
                idArray.add(cursor.getInt(idIx));
            }

            arrayAdapter.notifyDataSetChanged(); // Dizilere yeni veri eklendi ve bunları listede gösterme komutu.

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace(); // Bir sıkıntı varsar bunu göster.
        }



        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Aktivitede hangi menü gösterilecekse onu belirtiyoruz.

        //Inflater: Şişirmek
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art,menu); // Oluşturulan menü buraya bağlanır.

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // Kullanıcının seçtiği item da neler yapılacağı belirtilir.

        if (item.getItemId() == R.id.add_art_item){ // add_art_item a tıklanırsa ne yapılacağı belirtilir.
            Intent intent = new Intent(MainActivity.this,MainActivity2.class);
            intent.putExtra("info","new"); // Yeni art-sanat eklemek için kullanılacak method.
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}