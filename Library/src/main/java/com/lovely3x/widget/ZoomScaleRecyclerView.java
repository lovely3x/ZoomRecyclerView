package com.lovely3x.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * 支持视图缩放的RecyclerView
 * 通过设置特定的{@link ZoomPolicy} 来实现缩放效果
 * 默认有一个图片的缩放策略器{@link ImageViewZoomPolicy}
 * 在使用前需要先设置{@link ZoomPolicy} 来实现缩放效果
 * Created by lovely3x on 17/2/27.
 */
public class ZoomScaleRecyclerView extends RecyclerViewHeaderAndFooter {

    private ZoomPolicy mZoomPolicy;

    private int mActivePointerId;

    public ZoomScaleRecyclerView(Context context) {
        this(context, null);
    }

    public ZoomScaleRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomScaleRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            if (mZoomPolicy != null) {
                mZoomPolicy.laidOut();
            }
        }
    }

    public void setZoomPolicy(ZoomPolicy policy) {
        this.mZoomPolicy = policy;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean superProcess = super.onTouchEvent(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN: {
                mActivePointerId = MotionEventCompat.getPointerId(e, actionIndex);
                if (mZoomPolicy != null && mZoomPolicy.onPointerActionDown(e.getX(mActivePointerId), e.getY(mActivePointerId))) {
                    return true;
                }
            }
            break;
            case MotionEvent.ACTION_DOWN: {

                mActivePointerId = MotionEventCompat.getPointerId(e, 0);

                if (mZoomPolicy != null && mZoomPolicy.onActionDown(e.getX(), e.getY())) {
                    return true;
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {

                int index = MotionEventCompat.findPointerIndex(e, mActivePointerId);
                if (index < 0) {
                    return false;
                }

                if (mZoomPolicy != null && mActivePointerId != -1 && mZoomPolicy.onActionMove(e.getX(index), e.getY(index))) {
                    return true;
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (mZoomPolicy != null && mActivePointerId != -1 && mZoomPolicy.onActionUp(e.getX(), e.getY())) {
                    return true;
                }
                mActivePointerId = -1;
            }
            break;
            case MotionEvent.ACTION_POINTER_UP:
                if (MotionEventCompat.getPointerId(e, actionIndex) == mActivePointerId) {
                    final int newIndex = actionIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(e, newIndex);
                    if (mZoomPolicy != null) {
                        mZoomPolicy.onPointerActionUp(MotionEventCompat.getX(e, newIndex), MotionEventCompat.getY(e, newIndex));
                    }
                }
        }
        return superProcess;
    }

    /**
     * 缩放策略器
     */
    public static class ZoomPolicy {

        private final int TOUCH_SLOP;
        private final ZoomScaleRecyclerView mZoomScaleRecyclerView;
        private final Context mContext;
        private final View mScaleView;
        private final View mHeaderView;
        private float mLastY;

        private boolean mIsBeginDrag;
        private boolean mCaseByZoom;


        public ZoomPolicy(Context context, ZoomScaleRecyclerView recyclerView, View headerView, View scaleView) {
            this.mZoomScaleRecyclerView = recyclerView;
            this.mScaleView = scaleView;
            this.mHeaderView = headerView;
            this.mContext = context;
            this.TOUCH_SLOP = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        public Context getContext() {
            return mContext;
        }

        public View getScaleView() {
            return mScaleView;
        }

        public boolean onActionUp(float x, float y) {
            mLastY = -1;
            mIsBeginDrag = false;
            return false;
        }

        public boolean onActionMove(float x, float y) {
            float diff = (y - mLastY);

            if (mIsBeginDrag) {
                mLastY = y;
                return onMoved(diff);
            }

            if (Math.abs(diff) >= TOUCH_SLOP && isReachedTop() && diff > 0) {
                mIsBeginDrag = true;
                mLastY = y;
                return onMoved(diff);
            }

            return mIsBeginDrag;
        }

        private boolean isReachedTop() {
            int firstPos = findFirstVisibleItemPosition();
            int top = mHeaderView.getTop();
            return firstPos == 0 && top >= 0;
        }

        protected boolean onMoved(float diff) {
            return false;
        }

        public boolean onActionDown(float x, float y) {
            this.mLastY = y;
            return false;
        }

        public boolean onPointerActionDown(float x, float y) {
            this.mLastY = y;
            return false;
        }

        private int findFirstVisibleItemPosition() {
            if (mZoomScaleRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) mZoomScaleRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            } else {
                if (mZoomScaleRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    return ((GridLayoutManager) mZoomScaleRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                }
            }
            return -1;
        }

        protected void blockLayoutCallBack() {
            this.mCaseByZoom = true;
        }

        public final void laidOut() {
            if (!mCaseByZoom) {
                mCaseByZoom = false;
                onLaidOut();
            }
        }

        public void onLaidOut() {

        }

        public boolean onPointerActionUp(float x, float y) {
            return false;
        }
    }

    /**
     * ImageView 缩放策略器
     */
    public static class ImageViewZoomPolicy extends ZoomPolicy {

        private static final int DEFAULT_ANIM_DURATION = 300;
        private int animDuration = DEFAULT_ANIM_DURATION;

        private final View mHeaderView;
        private final ImageView mImageView;
        private int vHeight;
        private ValueAnimator animator;

        /**
         * 阻力系数
         * 值越小越费力
         */
        private float friction = 0.35F;

        /**
         * 插值器
         */
        private TimeInterpolator mAnimInterpolator = new FastOutSlowInInterpolator();

        public ImageViewZoomPolicy(Context context, ZoomScaleRecyclerView recyclerView, View headerView, ImageView scaleView) {
            super(context, recyclerView, headerView, scaleView);
            this.mImageView = scaleView;
            this.mHeaderView = headerView;
            scaleView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }


        public void setFriction(float friction) {
            this.friction = friction;
        }

        public float getFriction() {
            return friction;
        }

        @Override
        public void onLaidOut() {
            vHeight = mImageView.getHeight();
        }

        @Override
        public boolean onActionUp(float x, float y) {
            springBack();
            return super.onActionUp(x, y);
        }

        @Override
        protected boolean onMoved(float diff) {
            if (diff < 0 && mHeaderView.getHeight() <= vHeight) {
                return false;
            }
            blockLayoutCallBack();
            setHeaderViewHeight((int) (mHeaderView.getHeight() + (diff * friction)));
            return true;
        }

        void setHeaderViewHeight(int height) {
            ViewGroup.LayoutParams lp = mHeaderView.getLayoutParams();
            lp.height = height;
            mHeaderView.setLayoutParams(lp);
        }

        protected void springBack() {

            if (animator != null) {
                animator.cancel();
                animator = null;
            }

            ViewGroup.LayoutParams lp = mHeaderView.getLayoutParams();
            animator = ValueAnimator.ofInt(lp.height, vHeight);
            animator.setInterpolator(mAnimInterpolator);
            animator.setDuration(animDuration);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams lp = mHeaderView.getLayoutParams();
                    lp.height = (int) animation.getAnimatedValue();
                    mHeaderView.setLayoutParams(lp);

                }
            });

            animator.start();
        }

        public void setAnimDuration(int duration) {
            this.animDuration = duration;
        }
    }
}
