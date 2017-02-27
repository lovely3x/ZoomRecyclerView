package com.lovely3x.widget.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lovely3x.widget.ZoomScaleRecyclerView;

public class MainActivity extends AppCompatActivity {

    private ZoomScaleRecyclerView mList;
    private View mHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mList = (ZoomScaleRecyclerView) findViewById(R.id.rvhaf_activity_main_list);

        //设置布局管理器
        this.mList.setLayoutManager(new LinearLayoutManager(this));

        this.mHeaderView = getLayoutInflater().inflate(R.layout.view_header, mList, false);
        ImageView img = (ImageView) mHeaderView.findViewById(R.id.iv_view_header_img);

        //添加头视图
        this.mList.addHeaderView(mHeaderView);

        //设置缩放策略器
        this.mList.setZoomPolicy(new ZoomScaleRecyclerView.ImageViewZoomPolicy(this, mList, mHeaderView, img));

        //设置适配器
        this.mList.setAdapter(new SimpleAdapter());
    }


    /**
     * Adapter
     */
    public class SimpleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SimpleViewHolder(getLayoutInflater().inflate(R.layout.view_list_item, mList, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 5000;
        }
    }


    /**
     * Holder
     */
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {

        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
}
