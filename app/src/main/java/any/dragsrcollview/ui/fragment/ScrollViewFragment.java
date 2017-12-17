package any.dragsrcollview.ui.fragment;

import android.os.Bundle;
import android.view.View;

import any.dragsrcollview.R;

/**
 * Created by anyrsan on 2017/12/17.
 */

public class ScrollViewFragment extends BaseFragment {

    // 实际上可以传参过来，通过setArguments(Bundle bundle)
    public static ScrollViewFragment getInstance() {
        ScrollViewFragment scrollViewFragment = new ScrollViewFragment();
        return scrollViewFragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_scrollview_layout;
    }

    @Override
    public void scroll2Top() {
        rootView.scrollTo(0,0);
    }

    @Override
    public View getScrollableView() {
        return rootView;
    }

    @Override
    public void initView() {

    }
}
