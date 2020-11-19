package com.example.newstest.Tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.example.newstest.Utils.HttpUtil;

public class GetNewsTask extends AsyncTask<String, Void, String> {

    private static String TAG = "GetNewsTask";
    private Handler handler;

    public GetNewsTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected String doInBackground(String... strings) {
        return HttpUtil.HttpGet(strings[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        Message msg = handler.obtainMessage();
        msg.obj = s;
        handler.sendMessage(msg);
    }


}
