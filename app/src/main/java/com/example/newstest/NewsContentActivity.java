package com.example.newstest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newstest.Beans.News;
import com.example.newstest.Utils.HTMLUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class NewsContentActivity extends AppCompatActivity {
    private boolean collection = false;
    SQLiteDatabase db;
    News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content);
        Intent intent = getIntent();
        news = intent.getParcelableExtra("news");
        TextView newsTitle = findViewById(R.id.news_title);
        CustomTextView textView = findViewById(R.id.newsView);
        newsTitle.setText(news.getTitle());
        ArrayList<HashMap<String, String>> data = HTMLUtil.parseHTML(news.getContent());
        textView.setText(data);
        db = new UserOpenHelper(this).getWritableDatabase();

        String sql = String.format("select _HEAD,_MSG,_TITLE from res where _TITLE= ?");
        Cursor cursor = db.rawQuery(sql, new String[]{news.getTitle()});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                collection = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (collection) {
            menu.add(0, 0, 0, "cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        } else {
            menu.add(0, 0, 0, "collection").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);

                if (sp.getInt("id", -1) == -1) {
                    Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (collection) {
                    collection = false;
                    String sql = String.format("delete from res where _TITLE= ?");
                    db.execSQL(sql, new String[]{news.getTitle()});
                } else {
                    ContentValues values = new ContentValues();
                    values.put("_HEAD", news.getContent());
                    values.put("_MSG", news.getPic());
                    values.put("_TITLE", news.getTitle());
                    values.put("_UID", sp.getInt("id", -1));

                    db.insert("res", null, values);
                    Toast.makeText(NewsContentActivity.this, "success", Toast.LENGTH_SHORT)
                            .show();
                    collection = true;
                }
                invalidateOptionsMenu();

                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}