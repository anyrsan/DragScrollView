package any.dragsrcollview.ui.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import any.dragsrcollview.R;
import any.dragsrcollview.ui.adapter.RecyclerViewAdapter;

/**
 * Created by anyrsan on 2017/12/17.
 */

public class RecyclerViewFragment extends BaseFragment {

    // 实际上可以传参过来，通过setArguments(Bundle bundle)
    public static RecyclerViewFragment getInstance() {
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        return recyclerViewFragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_recyclerview_layout;
    }

    @Override
    public void scroll2Top() {
        RecyclerView recyclerView = (RecyclerView) rootView;
        recyclerView.scrollToPosition(0);
    }

    @Override
    public View getScrollableView() {
        return rootView;
    }

    @Override
    public void initView() {
        RecyclerView recyclerView = (RecyclerView) rootView;
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setFadingEdgeLength(0);
        recyclerView.setFocusable(false);
        recyclerView.setAdapter(adapter);
    }
}
