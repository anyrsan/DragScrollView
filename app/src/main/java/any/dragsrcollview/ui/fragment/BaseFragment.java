package any.dragsrcollview.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by anyrsan on 2017/12/17.
 */

public abstract class BaseFragment extends Fragment {

    protected View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutResId(), container, false);
        initView();
        return rootView;
    }

    public abstract int getLayoutResId();

    public abstract void scroll2Top();

    public abstract View getScrollableView();

    public abstract void initView();
}
