package com.example.newstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.newstest.Adapter.NewsAdapter;
import com.example.newstest.Beans.News;
import com.example.newstest.Tasks.GetNewsTask;
import com.example.newstest.Utils.JSONUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String NewsURL = "https://api.jisuapi.com/news/get";
    private static final String APIkey = "8a9ec006acc43dd8";
    SharedPreferences sp;

    private List<News> newsList = new ArrayList<>();
    UserOpenHelper helper;
    SQLiteDatabase db;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message message) {
            super.handleMessage(message);
            String response = (String) message.obj;
            Log.d(TAG, "response length = " + response.length());
            try {
                JSONObject obj = new JSONObject(response);
                String status = obj.getString("status");
                Log.d(TAG, "status=" + status);
                String msg = obj.getString("msg");
                Log.d(TAG, "msg=" + msg);
                if (status.equals("0") && msg.equals("ok")) {
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            newsList = JSONUtil.parseJSON(response);
            getNewsContent(newsList);
        }
    };

    private void getNewsContent(List<News> newsList) {
        Intent intent = new Intent(MainActivity.this, NewsListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("newsList", (ArrayList<? extends Parcelable>) newsList);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button get = findViewById(R.id.get);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.logOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  String sql = String.format("delete from res ");
                // db.execSQL(sql);
                //  MainActivity.this.finish();
                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                sp.edit().remove("id").commit();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        get.setOnClickListener(this);

//        adapter.setOnclick(new NewsAdapter.onItemClick() {
//            @Override
//            public void onclick(int p) {
//                String sql = String.format("insert into res(_ID,_DATE,_HEAD,_MSG,_TITLE) values('%s','%s','%s','%s','%s')", sp.getInt("id", 0), newsList.get(p).getTime(), newsList.get(p).getContent(), newsList.get(p).getPic(), newsList.get(p).getTitle());
//                try {
//                    db.execSQL(sql);
////                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
////                    Intent intent = new Intent(AddPage.this, BroadcastAlarm.class);
////                    PendingIntent sender = PendingIntent.getBroadcast(AddPage.this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
////                    //if(interval==0)
////                    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, sender);
//                    Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getCollectList();
    }

    public void getCollectList() {
        newsList.clear();
        sp = getSharedPreferences("data", MODE_PRIVATE);
        helper = new UserOpenHelper(this);
        db = helper.getWritableDatabase();
        String sql = String.format("select _HEAD,_MSG,_TITLE from res where _UID=" + sp.getInt("id", -1) + " ");
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                News a = new News();
                a.setContent(cursor.getString(0));
                a.setPic(cursor.getString(1));
                a.setTitle(cursor.getString(2));
                a.setTime(new Date(System.currentTimeMillis()));
                newsList.add(a);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.news_list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        NewsAdapter adapter = new NewsAdapter(newsList, this, false);
        View footer = LayoutInflater.from(this).inflate(R.layout.footview, recyclerView, false);
        adapter.setFooterView(footer);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.get) {
            sendRequestWithHttp();
        }
    }


    private void sendRequestWithHttp() {
        String requestURL = String.format(NewsURL + "?channel=%s&start=0&num=15&appkey=%s", "头条", APIkey);
        Log.d(TAG, "requestURL=" + requestURL);
        GetNewsTask task = new GetNewsTask(handler);
        //task.setListener(listener);
        task.execute(requestURL);
    }

    private void outputNewsContent(List<News> newsList) {
        // write to txt file
        FileOutputStream out;
        BufferedWriter writer = null;
        try {
            out = this.openFileOutput("data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            for (News news : newsList) {
                //Log.d(TAG,news.toString());
                writer.write(news.toString() + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "File output");
    }
}