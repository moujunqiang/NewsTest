package com.example.newstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.newstest.Adapter.NewsAdapter;
import com.example.newstest.Beans.News;
import com.example.newstest.Tasks.GetNewsTask;
import com.example.newstest.Utils.JSONUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class NewsListActivity extends AppCompatActivity implements OnBannerListener {

    private static final String TAG = "NewsListActivity";
    private SwipeRefreshLayout refreshLayout;
    private NewsAdapter adapter;
    private List<News> newsList;
    private int PAGE_COUNT = 15;
    private Banner banner;
    private RecyclerView recyclerView;

    private static final String NewsURL = "https://api.jisuapi.com/news/get";
    private static final String APIkey = "8a9ec006acc43dd8";

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            newsList = JSONUtil.parseJSON((String) msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);
        initNewsList();
        initBanner();
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        recyclerView = findViewById(R.id.news_list_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NewsAdapter(newsList, this, newsList.size() > 0 ? true : false);
        setFooterView(recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        nestedScrollView.setOnScrollChangeListener(new ScrollListener(adapter, layoutManager) {
            @Override
            public void onLoadMore() {
                Log.d(TAG, "LoadMore");
                updateRecyclerView(adapter.getRealLastPosition(), adapter.getRealLastPosition() + PAGE_COUNT);
            }
        });
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNews();
            }
        });
    }


    private void initBanner() {
        List<String> bannerList = new ArrayList<>();
        for (News item : newsList.subList(0, 5)) {
            bannerList.add(item.getPic());
        }
        List<String> bannerList_Title = new ArrayList<>();
        for (News title : newsList.subList(0, 5)) {
            bannerList_Title.add(title.getTitle());
        }
        banner = findViewById(R.id.banner);
        banner.setDelayTime(4500);//间隔时间
        banner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Glide.with(context).load(path).into(imageView);
            }
        });   //设置图片加载器
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);//Style
        banner.setImages(bannerList);  //设置banner中显示图片
        banner.isAutoPlay(true);
        banner.setBannerTitles(bannerList_Title);
        banner.setOnBannerListener(this);
        banner.start();  //设置完毕后调用
    }

    private void setFooterView(RecyclerView recyclerView) {
        View footer = LayoutInflater.from(this).inflate(R.layout.footview, recyclerView, false);
        adapter.setFooterView(footer);
    }

    private void initNewsList() {
        Intent intent = getIntent();
        newsList = intent.getParcelableArrayListExtra("newsList");
    }

    @Override
    public void OnBannerClick(int position) {
        News clickedNews = newsList.get(position);
        Intent intent = new Intent(this, NewsContentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("news", clickedNews);
        intent.putExtras(bundle);
        startActivity(intent);
//        Log.i("tag", "你点了第"+position+"张轮播图");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
    }

    private void refreshNews() {
        refreshLayout.setRefreshing(true);
        adapter.resetData();
        updateRecyclerView(0, PAGE_COUNT);
        refreshLayout.setRefreshing(false);
    }

    public void updateRecyclerView(int fromIndex, int number) {
        getNews(fromIndex, number);
        List<News> newData = newsList;
        Log.d(TAG, "First Title: " + newsList.get(0).getTitle());
        if (newData.size() > 0) {
            // 然后传给Adapter，并设置hasMore为true
            adapter.updateList(newData, true);
            adapter.notifyDataSetChanged();
        } else {
            adapter.updateList(null, false);
        }
    }

    public void getNews(int fromIndex, int number) {
        String requestURL = String.format(NewsURL + "?channel=%s&start=%s&num=%s&appkey=%s", "头条", fromIndex, number, APIkey);
        Log.d(TAG, "requestURL=" + requestURL);
        GetNewsTask task = new GetNewsTask(handler);
        task.execute(requestURL);
    }

}