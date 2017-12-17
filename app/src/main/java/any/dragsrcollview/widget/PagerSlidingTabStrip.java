/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package any.dragsrcollview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

import any.dragsrcollview.R;

/**
 * Created by anyrsan on 2017/12/12.
 */
public class PagerSlidingTabStrip extends HorizontalScrollView {

    public interface IconTabProvider {
        public int getPageIconResId(int position);
    }

    // @formatter:off
    private static final int[] ATTRS = new int[]{android.R.attr.textSize,
            android.R.attr.textColor};
    // @formatter:on

    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;

    private final PageListener pageListener = new PageListener();
    public OnPageChangeListener delegatePageListener;

    private LinearLayout tabsContainer;
    private ViewPager pager;

    private int tabCount;

    private int currentPosition = 0;
    private float currentPositionOffset = 0f;

    private Paint rectPaint;
    private Paint dividerPaint;

    private int addIndex = -1;
    private int mRedPackageColor = Color.argb(255, 192, 163, 109);  // 浅黄色
    /**
     * 下划线的颜色
     */
    private int indicatorColor = ContextCompat.getColor(getContext(), R.color.white);
    private int underlineColor = 0x1A000000;
    private int dividerColor = 0x1A000000;

    private boolean shouldExpand = false;
    private boolean textAllCaps = true;

    private int scrollOffset = 20;

    private int indicatorHeight = 2;
    private int underlineHeight = 2;

    private int dividerPadding = 12;
    private int tabPadding = 15;
    private int dividerWidth = 1;

    private int tabTextSize = 13;
    private int tabSelectSize = 15;

    private int tabTextColor = ContextCompat.getColor(getContext(), R.color.white);
    private int selectTextColor = ContextCompat.getColor(getContext(), R.color.white);

    private int lastScrollX = 0;

    private int endScrollX = 0;

    private int tabBackgroundResId;

    private Locale locale;

    private boolean isFromUser = false;

    private boolean junfen = false;

    public void setJunfen(boolean junfen) {
        this.junfen = junfen;
    }

    public PagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(tabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        scrollOffset = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
        indicatorHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        underlineHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
        dividerPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
        tabPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        dividerWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);


        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColor(0, tabTextColor);

        a.recycle();

        // get custom attrs

        a = context.obtainStyledAttributes(attrs,
                R.styleable.PagerSlidingTabStrip);

