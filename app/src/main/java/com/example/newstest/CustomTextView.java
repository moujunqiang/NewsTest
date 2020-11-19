package com.example.newstest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.newstest.Tasks.DownloadPicTask;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CustomTextView extends LinearLayout {

    private Context mContext;
    private TypedArray mTypedArray;
    private LayoutParams params;
    private ImageView imageView;


    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.setOrientation(LinearLayout.VERTICAL);
        mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.customTextView);
    }

    public void setText(ArrayList<HashMap<String, String>> datas) {
        //遍历ArrayList
        for (HashMap<String, String> hashMap : datas) {
            //获取key为"type"的值
            String type = hashMap.get("type");
            //如果value=imaeg
            if (type.equals("image")) {
                Log.d("CustomTextView", hashMap.get("value"));
                //获取自定义属性属性
                int imagewidth = mTypedArray.getDimensionPixelOffset(R.styleable.customTextView_image_width, 100);
                int imageheight = mTypedArray.getDimensionPixelOffset(R.styleable.customTextView_image_height, 100);
                imageView = new ImageView(mContext);
                params = new LayoutParams(imagewidth, imageheight);
                params.gravity = Gravity.CENTER_HORIZONTAL;    //居中
                imageView.setLayoutParams(params);
                addView(imageView);
                DownloadPicTask task = new DownloadPicTask(imageView, handler);
                task.execute(hashMap.get("value"));
                //new DownloadPicThread(imageView, hashMap.get("value")).start();
            } else {
                float textSize = mTypedArray.getDimension(R.styleable.customTextView_textSize, 16);
                int textColor = mTypedArray.getColor(R.styleable.customTextView_textColor, 0xFF0000FF);
                TextView textView = new TextView(mContext);
                textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
                textView.setText(Html.fromHtml(hashMap.get("value")));
                textView.setTextSize(textSize);        //设置字体大小
                textView.setTextColor(textColor);    //设置字体颜色
                addView(textView);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            HashMap<String, Object> hashMap = (HashMap<String, Object>) msg.obj;
            ImageView imageView = (ImageView) hashMap.get("imageview");
            Drawable drawable = (Drawable) hashMap.get("drawable");
            imageView.setImageDrawable(drawable);
        }
    };

//    @SuppressLint("HandlerLeak")
//    private Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            Log.d("Handler", "Handler run in thread " + Thread.currentThread().toString());
//            HashMap<String, Object> hashMap = (HashMap<String, Object>) msg.obj;
//            ImageView imageView = (ImageView) hashMap.get("imageView");
//            //LayoutParams params = new LayoutParams(msg.arg1, msg.arg2);
//            params.gravity = Gravity.CENTER_HORIZONTAL;    //居中
//            //imageView.setLayoutParams(params);
//            Drawable drawable = (Drawable) hashMap.get("drawable");
//            imageView.setImageDrawable(drawable);        //显示图片
//            Toast.makeText(mContext, "Image Loaded", Toast.LENGTH_SHORT).show();
//        }
//    };
//
//    /**
//     * 定义一个线程类，异步加载图片
//     *
//     * @author Administrator
//     */
//    private class DownloadPicThread extends Thread {
//        private ImageView imageView;
//        private String mUrl;
//
//
//        public DownloadPicThread(ImageView imageView, String mUrl) {
//            super();
//            this.imageView = imageView;
//            this.mUrl = mUrl;
//        }
//
//        private Drawable loadImageFromNetwork(String imageUrl) {
//            Drawable drawable = null;
//            try {
//                // 可以在这里通过文件名来判断，是否本地有此图片
//                Log.d("LoadImage", imageUrl);
//                drawable = Drawable.createFromStream(new URL(imageUrl).openStream(), "image.jpg");
//            } catch (IOException e) {
//                Log.d("test", e.getMessage());
//            }
//            if (drawable == null) {
//                Log.d("test", "null drawable");
//            } else {
//                Log.d("test", "not null drawable");
//            }
//            return drawable;
//        }
//
//        @Override
//        public void run() {
//            Log.d("Download", "Download run in thread " + Thread.currentThread().toString());
//            Drawable drawable = null;
//            int newImgWidth = 0;
//            int newImgHeight = 0;
//            try {
//                drawable = loadImageFromNetwork(mUrl);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            //使用Handler更新UI
//            if (drawable != null) {
//                Message msg = handler.obtainMessage();
//                HashMap<String, Object> hashMap = new HashMap<>();
//                hashMap.put("imageView", imageView);
//                hashMap.put("drawable", drawable);
//                msg.obj = hashMap;
//                msg.arg1 = newImgWidth;
//                msg.arg2 = newImgHeight;
//                handler.sendMessage(msg);
//            }
//        }
//    }
}

