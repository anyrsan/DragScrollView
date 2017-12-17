/*
 * Copyright 2015 chenupt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 */

package any.dragsrcollview.widget;

import android.content.Context;
import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by anyrsan on 2017/12/12.
 * TopView
 * BottomView
 */
public class DragScrollView extends NestedScrollView {

    private DragScrollHelper scrollHelper;
    private LinearLayout subView;

    private View bootView;      // 底部评论列表

    private Scroller mScroller;
    private int mTouchSlop;         //表示滑动的时候，手的移动要大于这个距离才开始移动控件。
    private int mMaximumVelocity;   //允许执行一个fling手势动作的最大速度值
    private View mHeadView;         //需要被滑出的头部
    private int mCurY;              //当前已经滚动的距离
    private VelocityTracker mVelocityTracker;
    private boolean mDisallowIntercept;  //是否允许拦截事件

    private int minHeight = 0;

    // 注意bottom距离
    private int bottomTopHeight = 0;

    private boolean isInitBottomHeight = false;


    private int mTopPaddingHeight = 0;


    public DragScrollView(Context context) {
        this(context, null);
    }

    public DragScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();   //表示滑动的时候，手的移动要大于这个距离才开始移动控件。
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity(); //允许执行一个fling手势动作的最大速度值
        scrollHelper = new DragScrollHelper();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        subView = (LinearLayout) getChildAt(0);
        mHeadView = subView.getChildAt(0);
        bootView = subView.getChildAt(1);
        bootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottomTopHeight = ((ViewGroup) bootView).getChildAt(0).getHeight() - 100;     // 注意这里
                minHeight = getHeight() / 2 + bottomTopHeight;

