package com.lovely3x.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * 支持添加头视图和尾视图的RecyclerView
 * 在添加头尾视图前需要先设置布局管理器
 * Created by lovely3x on 17/1/10.
 */
public class RecyclerViewHeaderAndFooter extends RecyclerView {

    private final ArrayList<View> mFooterViews = new ArrayList<>();
    private final ArrayList<View> mHeaderViews = new ArrayList<>();

    private final HeaderAndFooterRecyclerView mWrappedAdapter = new HeaderAndFooterRecyclerView();

    private Adapter mAdapter;

    public RecyclerViewHeaderAndFooter(Context context) {
        super(context);
    }

    public RecyclerViewHeaderAndFooter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewHeaderAndFooter(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        this.mAdapter = adapter;
        if (mAdapter == null) {
            super.setAdapter(null);
        } else {
            super.setAdapter(mWrappedAdapter);
        }
    }

    public void addFooterView(View view) {
        addFooterView(view, mFooterViews.size());
    }

    public void addFooterView(View view, int index) {
        mFooterViews.add(index < 0 ? 0 : index, view);
        mWrappedAdapter.notifyDataSetChanged();
    }

    public void addHeaderView(View view) {
        addHeaderView(view, mHeaderViews.size());
    }

    public void removeFooterView(View view) {
        int index = mFooterViews.indexOf(view);
        if (index >= 0) {
            removeFooterView(index);
        }
    }

    public View removeFooterView(int index) {
        View view = mFooterViews.remove(index);
        mWrappedAdapter.notifyItemRemoved(index);
        return view;
    }

    public View removeHeaderView(int index) {
        View view;
        if ((view = mHeaderViews.remove(index)) != null) {
            index = (mHeaderViews.size() + (mAdapter == null ? 0 : mAdapter.getItemCount())) + index;
            mWrappedAdapter.notifyItemRemoved(index);
        }
        return view;
    }

    public void removeHeaderView(View view) {
        int index = mHeaderViews.indexOf(view);
        if (index >= 0) removeHeaderView(index);
    }

    public void addHeaderView(View view, int index) {
        mHeaderViews.add(index < 0 ? 0 : index, view);
        mWrappedAdapter.notifyDataSetChanged();
    }

    private final class HeaderAndFooterRecyclerView extends Adapter {

        private int HEADER_VIEW_OFFSET = Integer.MAX_VALUE >> 2;
        private int FOOTER_VIEW_OFFSET = Integer.MAX_VALUE >> 1;


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType >= HEADER_VIEW_OFFSET && viewType < FOOTER_VIEW_OFFSET) {
                HeaderAndFooterViewHolder holder = new HeaderAndFooterViewHolder(mHeaderViews.get(viewType - HEADER_VIEW_OFFSET));
                holder.setIsRecyclable(false);
                return holder;
            } else if (viewType >= FOOTER_VIEW_OFFSET) {
                int pos = viewType - FOOTER_VIEW_OFFSET;
                int footerIndex = pos - mHeaderViews.size() - (mAdapter == null ? 0 : mAdapter.getItemCount());
                HeaderAndFooterViewHolder holder = new HeaderAndFooterViewHolder(mFooterViews.get(footerIndex));
                holder.setIsRecyclable(false);
                return holder;
            } else {
                return mAdapter == null ? null : mAdapter.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position < mHeaderViews.size()) {//Header
                final int headerViewIndex = position;
            } else if (position >= mHeaderViews.size() + (mAdapter == null ? 0 : mAdapter.getItemCount())) {//Footer
                final int footerViewIndex = position - mHeaderViews.size() - (mAdapter == null ? 0 : mAdapter.getItemCount());
            } else {
                if (mAdapter != null) {
                    mAdapter.onBindViewHolder(holder, position - mHeaderViews.size());
                }
            }
        }

        @Override
        public int getItemCount() {

            final int realCount = mAdapter == null ? 0 : mAdapter.getItemCount();
            final int headerSize = mHeaderViews.size();
            final int footerSize = mFooterViews.size();

            return realCount + footerSize + headerSize;

        }

        @Override
        public int getItemViewType(int position) {
            final int headerViewCount = mHeaderViews.size();
            if (position < headerViewCount) {
                return HEADER_VIEW_OFFSET + position;
            } else {
                int realAdapterSize = mAdapter == null ? 0 : mAdapter.getItemCount();
                final int max = headerViewCount + realAdapterSize;
                if (position >= max) {
                    return FOOTER_VIEW_OFFSET + position;
                } else {
                    if (mAdapter != null) {
                        int type = mAdapter.getItemViewType(position - headerViewCount);
                        if (type >= FOOTER_VIEW_OFFSET) {
                            throw new IllegalArgumentException("Type index must less then " + FOOTER_VIEW_OFFSET);
                        }
                        return type;
                    }
                }
            }

            return super.getItemViewType(position);
        }
    }

    private static class HeaderAndFooterViewHolder extends ViewHolder {

        HeaderAndFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

}
