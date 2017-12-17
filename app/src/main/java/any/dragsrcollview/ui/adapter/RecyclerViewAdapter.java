package any.dragsrcollview.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import any.dragsrcollview.R;

/**
 * Created by anyrsan on 2017/12/17.
 */

public class RecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 15;
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

}
