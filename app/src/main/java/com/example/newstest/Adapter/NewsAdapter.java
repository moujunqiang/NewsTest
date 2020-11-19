package com.example.newstest.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newstest.Beans.News;
import com.example.newstest.NewsContentActivity;
import com.example.newstest.R;
import com.example.newstest.Tasks.DownloadPicTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "NewsAdapter";
    private List<News> mNewsList;
    private Context context;
    private View footerView;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private boolean hasMore;
    private boolean fadeTips = false;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            HashMap<String, Object> hashMap = (HashMap<String, Object>) msg.obj;
            ImageView imageView = (ImageView) hashMap.get("imageview");
            Drawable drawable = (Drawable) hashMap.get("drawable");
            imageView.setImageDrawable(drawable);
        }
    };

    public NewsAdapter(List<News> newsList, Context context, boolean hasMore) {
        mNewsList = newsList;
        this.context = context;
        this.hasMore = hasMore;
    }

    public void setFooterView(View footerView) {
        Log.d(TAG, "setFootView");
        this.footerView = footerView;
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (footerView != null && viewType == TYPE_FOOTER) {
            return new FootHolder(footerView);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        final NormalHolder holder = new NormalHolder(view);
        holder.newsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                News news = mNewsList.get(position);
                Log.d(TAG, "Date=" + news.getTime());
                Toast.makeText(view.getContext(), news.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(parent.getContext(), NewsContentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("news", news);
                intent.putExtras(bundle);
                parent.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            if (holder instanceof NormalHolder) {
                final News news = mNewsList.get(position);
                ImageView imageView = ((NormalHolder) holder).newsImage;
                DownloadPicTask task = new DownloadPicTask(imageView, handler);
                task.execute(news.getPic());
                ((NormalHolder) holder).newsImage.setTag(news.getPic());
                ((NormalHolder) holder).newsTitle.setText(news.getTitle());
                return;
            }
        } else {
            ((FootHolder) holder).tips.setVisibility(View.VISIBLE);
            if (hasMore == true) {
                ((FootHolder) holder).itemView.setVisibility(View.VISIBLE);
                fadeTips = false;
                if (mNewsList.size() > 0) {
                    ((FootHolder) holder).tips.setText("Loading More...");
                }
            } else {
                ((FootHolder) holder).itemView.setVisibility(View.GONE);

                if (mNewsList.size() > 0) {
                    ((FootHolder) holder).tips.setText("No More News");
                    ((FootHolder) holder).tips.setVisibility(View.GONE);
                    fadeTips = true;
                    hasMore = true;
                }
            }
        }
    }

    public boolean isFadeTips() {
        return fadeTips;
    }

    public void resetData() {
        mNewsList = new ArrayList<>();
    }

    public void updateList(List<News> newData, boolean hasMore) {
        if (newData != null) {
            mNewsList.addAll(newData);
        }
        this.hasMore = hasMore;
        //notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (footerView == null) {
            return mNewsList.size();
        } else {
            return mNewsList.size() + 1;
        }
    }

    public int getRealLastPosition() {
        return mNewsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }


    static class NormalHolder extends RecyclerView.ViewHolder {
        View newsView;
        ImageView newsImage;
        TextView newsTitle;

        public NormalHolder(@NonNull View itemView) {
            super(itemView);
            newsView = itemView;
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
        }
    }

    static class FootHolder extends RecyclerView.ViewHolder {
        TextView tips;
        View itemView;

        public FootHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
            tips = itemView.findViewById(R.id.tips);
        }
    }

}
