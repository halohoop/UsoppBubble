package com.halohoop.usoppbubble.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;

import com.halohoop.usoppbubble.R;
import com.halohoop.usoppbubble.utils.Utils;

/**
 * Created by Pooholah on 2017/5/26.
 */

public class UsoppBubble extends AppCompatTextView implements DraggableListener {
    private UsoppFrameLayout mBubblesView = null;
//    private ViewGroup mParent;
//    private int mIndexAtParent = 0;
    /**
     * 可触摸区域的半径，正方形的边长的一半
     */
    private float mTouchAreaLargerRatio = 2.0f;
    private int mDragBackgroundColor = Color.rgb(150, 215, 240);

    public UsoppBubble(Context context) {
        super(context);
    }

    public UsoppBubble(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        resolveAttrs(context, attrs);
    }

    public UsoppBubble(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        resolveAttrs(context, attrs);
//        resolveSystemAttrs(context);
    }

//    private void resolveSystemAttrs(Context context) {
//        TypedValue typedValue = new TypedValue();
//        context.getTheme().resolveAttribute(android.R.attr.background, typedValue, true);
//        int[] attribute = new int[] { android.R.attr.background };
//        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
//        ShapeDrawable drawable = (ShapeDrawable) array.getDrawable(android.R.attr.background);
//        tmpcolor = drawable.getPaint().getColor();
//        array.recycle();
//    }

    private void resolveAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UsoppBubble);
        mTouchAreaLargerRatio = typedArray
                .getFloat(R.styleable.UsoppBubble_touch_area_larger_ratio, mTouchAreaLargerRatio);
        mDragBackgroundColor = typedArray
                .getColor(R.styleable.UsoppBubble_drag_background_color, mDragBackgroundColor);
        typedArray.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            //mTouchAreaLargerRatio
            float largedSize = (bottom - top) * mTouchAreaLargerRatio;
            int deltaY = (int) (largedSize - (bottom - top));
            int deltaX = largedSize > (right - left) ? (int) (largedSize - (right - left)) : 0;
            ViewGroup vg = (ViewGroup) getParent();
            //如果设定的半径大于固有的半径，就按设定的，如果没有大于就按固有的
            Rect rect = new Rect(left - deltaX, top - deltaY, right + deltaX, bottom + deltaY);
            vg.setTouchDelegate(new TouchDelegate(
                    rect,
                    this));
        }
    }

    public void createBubbles(float rawX, float rawY, float contentOffset) {
        mBubblesView = getBubbleShowingTarget(getContext());
        mBubblesView.init(this, rawX, rawY, contentOffset);
    }

    private UsoppFrameLayout getBubbleShowingTarget(Context context) {
        UsoppFrameLayout usoppFrameLayout = (UsoppFrameLayout) ((Activity) context).findViewById(R.id.usopp_container);
        return usoppFrameLayout;
    }

    private float mContentOffset = 0;

    private float getContentOffset() {
        int[] location = new int[2];
        Utils.scanForActivity(getContext()).findViewById(android.R.id.content).getLocationInWindow(location);
        return location[1];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mTouchable)return false;
        float rawX = event.getRawX();
        float rawY = event.getRawY() - mContentOffset;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //重要，请求父类不要拦截触摸事件
                getParent().requestDisallowInterceptTouchEvent(true);
                //android.R.id.content是否在状态栏之下,
                mContentOffset = getContentOffset();
                createBubbles(rawX, rawY - mContentOffset, mContentOffset);
                mBubblesView.updatePointsDispatch(rawX, rawY - mContentOffset);
                setVisibility(View.INVISIBLE);
                break;
            case MotionEvent.ACTION_MOVE:
                mBubblesView.updatePointsDispatch(rawX, rawY);
                mBubblesView.invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mBubblesView.updatePointsDispatch(rawX, rawY);
                mBubblesView.prepareForAnim(rawX, rawY);
//                mParent.addView(this, mIndexAtParent);//动画结束时候才能调用
                mContentOffset = 0;
                break;
        }
        return true;
    }


    /**
     * 画出来的桩点和橡皮筋 是什么 样式
     */
    public final static int MODE_NONE = 0;
    public final static int MODE_GLOW = 1;
    public final static int MODE_EMBOSS = 2;
    private int mMode = MODE_NONE;

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        this.mMode = mode;
        if (mBubblesView != null) {
            mBubblesView.setPaintMaskFilter(mode);
        }
    }

    @Override
    public void onBubbleDragStart(UsoppBubble view) {
        //do nothing
    }

    @Override
    public void onOnBubbleReleaseWithLaunch(UsoppBubble view) {
        this.setCount(0);
    }

    @Override
    public void onOnBubbleReleaseWithoutLaunch(UsoppBubble view) {

    }

    public int getDragBackgroundColor() {
        return mDragBackgroundColor;
    }

    private int mMaxNum = 99;
    private int mCount = 0;
    private boolean mTouchable = false;

    public int getMaxNum() {
        return mMaxNum;
    }

    public void setMaxNum(int maxNum) {
        this.mMaxNum = maxNum;
    }

    public void setCount(int num) {
        this.mCount = num;
        if (num <= 0) {
            setVisibility(View.INVISIBLE);
            mTouchable = false;
        }else{
            setVisibility(View.VISIBLE);
            mTouchable = true;
        }
        super.setText(num>mMaxNum?mMaxNum+"+":""+num);
    }

    public int getCount(){
        return mCount;
    }

    /**
     * callbacks
     */
    private DraggableListener[] mDragListener = null;

    public void setDragListener(DraggableListener dragListener) {
        if(mDragListener==null) mDragListener = new DraggableListener[2];
        this.mDragListener[0] = this;
        this.mDragListener[1] = dragListener;
    }

    final public DraggableListener getDragListener() {
        return mDragListener[0];
    }
}