        indicatorColor = a.getColor(
                R.styleable.PagerSlidingTabStrip_pstsIndicatorColor,
                indicatorColor);
        underlineColor = a.getColor(
                R.styleable.PagerSlidingTabStrip_pstsUnderlineColor,
                underlineColor);
        dividerColor = a
                .getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor,
                        dividerColor);
        indicatorHeight = a.getDimensionPixelSize(
                R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight,
                indicatorHeight);
        underlineHeight = a.getDimensionPixelSize(
                R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight,
                underlineHeight);
        dividerPadding = a.getDimensionPixelSize(
                R.styleable.PagerSlidingTabStrip_pstsDividerPadding,
                dividerPadding);
        tabPadding = a.getDimensionPixelSize(
                R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight,
                tabPadding);
        tabBackgroundResId = a.getResourceId(
                R.styleable.PagerSlidingTabStrip_pstsTabBackground,
                tabBackgroundResId);
        shouldExpand = a
                .getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand,
                        shouldExpand);
        scrollOffset = a
                .getDimensionPixelSize(
                        R.styleable.PagerSlidingTabStrip_pstsScrollOffset,
                        scrollOffset);
        textAllCaps = a.getBoolean(
                R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, textAllCaps);

        // 默认字体大小...
        tabTextSize = a.getInt(
                R.styleable.PagerSlidingTabStrip_pstsTabTextSize,
                tabTextSize);
        tabSelectSize = a.getInt(
                R.styleable.PagerSlidingTabStrip_pstsTabSelectTextSize,
                tabSelectSize);


        a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);

        defaultTabLayoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

        expandedTabLayoutParams = new LinearLayout.LayoutParams(0,
                LayoutParams.MATCH_PARENT, 1.0f);

        if (locale == null) {
            locale = getResources().getConfiguration().locale;
        }
    }

    public void setTabTextColor(int normalColorId, int selectColorId) {
        tabTextColor = normalColorId;
        selectTextColor = selectColorId;
        indicatorColor = selectColorId;
    }

    private IPageSlidingTab itab;

    public void setViewPager(ViewPager pager, IPageSlidingTab itab) {
        this.pager = null;
        this.itab = itab;
        this.pager = pager;
        if (pager.getAdapter() == null) {
            throw new IllegalStateException(
                    "ViewPager does not have adapter instance.");
        }
        this.pager.addOnPageChangeListener(pageListener);
        notifyDataSetChanged();
        if (pager.getAdapter().getCount() > 0) {
            tempTv = (TextView) tabsContainer.getChildAt(pager.getCurrentItem());

            tempTv.setTag(pager.getCurrentItem());

            handerTextView(tempTv, pager.getCurrentItem());
        }
    }


    /**
     * 更新选中的文本样式
     *
     * @param tempTv
     */
    private void handerTextView(TextView tempTv, int position) {
        if (tempTv == null)
            return;

        if (position == addIndex) {
            tempTv.setTextColor(mRedPackageColor);
            tempTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, tabTextSize);
        } else {
            tempTv.setTextColor(selectTextColor);
            tempTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, tabSelectSize);
        }


    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged() {
        tabsContainer.removeAllViews();
        tabCount = pager.getAdapter().getCount();

        if (junfen) {
            defaultTabLayoutParams.width = 0;
            defaultTabLayoutParams.weight = 1;
        }

        for (int i = 0; i < tabCount; i++) {
            if (pager.getAdapter() instanceof IconTabProvider) {
                addIconTab(i,
                        ((IconTabProvider) pager.getAdapter())
                                .getPageIconResId(i));
            } else {
                String tabText = pager.getAdapter().getPageTitle(i).toString();
                addTextTab(i, tabText);
            }
        }

        updateTabStyles();

        getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @SuppressLint("NewApi")
                    @Override
                    public void onGlobalLayout() {

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            getViewTreeObserver().removeGlobalOnLayoutListener(
                                    this);
                        } else {
                            getViewTreeObserver().removeOnGlobalLayoutListener(
                                    this);
                        }

                        currentPosition = pager.getCurrentItem();
                        scrollToChild(currentPosition, 0);
                    }
                });

    }


    private void addTextTab(final int position, String title) {
        TextView tab = new TextView(getContext());

        tab.setText(title);

        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();

        addTab(position, tab);
    }

    private void addIconTab(final int position, int resId) {

        ImageButton tab = new ImageButton(getContext());
        tab.setImageResource(resId);

        addTab(position, tab);
    }

    private void addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == pager.getCurrentItem()) {
                    if (itab != null) {
                        itab.slideTop();
                    }
                } else {
                    pager.setCurrentItem(position); // 带过度动画
                }
            }
        });

        tab.setPadding(tabPadding, 0, tabPadding, 0);
        // origin...
        //        tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);

        //zy2
        if (position == addIndex) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tab.getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            } else {
                params.weight = LayoutParams.WRAP_CONTENT;
                params.height = LayoutParams.WRAP_CONTENT;
            }
            tabsContainer.addView(tab, position, params);
        } else {
            tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
        }
    }

    // zy2

    /**
     * 更新指定的Tab的文本
     *
     * @param positon
     * @param msg
     */
    public void notifyTabText(int positon, String msg) {
        if (positon < 0)
            return;

        if (positon <= tabsContainer.getChildCount()) {
            View v = tabsContainer.getChildAt(positon);
            if (v instanceof TextView) {
                TextView tab = (TextView) v;
                tab.setText(msg);
                handerTextView(tab, positon);
            }
        }
    }


    @SuppressLint("NewApi")
    private void updateTabStyles() {
        for (int i = 0; i < tabCount; i++) {

            View v = tabsContainer.getChildAt(i);

            if (v instanceof TextView) {

                TextView tab = (TextView) v;
                tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, tabTextSize); //

                if (i == addIndex) {
                    tab.setTextColor(mRedPackageColor);
                } else {
                    tab.setTextColor(tabTextColor);
                }
            }
        }

    }

    private void scrollToChild(int position, int offset) {

        if (tabCount == 0) {
            return;
        }


        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }

        if (getScrollX() != endScrollX) {
            // 说明用户手动滑动了
            isFromUser = true;
        } else {
            isFromUser = false;
        }

        if (!isFromUser) {
            if (newScrollX != lastScrollX) {
                lastScrollX = newScrollX;
                scrollTo(newScrollX, 0);
            }
            endScrollX = getScrollX();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || tabCount == 0) {
            return;
        }

        final int height = getHeight();

        // draw indicator line

        rectPaint.setColor(indicatorColor);

        // default: line below current tab
        View currentTab = tabsContainer.getChildAt(currentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        float paddLeft = currentTab.getPaddingLeft();
        float paddRight = currentTab.getPaddingRight();


        // if there is an offset, start interpolating left and right coordinates
        // between current and next tab
        if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

            View nextTab = tabsContainer.getChildAt(currentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset)
                    * lineLeft);
            lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset)
                    * lineRight);
        }

        if (indicatorWeight > 0) {
            int w = (int) (currentTab.getWidth() - paddLeft - paddRight);
            canvas.drawRect(lineLeft + paddLeft + w / indicatorWeight, height - indicatorHeight,
                    lineRight - paddRight - w / indicatorWeight, height - indicatorBottomPadding, rectPaint);
        } else {
            canvas.drawRect(lineLeft + paddLeft, height - indicatorHeight, lineRight - paddRight, height - indicatorBottomPadding, rectPaint);
        }

        // draw underline
        //
        //		 rectPaint.setColor(underlineColor);
        //		 canvas.drawRect(0, height - underlineHeight,
        //		 tabsContainer.getWidth(),
        //		 height, rectPaint);
        //
        //		// draw divider
        //
        //		 dividerPaint.setColor(dividerColor);
        //		 for (int i = 0; i < tabCount - 1; i++) {
        //		 View tab = tabsContainer.getChildAt(i);
        //		 canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(),
        //		 height - dividerPadding, dividerPaint);
        //		 }
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {

            currentPosition = position;
            currentPositionOffset = positionOffset;

            if (tabsContainer.getChildAt(position) != null) {
                scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
            }

            invalidate();

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                // 还有一种情况就是如果当前选择的view 的getLeft() 或者 getRight() 不可见那就也要移动
                if (isFromUser) {
                    View currentTab = tabsContainer.getChildAt(currentPosition);
                    if (currentTab != null) {
                        float lineLeft = currentTab.getLeft();
                        float lineRight = currentTab.getRight();
                        if (lineLeft < getScrollX()
                                || lineRight > getScrollX() + getWidth()) {
                            endScrollX = getScrollX();
                        }
                    }
                }
                scrollToChild(pager.getCurrentItem(), 0);
                endScrollX = getScrollX();
            }

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }

            if (tempTv != null) {
                tempTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, tabTextSize);
                //                tempTv.setTextColor(tabTextColor);

                // zy2
                int pos = (int) tempTv.getTag();
                if (pos == addIndex) {
                    tempTv.setTextColor(mRedPackageColor);
                } else {
                    tempTv.setTextColor(tabTextColor);
                }
            }
            TextView tv = (TextView) tabsContainer.getChildAt(position);
            tempTv = tv;

            // zy2
            tempTv.setTag(position);

            handerTextView(tempTv, position);
        }
    }

    // zy2 底部标签的长度，是 tab 文字的长度的几分之几
    private int indicatorWeight = 0;

    public void setIndicatorWeight(int indicatorWeight) {
        this.indicatorWeight = indicatorWeight;
    }


    public interface IPageSlidingTab {
        public abstract void slideTop();
    }

    private TextView tempTv;

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.indicatorColor = ContextCompat.getColor(getContext(), (resId));
        invalidate();
    }

    public int getIndicatorColor() {
        return this.indicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.indicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public void setUnderlineColor(int underlineColor) {
        this.underlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.underlineColor = ContextCompat.getColor(getContext(), resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.dividerColor = ContextCompat.getColor(getContext(), resId);
        invalidate();
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.underlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getUnderlineHeight() {
        return underlineHeight;
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.dividerPadding = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPadding() {
        return dividerPadding;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
        requestLayout();
    }

    public boolean getShouldExpand() {
        return shouldExpand;
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.textAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
        this.tabTextSize = textSizePx;
        updateTabStyles();
    }

    public void setTextSelectSize(int textSizePx) {
        this.tabSelectSize = textSizePx;
        updateTabStyles();
    }

    public int getTextSize() {
        return tabTextSize;
    }

    public void setTextColor(int textColor) {
        this.tabTextColor = textColor;
        updateTabStyles();
    }

    public void setTextColorResource(int resId) {
        this.tabTextColor = ContextCompat.getColor(getContext(), resId);
        updateTabStyles();
    }

    public int getTextColor() {
        return tabTextColor;
    }

    public void setTabBackground(int resId) {
        this.tabBackgroundResId = resId;
    }

    public int getTabBackground() {
        return tabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.tabPadding = paddingPx;
        updateTabStyles();
    }

    public int getTabPaddingLeftRight() {
        return tabPadding;
    }

    private int indicatorBottomPadding = 0;

    public void setIndicatorBottomPadding(int indicatorBottomPadding) {
        this.indicatorBottomPadding = indicatorBottomPadding;
    }


    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
