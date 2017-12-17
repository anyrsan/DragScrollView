package any.dragsrcollview.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import any.dragsrcollview.R;
import any.dragsrcollview.ui.fragment.BaseFragment;
import any.dragsrcollview.ui.fragment.RecyclerViewFragment;
import any.dragsrcollview.ui.fragment.ScrollViewFragment;
import any.dragsrcollview.ui.fragment.WebViewFragment;
import any.dragsrcollview.utils.StatusBarUtils;
import any.dragsrcollview.widget.DragScrollHelper;
import any.dragsrcollview.widget.DragScrollView;
import any.dragsrcollview.widget.PagerSlidingTabStrip;

/**
 * Created by anyrsan on 2017/12/17.
 */

public class MainActivity extends AppCompatActivity implements DragScrollHelper.ScrollableContainer {

    PagerSlidingTabStrip slidingTabStrip;

    ViewPager viewPager;

    DragScrollView dragScrollView;

    MyAdapter adapter;

    View dtView;

    View toolbar;

    int stopHeight = 0;

    int topHeight = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.ct_relativelayout);
        toolbar.setBackgroundColor(Color.TRANSPARENT);

        dtView = findViewById(R.id.dt_view);
        dtView.setBackgroundColor(Color.TRANSPARENT);

        stopHeight = StatusBarUtils.dp2px(getResources(), 48);
        topHeight = StatusBarUtils.dp2px(getResources(), 300);

        viewPager = findViewById(R.id.dbl_viewpager);
        adapter = new MyAdapter(getSupportFragmentManager(), getListFragment());
        viewPager.setAdapter(adapter);


        slidingTabStrip = findViewById(R.id.dbl_pagerslidingtabstrip);
        slidingTabStrip.setJunfen(true);
        int color = getResources().getColor(R.color.colorPrimary);
        int selectColor = getResources().getColor(R.color.colorAccent);
        slidingTabStrip.setTabTextColor(color, selectColor);

        slidingTabStrip.setViewPager(viewPager, new PagerSlidingTabStrip.IPageSlidingTab() {
            @Override
            public void slideTop() {
                scroll2Top();
            }
        });

        dragScrollView = findViewById(R.id.am_dragscrollview);
        dragScrollView.setCurrentScrollableContainer(this);
        dragScrollView.setTopPaddingHeight(stopHeight);
        dragScrollView.setOnScrollChangeListener(mNestedOnScrollChangeListener);
    }


    NestedScrollView.OnScrollChangeListener mNestedOnScrollChangeListener = new NestedScrollView.OnScrollChangeListener() {

        @Override
        public void onScrollChange(NestedScrollView nestedScrollView, int x, int y, int oldX, int oldY) {
            if (y <= 0) {   // 到达顶部
                toolbar.setBackgroundColor(Color.TRANSPARENT);
                dtView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                // 到 banner 底部的可滑动距离
                int scroll2BottomY = topHeight - stopHeight;
                if (y > 0 && y <= scroll2BottomY) {
                    float scale = y * 1.0f / scroll2BottomY;
                    int dex = y - oldY;
                    if (dex < 0) {      // 渐变为透明
                    } else {            // 渐变为红色
                        if (y == scroll2BottomY) {
                            scale = 1;
                        }
                    }
                    int changeColor2;
                    float alpha;
                    if (scale == 1) {
                        changeColor2 = getResources().getColor(R.color.colorPrimary);
                    } else {
                        alpha = scale * 255;
                        changeColor2 = changeColorAlpha(getResources().getColor(R.color.colorPrimary), (int) alpha);
                    }
                    dtView.setBackgroundColor(changeColor2);
                } else if (y <= topHeight) { // 防止快速向下滑，标题栏本应为不透明主题色，但导致标题栏的渐变颜色过浅
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        }
    };

    public static int changeColorAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


    @Override
    public View getScrollableView() {
        return adapter.getItem(viewPager.getCurrentItem()).getScrollableView();
    }

    public void scroll2Top() {
        adapter.getItem(viewPager.getCurrentItem()).scroll2Top();
    }

    public List<BaseFragment> getListFragment() {
        List<BaseFragment> lists = new ArrayList<>();
        lists.add(RecyclerViewFragment.getInstance());
        lists.add(ScrollViewFragment.getInstance());
        lists.add(WebViewFragment.getInstance());
        lists.add(RecyclerViewFragment.getInstance());
        return lists;
    }


    public class MyAdapter extends FragmentPagerAdapter {

        private final String[] titles = {"Table1", "Table2", "Table3", "Table4"};

        private List<BaseFragment> listsFgs;

        public MyAdapter(FragmentManager fm, List<BaseFragment> listsFgs) {
            super(fm);
            this.listsFgs = listsFgs;
        }


        @Override
        public BaseFragment getItem(int position) {
            return listsFgs.get(position);
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }


}
