package any.dragsrcollview.widget;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ScrollView;

/**
 * Created by  anyrsan on 2017/12/12.
 */
public class DragScrollHelper {

    private ScrollableContainer mCurrentScrollableContainer;

    /**
     * 包含有 ScrollView ListView RecyclerView 的组件
     */
    public interface ScrollableContainer {

        /**
         * @return ScrollView ListView RecyclerView 或者其他的布局的实例
         */
        View getScrollableView();
    }

    public void setCurrentScrollableContainer(ScrollableContainer scrollableContainer) {
        this.mCurrentScrollableContainer = scrollableContainer;
    }

    private View getScrollableView() {
        if (mCurrentScrollableContainer == null)
            return null;
        return mCurrentScrollableContainer.getScrollableView();
    }

    /**
     * 判断是否滑动到顶部方法,ScrollAbleLayout根据此方法来做一些逻辑判断
     * 目前只实现了AdapterView,ScrollView,RecyclerView
     * 需要支持其他view可以自行补充实现
     */
    public boolean isTop() {
        View scrollableView = getScrollableView();
        if (scrollableView == null) {
            throw new NullPointerException("You should call ScrollableHelper.setCurrentScrollableContainer() to set ScrollableContainer.");
        }
        if (scrollableView instanceof AdapterView) {
            return isAdapterViewTop((AdapterView) scrollableView);
        }
        if (scrollableView instanceof ScrollView) {
            return isScrollViewTop((ScrollView) scrollableView);
        }
        if (scrollableView instanceof RecyclerView) {
            return isRecyclerViewTop((RecyclerView) scrollableView);
        }
        if (scrollableView instanceof WebView) {
            return isWebViewTop((WebView) scrollableView);
        }
        if (scrollableView instanceof NestedScrollView) {
            return isNestedScrollViewTop((NestedScrollView) scrollableView);
        }
        throw new IllegalStateException("scrollableView must be a instance of AdapterView|ScrollView|RecyclerView");
    }

    public static boolean isRecyclerViewTop(RecyclerView recyclerView) {
        if (recyclerView != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                View childAt = recyclerView.getChildAt(0);
                int mt = 0;
                if (childAt != null) {
                    ViewGroup.LayoutParams params = childAt.getLayoutParams();
                    if (params instanceof RecyclerView.LayoutParams) {
                        mt = ((RecyclerView.LayoutParams) params).topMargin;
                    }
                }

                if (childAt == null || (firstVisibleItemPosition == 0 && childAt.getTop() == mt)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAdapterViewTop(AdapterView adapterView) {
        if (adapterView != null) {
            int firstVisiblePosition = adapterView.getFirstVisiblePosition();
            View childAt = adapterView.getChildAt(0);
            if (childAt == null || (firstVisiblePosition == 0 && childAt.getTop() == 0)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNestedScrollViewTop(NestedScrollView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }

    public static boolean isScrollViewTop(ScrollView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }

    public static boolean isWebViewTop(WebView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }


    public int getRecyclerViewHeight(View view) {
        int height = 0;
        if (view != null && view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {


                }
            });

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                for (int i = 0; i < layoutManager.getItemCount(); i++) {
                    View v = layoutManager.findViewByPosition(i);
                    Log.e("msg", "v..." + v + "==>" + layoutManager.getHeight());
                    if (v != null) {
                        Log.e("msg", "v..." + v.getHeight() + "==>");
                    }
                }
            }
        }
        return height;
    }


    /**
     * 获取滚动的距离
     * @param recyclerView
     * @return
     */
    public static int getScollYDistance(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();

        View firstVisiableChildView = layoutManager.findViewByPosition(position);

        int itemHeight = firstVisiableChildView.getHeight();

        return (position) * itemHeight - firstVisiableChildView.getTop();
    }
}