                bootView.setLayoutParams(new LinearLayout.LayoutParams(getWidth(), getHeight()));
                bootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                if (isInitBottomHeight) {
                    bootView.setLayoutParams(new LinearLayout.LayoutParams(getWidth(), minHeight));
                }
            }
        });
    }


    // 写算法  recycleView 的高度  item   主动发送完评论，

    private float mDownX;  //第一次按下的x坐标
    private float mDownY;  //第一次按下的y坐标
    private float mLastY;  //最后一次移动的Y坐标
    private boolean verticalScrollFlag = false;   //是否允许垂直滚动
    private boolean mEvent = false;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return true;
    }

    /**
     * 说明：一旦dispatTouchEvent返回true，即表示当前View就是事件传递需要的 targetView，事件不会再传递给
     * 其他View，如果需要将事件继续传递给子View，可以手动传递
     * 由于dispatchTouchEvent处理事件的优先级高于子View，也高于onTouchEvent,所以在这里进行处理
     * 好处一：当有子View，并且子View可以获取焦点的时候，子View的onTouchEvent会优先处理，如果当前逻辑
     * 在onTouchEnent中，则事件无法到达，逻辑失效
     * 好处二：当子View是拥有滑动事件时，例如ListView，ScrollView等，不需要对子View的事件进行拦截，可以
     * 全部让该父控件处理，在需要的地方手动将事件传递给子View，保证滑动的流畅性，结尾两行代码就是证明：
     * super.dispatchTouchEvent(ev);
     * return true;
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float currentX = ev.getX();                   //当前手指相对于当前view的X坐标
        float currentY = ev.getY();                   //当前手指相对于当前view的Y坐标
        float shiftX = Math.abs(currentX - mDownX);   //当前触摸位置与第一次按下位置的X偏移量
        float shiftY = Math.abs(currentY - mDownY);   //当前触摸位置与第一次按下位置的Y偏移量
        float deltaY;                                 //滑动的偏移量，即连续两次进入Move的偏移量
        obtainVelocityTracker(ev);                    //初始化速度追踪器
        switch (ev.getAction()) {
            //Down事件主要初始化变量
            case MotionEvent.ACTION_DOWN:
                mDisallowIntercept = false;
                verticalScrollFlag = false;
                mDownX = currentX;
                mDownY = currentY;
                mLastY = currentY;
                mScroller.abortAnimation();
                mEvent = true;
                super.dispatchTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDisallowIntercept)
                    break;
                deltaY = mLastY - currentY; //连续两次进入move的偏移量
                mLastY = currentY;
                if (shiftX > mTouchSlop && shiftX > shiftY) {
                    //水平滑动
                    verticalScrollFlag = false;
                } else if (shiftY > mTouchSlop && shiftY > shiftX) {
                    //垂直滑动
                    verticalScrollFlag = true;
                }
                /**
                 * 这里要注意，对于垂直滑动来说，给出以下三个条件
                 * 头部没有固定，允许滑动的View处于第一条可见，当前按下的点在头部区域
                 * 三个条件满足一个即表示需要滚动当前布局，否者不处理，将事件交给子View去处理
                 */
                try {
                    if (verticalScrollFlag) {
                        if (scrollHelper.isTop() && !isStickied()) {
                            scrollBy(0, (int) ((deltaY + 0.5) * 1.1));
                            invalidate();
                            // 自己处理，取消
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                            super.dispatchTouchEvent(ev);
                        } else if (isStickied()) {
                            //改写down
                            // 改写move
                            return super.dispatchTouchEvent(resetMotionEvent(ev));
                        } else {
                            super.dispatchTouchEvent(ev);
                        }
                    } else {
                        // 横向滑动
                        super.dispatchTouchEvent(ev);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (verticalScrollFlag && (!isStickied() || scrollHelper.isTop())) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity); //1000表示单位，每1000毫秒允许滑过的最大距离是mMaximumVelocity
                    float yVelocity = mVelocityTracker.getYVelocity();  //获取当前的滑动速度
                    //根据当前的速度和初始化参数，将滑动的惯性初始化到当前View，至于是否滑动当前View，取决于computeScroll中计算的值
                    //这里不判断最小速度，确保computeScroll一定至少执行一次
                    mScroller.fling(0, getScrollY(), 0, (int) -yVelocity, 0, 0, 0, Integer.MAX_VALUE);
                    invalidate();  //更新界面，该行代码会导致computeScroll中的代码执行
                    //阻止快读滑动的时候点击事件的发生，滑动的时候，将Up事件改为Cancel就不会发生点击了
                    if (!isStickied()) {
                        int action = ev.getAction();
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        boolean dd = super.dispatchTouchEvent(ev);
                        ev.setAction(action);
                        return dd;
                    }
                }
                recycleVelocityTracker();
                //手动将事件传递给子View，让子View自己去处理事件
                super.dispatchTouchEvent(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
                recycleVelocityTracker();
                //手动将事件传递给子View，让子View自己去处理事件
                super.dispatchTouchEvent(ev);
                break;
            default:
                break;
        }
        //消费事件，返回True表示当前View需要消费事件，就是事件的TargetView
        return true;
    }


    public MotionEvent resetMotionEvent(MotionEvent ev) {
        if (mEvent) {
            int action = ev.getAction();
            ev.setAction(MotionEvent.ACTION_CANCEL);
            super.dispatchTouchEvent(ev);
            ev.setAction(MotionEvent.ACTION_DOWN);
            super.dispatchTouchEvent(ev);
            ev.setAction(action);
        }
        mEvent = false;
        return ev;
    }


    /**
     * 头部是否已经固定
     */
    public boolean isStickied() {
        return mCurY == getHeadViewHeight();
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int currY = mScroller.getCurrY();
            scrollTo(0, currY);  //将外层布局滚动到指定位置
            invalidate();        //移动完后刷新界面
        }
    }


    public void setTopPaddingHeight(int mTopPaddingHeight) {
        this.mTopPaddingHeight = mTopPaddingHeight;
    }

    private int getHeadViewHeight() {
        return mHeadView.getHeight() - mTopPaddingHeight;
    }

    /**
     * 对滑动范围做限制
     */
    @Override
    public void scrollTo(int x, int y) {
        y = Math.max(0, Math.min(y, getHeadViewHeight()));
        mCurY = y;
        super.scrollTo(x, y);
    }


    private DragScrollHelper.ScrollableContainer scrollableContainer;

    public void setCurrentScrollableContainer(DragScrollHelper.ScrollableContainer scrollableContainer) {
        this.scrollableContainer = scrollableContainer;
        scrollHelper.setCurrentScrollableContainer(scrollableContainer);
    }

}
