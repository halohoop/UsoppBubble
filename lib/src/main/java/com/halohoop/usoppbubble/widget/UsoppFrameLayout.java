package com.halohoop.usoppbubble.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.halohoop.usoppbubble.utils.Utils;

/**
 * Created by Pooholah on 2017/6/11.
 */

public class UsoppFrameLayout extends LinearLayout {
    public UsoppFrameLayout(Context context) {
        super(context);
    }

    public UsoppFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UsoppFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //执行动画的时候不能进行第二次触发
        if (mDataBean!=null && (mDataBean.mIsBounceAnimStart
                ||mDataBean.mIsBurstAnimStart
                ||mDataBean.mIsLaunchAnimStart)) {
            return true;
        }
        return false;
    }

//    private SoftReference<DataBean> mDataBean = null;

    private DataBean mDataBean = null;

    private class DataBean {
        private boolean DEBUG = true;
        private Paint debugPaint;

        //when create instance it should be initialized
        private DraggableListener mDragListener = null;
        private UsoppBubble mClickView;//UsoppBubble
        /**
         * 屏幕的宽高,onSizeChanged的时候会重新更新
         */
        private Point mScreenSize;
        /**
         * bubble default state 不需要这个属性，因为一按下就已经是拖拽状态
         */
//        private final int BUBBLE_STATE_DEFAUL = 0;
        /**
         * bubble dragging
         */
        private static final int BUBBLE_STATE_DRAGGING = 1;
        /**
         * let go and launch
         */
        private static final int BUBBLE_STATE_RELEASE_LAUNCH = 2;
        /**
         * reach the edge to burst
         */
        private static final int BUBBLE_STATE_BURSRT = 3;
        /**
         * let go without launch
         */
        private static final int BUBBLE_STATE_RELEASE_NO_LAUNCH = 4;
        /**
         * 状态标志
         */
        private int mBubbleState = BUBBLE_STATE_RELEASE_NO_LAUNCH;
        /**
         * 动画是否已经开始
         */
        private boolean mIsBurstAnimStart = false;
        private boolean mIsLaunchAnimStart = false;
        private boolean mIsBounceAnimStart = false;
        /**
         * 当是释放爆炸动画时候，标记当前需要绘制哪一张图片
         */
        private int mCurDrawableIndex = 0;

        /**
         * >=这个范围才会发射
         */
        private float mLaunchThreadhold = 80;
        private RectF mLaunchArea = null;
        private RectF mMaxStretch = null;
        private static final int LAUNCH_TO_LEFT = 0;
        private static final int LAUNCH_TO_TOP = 1;
        private static final int LAUNCH_TO_RIGHT = 2;
        private static final int LAUNCH_TO_BOTTOM = 3;
        private static final int LAUNCH_DIRE_NONE = 4;//原地爆炸
        private int mLaunchDire = LAUNCH_DIRE_NONE;//发射方向指向哪里

        /**
         * 爆炸的动画播放的区域半径
         */
        private int mBurstRectFRadius = 100;
        /**
         * 爆炸的动画播放的区域
         */
        private RectF mBurstRectF = new RectF(0, 0, mBurstRectFRadius * 2, mBurstRectFRadius * 2);

        private Bitmap[] mBitmapsExplodes = null;
        //        private Bitmap mDragBitmap;
        private float mDragBitmapOffsetX = 0;
        private float mDragBitmapOffsetY = 0;

        private PointF mMovePointCenter = new PointF();
        private PointF mMoveDrawPointCenter = new PointF();
        /**
         * 只有发射的时候才使用，因为 图片 和 橡皮筋 的连接点 要脱离，分别用变量记录位置
         */
        private PointF mMoveElasticDrawPointCenterWhenLaunch = new PointF();
        private PointF mStickyPointCenter0 = new PointF();
        private PointF mStickyPointCenterMid = new PointF();
        private PointF mStickyPointCenter1 = new PointF();
        private int mColorStickyPointCenter = Color.rgb(245, 179, 43);
        private int mColorStickyPointCenterNotReady = Color.rgb(243, 67, 54);
        private int mColorStickyPointCenterReady = Color.rgb(76, 175, 80);
        private float mStickyPointRaidus = 12;
        private float mStickyPointRaidusReady = 12;

        /**
         * 默认让其变大1.2倍，变宽的是
         * mStickyPointCenter0和mStickyPointCenter1
         * 分别距离mStickyPointCenterMid的距离
         */
        private float mLargerRatio = 1.1f;//经验值

        /**
         * 一共需要画四条贝塞尔，所以是四个控制点
         */
        private PointF mSidesInnerBesierCtrlPoint0 = new PointF();
        private PointF mSidesInnerBesierCtrlPoint1 = new PointF();
        private PointF mSidesOuterBesierCtrlPoint0 = new PointF();
        private PointF mSidesOuterBesierCtrlPoint1 = new PointF();
        /**
         * 贝塞尔曲线offset
         */
        private float mMovePointQuadMidLineOffset = 5;//默认值，后会通过屏幕比例算出来
        private float mMovePointQuadSidesOffset = 50;//同上
        /**
         * 外层橡皮筋和内层之间的offset
         */
        private float mQuadSidesOuterOffset = 25;//默认值，后会通过屏幕比例算出来
        private float mQuadMidLineOuterOffset = 30;//同上

        /**
         * 当进入这个区域的范围的时候，不做任何旋转
         */
        private float mResetSensorRaidus = 96.0f;
        private RectF mStickySensor;
        private Rect mClickViewRect;
        private float mHalfClickViewWidth = 0;
        private Paint mPaint;
        private MaskFilter mFilter = null;
        private float mLaunchYRaw;
        private float mLaunchXRaw;
        private Path mElasticPath;
        private int mElasticColor = Color.rgb(255, 78, 18);

        private float mRotateDegrees = 0;
        /**
         * 是否拖出达到了可以发射的区域
         */
        private boolean mIsReadyToLaunch = false;
        /**
         * 是否拖出达到了最大拉伸距离
         */
        private boolean mIsReachMaxStretch = false;
    }

    // ---------------------------------
    public void init(UsoppBubble clickView, float rawX, float rawY, float contentOffset) {
        setWillNotDraw(false);
        mDataBean = null;
//        mDataBean = new StrongReference<>(new DataBean());
        mDataBean = new DataBean();
        if (mDataBean.DEBUG) {
            mDataBean.debugPaint = new Paint();
        }
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mDataBean.mDragListener = clickView.getDragListener();
        mDataBean.mClickView = clickView;
        mDataBean.mScreenSize = Utils.getContentSize(Utils.scanForActivity(getContext()), contentOffset);

        mDataBean.mResetSensorRaidus = Math.min(mDataBean.mScreenSize.x, mDataBean.mScreenSize.y) / 14.4f;//经验值
        mDataBean.mLaunchThreadhold = Math.min(mDataBean.mScreenSize.x, mDataBean.mScreenSize.y) / 9f;//经验值

        //确定固定的中点
        mDataBean.mClickViewRect = new Rect();
//            clickView.getGlobalVisibleRect(mClickViewRect);
        clickView.getLocalVisibleRect(mDataBean.mClickViewRect);
        int[] location = new int[2];
//            clickView.getLocationOnScreen(location);
        clickView.getLocationInWindow(location);
        int left = location[0];
        int top = location[1];
        mDataBean.mClickViewRect.offset(left, top);
        mDataBean.mStickyPointCenterMid.x = mDataBean.mClickViewRect.centerX();
        mDataBean.mStickyPointCenterMid.y = mDataBean.mClickViewRect.centerY() - contentOffset;
        mDataBean.mHalfClickViewWidth = mDataBean.mClickViewRect.width() / 2.0f;

        mDataBean.mStickyPointRaidus = Math.max(mDataBean.mClickViewRect.width(), mDataBean.mClickViewRect.height()) / 7.0f;//经验值
        mDataBean.mStickyPointRaidusReady = Math.max(mDataBean.mClickViewRect.width(), mDataBean.mClickViewRect.height()) / 7.0f;//经验值

        mDataBean.mMovePointQuadMidLineOffset = Math.min(mDataBean.mClickViewRect.width(), mDataBean.mClickViewRect.height()) / 11.6f;//经验值
        mDataBean.mMovePointQuadSidesOffset = Math.min(mDataBean.mClickViewRect.width(), mDataBean.mClickViewRect.height()) / 1.16f;//经验值
        mDataBean.mQuadSidesOuterOffset = Math.min(mDataBean.mClickViewRect.width(), mDataBean.mClickViewRect.height()) / 2.32f;//经验值
        mDataBean.mQuadMidLineOuterOffset = Math.min(mDataBean.mClickViewRect.width(), mDataBean.mClickViewRect.height()) / 1.9f;//经验值

        mDataBean.mDragBitmapOffsetX = -mDataBean.mClickViewRect.width();
        mDataBean.mDragBitmapOffsetY = -mDataBean.mClickViewRect.height() / 2.0f;

        mDataBean.mLaunchArea = new RectF(mDataBean.mStickyPointCenterMid.x - mDataBean.mLaunchThreadhold,
                mDataBean.mStickyPointCenterMid.y - mDataBean.mLaunchThreadhold,
                mDataBean.mStickyPointCenterMid.x + mDataBean.mLaunchThreadhold,
                mDataBean.mStickyPointCenterMid.y + mDataBean.mLaunchThreadhold);
        float oneOf3OfScreenWidth = mDataBean.mScreenSize.x / 3.0f;
        mDataBean.mMaxStretch = new RectF(mDataBean.mStickyPointCenterMid.x - oneOf3OfScreenWidth,
                mDataBean.mStickyPointCenterMid.y - oneOf3OfScreenWidth,
                mDataBean.mStickyPointCenterMid.x + oneOf3OfScreenWidth,
                mDataBean.mStickyPointCenterMid.y + oneOf3OfScreenWidth);

        //当手指在这个区域内就不做任何旋转
        mDataBean.mStickySensor = new RectF(mDataBean.mClickViewRect.centerX() - mDataBean.mResetSensorRaidus, mDataBean.mClickViewRect.centerY() - mDataBean.mResetSensorRaidus,
                mDataBean.mClickViewRect.centerX() + mDataBean.mResetSensorRaidus, mDataBean.mClickViewRect.centerY() + mDataBean.mResetSensorRaidus);

        mDataBean.mMovePointCenter.x = rawX;
        mDataBean.mMovePointCenter.y = rawY;
        mDataBean.mMoveDrawPointCenter.x = rawX;
        mDataBean.mMoveDrawPointCenter.y = rawY;

        updatePointsDispatch(rawX, rawY);

        //初始化爆炸动画
        initEndAnimationBitmaps();

        //创建画笔
        mDataBean.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDataBean.mPaint.setStyle(Paint.Style.FILL);
        mDataBean.mPaint.setStrokeWidth(1);
        mDataBean.mPaint.setColor(mDataBean.mElasticColor);
        mDataBean.mElasticPath = new Path();

        setPaintMaskFilter(mDataBean.mClickView.getMode());
    }

    public void setDEBUG(boolean DEBUG) {
        mDataBean.DEBUG = DEBUG;
        mDataBean.debugPaint = null;
    }

    /**
     * 更新弹弓固定位置的点
     *
     * @param currMoveX
     * @param currMoveY
     */
    public void updatePointsDispatch(float currMoveX, float currMoveY) {
        mDataBean.mBubbleState = DataBean.BUBBLE_STATE_DRAGGING;
        mDataBean.mIsReadyToLaunch = isReadyToLaunch(currMoveX, currMoveY);
        mDataBean.mIsReachMaxStretch = isReachMaxStretch(currMoveX, currMoveY);

        //首先更新移动点不要错乱，因为后面可能需要用到最新的移动点
        updateMoveAndControlPoints(currMoveX, currMoveY);

        updateLaunchDirection(mDataBean.mStickyPointCenterMid, mDataBean.mMovePointCenter);
    }


    private boolean isInResetArea(float rawX, float rawY) {
        return mDataBean.mStickySensor.contains(rawX, rawY);
    }

    private void updateLaunchDirection(PointF stickyPointCenterMid, PointF movePointCenter) {
        //四条线，固定的中点连接四个角形成
        float[] k_h_LeftTop = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(0, 0));
        float[] k_h_RightTop = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(mDataBean.mScreenSize.x, 0));
        float[] k_h_LeftBottom = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(0, mDataBean.mScreenSize.y));
        float[] k_h_RightBottom = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(mDataBean.mScreenSize.x, mDataBean.mScreenSize.y));
        if ((k_h_RightBottom[0] * movePointCenter.x + k_h_RightBottom[1]) <= movePointCenter.y
                && (k_h_RightTop[0] * movePointCenter.x + k_h_RightTop[1]) >= movePointCenter.y) {
            mDataBean.mLaunchDire = DataBean.LAUNCH_TO_RIGHT;
        } else if ((k_h_LeftBottom[0] * movePointCenter.x + k_h_LeftBottom[1]) >= movePointCenter.y
                && (k_h_RightBottom[0] * movePointCenter.x + k_h_RightBottom[1]) >= movePointCenter.y) {
            mDataBean.mLaunchDire = DataBean.LAUNCH_TO_BOTTOM;
        } else if ((k_h_LeftTop[0] * movePointCenter.x + k_h_LeftTop[1]) >= movePointCenter.y
                && (k_h_LeftBottom[0] * movePointCenter.x + k_h_LeftBottom[1]) <= movePointCenter.y) {
            mDataBean.mLaunchDire = DataBean.LAUNCH_TO_LEFT;
        } else if ((k_h_LeftTop[0] * movePointCenter.x + k_h_LeftTop[1]) <= movePointCenter.y
                && (k_h_RightTop[0] * movePointCenter.x + k_h_RightTop[1]) <= movePointCenter.y) {
            mDataBean.mLaunchDire = DataBean.LAUNCH_TO_TOP;
        } else {
            mDataBean.mLaunchDire = DataBean.LAUNCH_DIRE_NONE;
        }
    }

    /**
     * 初始化最后的爆炸动画
     */
    private void initEndAnimationBitmaps() {
        mDataBean.mBitmapsExplodes = new Bitmap[5];
        for (int i = 0; i < 5; i++) {
            int identifier = getContext().getResources().getIdentifier("burst_" + (i + 1), "drawable", getContext().getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), identifier);
            mDataBean.mBitmapsExplodes[i] = bitmap;
        }
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        if (mDataBean.DEBUG) {
            mDataBean.debugPaint.setTextSize(50);
            mDataBean.debugPaint.setColor(Color.GREEN);
            canvas.drawText("DEBUG_MODE_UsoppBubble", 0, mDataBean.mScreenSize.y / 2, mDataBean.debugPaint);
        }
        if (mDataBean.mBubbleState == DataBean.BUBBLE_STATE_RELEASE_NO_LAUNCH) {
            drawBounce(canvas);
        } else if (mDataBean.mBubbleState == DataBean.BUBBLE_STATE_RELEASE_LAUNCH) {
            drawLaunch(canvas);
        } else if (mDataBean.mBubbleState == DataBean.BUBBLE_STATE_BURSRT) {
            drawBurst(canvas);
        } else if (mDataBean.mBubbleState == DataBean.BUBBLE_STATE_DRAGGING) {
            drawDragging(canvas);
        }
    }

    //        private boolean mIsDrawReset = false;
    private void drawDragging(Canvas canvas) {
        canvas.save();
        canvas.rotate(mDataBean.mRotateDegrees, mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y);
        canvas.translate(mDataBean.mDragBitmapOffsetX, mDataBean.mDragBitmapOffsetY);
        canvas.translate(mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y);
        drawBubble(canvas);
        canvas.restore();
        mDataBean.mElasticPath.reset();
        mDataBean.mElasticPath.moveTo(mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesInnerBesierCtrlPoint0.x, mDataBean.mSidesInnerBesierCtrlPoint0.y,
                mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesInnerBesierCtrlPoint1.x, mDataBean.mSidesInnerBesierCtrlPoint1.y,
                mDataBean.mStickyPointCenter1.x, mDataBean.mStickyPointCenter1.y);

        mDataBean.mElasticPath.quadTo(mDataBean.mSidesOuterBesierCtrlPoint1.x, mDataBean.mSidesOuterBesierCtrlPoint1.y,
                mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesOuterBesierCtrlPoint0.x, mDataBean.mSidesOuterBesierCtrlPoint0.y,
                mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y);

        mDataBean.mPaint.setStyle(Paint.Style.FILL);
        mDataBean.mPaint.setColor(mDataBean.mElasticColor);
        mDataBean.mPaint.setStrokeWidth(1);
        mDataBean.mPaint.setMaskFilter(mDataBean.mFilter);
        canvas.drawPath(mDataBean.mElasticPath, mDataBean.mPaint);

        //画固定点的桩
        drawStickyPoints(canvas);
    }

    private void drawBurst(Canvas canvas) {
        if (mDataBean.mBitmapsExplodes != null && mDataBean.mBurstRectF != null) {
            canvas.drawBitmap(mDataBean.mBitmapsExplodes[mDataBean.mCurDrawableIndex], null, mDataBean.mBurstRectF, null);
        }
    }

    private void drawBounce(Canvas canvas) {
        canvas.save();
        canvas.translate(mDataBean.mLaunchXRaw, mDataBean.mLaunchYRaw);
        canvas.rotate(mDataBean.mRotateDegrees, 0, 0);
        canvas.translate(-mDataBean.mClickViewRect.width(), -mDataBean.mClickViewRect.height() / 2.0f);
        drawBubble(canvas);
        canvas.restore();
        mDataBean.mElasticPath.reset();
        mDataBean.mElasticPath.moveTo(mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesInnerBesierCtrlPoint0.x, mDataBean.mSidesInnerBesierCtrlPoint0.y,
                mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesInnerBesierCtrlPoint1.x, mDataBean.mSidesInnerBesierCtrlPoint1.y,
                mDataBean.mStickyPointCenter1.x, mDataBean.mStickyPointCenter1.y);

        mDataBean.mElasticPath.quadTo(mDataBean.mSidesOuterBesierCtrlPoint1.x, mDataBean.mSidesOuterBesierCtrlPoint1.y,
                mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesOuterBesierCtrlPoint0.x, mDataBean.mSidesOuterBesierCtrlPoint0.y,
                mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y);

        mDataBean.mPaint.setStyle(Paint.Style.FILL);
        mDataBean.mPaint.setColor(mDataBean.mElasticColor);
        mDataBean.mPaint.setStrokeWidth(1);
        mDataBean.mPaint.setMaskFilter(mDataBean.mFilter);
        canvas.drawPath(mDataBean.mElasticPath, mDataBean.mPaint);

        //画固定点的桩
        drawStickyPoints(canvas);
    }

    private void drawLaunch(Canvas canvas) {
        canvas.save();
        canvas.translate(mDataBean.mLaunchXRaw, mDataBean.mLaunchYRaw);
        canvas.rotate(mDataBean.mRotateDegrees, 0, 0);
        canvas.translate(-mDataBean.mClickViewRect.width(), -mDataBean.mClickViewRect.height() / 2.0f);
        drawBubble(canvas);
        canvas.restore();
        mDataBean.mElasticPath.reset();
        mDataBean.mElasticPath.moveTo(mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesInnerBesierCtrlPoint0.x, mDataBean.mSidesInnerBesierCtrlPoint0.y,
                mDataBean.mMoveElasticDrawPointCenterWhenLaunch.x, mDataBean.mMoveElasticDrawPointCenterWhenLaunch.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesInnerBesierCtrlPoint1.x, mDataBean.mSidesInnerBesierCtrlPoint1.y,
                mDataBean.mStickyPointCenter1.x, mDataBean.mStickyPointCenter1.y);

        mDataBean.mElasticPath.quadTo(mDataBean.mSidesOuterBesierCtrlPoint1.x, mDataBean.mSidesOuterBesierCtrlPoint1.y,
                mDataBean.mMoveElasticDrawPointCenterWhenLaunch.x, mDataBean.mMoveElasticDrawPointCenterWhenLaunch.y);
        mDataBean.mElasticPath.quadTo(mDataBean.mSidesOuterBesierCtrlPoint0.x, mDataBean.mSidesOuterBesierCtrlPoint0.y,
                mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y);

        mDataBean.mPaint.setStyle(Paint.Style.FILL);
        mDataBean.mPaint.setColor(mDataBean.mElasticColor);
        mDataBean.mPaint.setStrokeWidth(1);
        mDataBean.mPaint.setMaskFilter(mDataBean.mFilter);
        canvas.drawPath(mDataBean.mElasticPath, mDataBean.mPaint);

        //画固定点的桩
        drawStickyPoints(canvas);
    }

    private void drawBubble(Canvas canvas) {
        float left = 0;
        float top = 0;
        float right = mDataBean.mClickViewRect.width();
        float bottom = mDataBean.mClickViewRect.height();
        float midHeight = bottom / 2.0f;
        float midWidth = right / 2.0f;

        mDataBean.mPaint.setMaskFilter(null);
        mDataBean.mPaint.setColor(mDataBean.mClickView.getDragBackgroundColor());
        float offset = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            offset = mDataBean.mClickViewRect.width() - ((float) mDataBean.mClickViewRect.width()) / 1.845f;
            canvas.drawArc(left + offset, top, right, bottom, -90, 180, true, mDataBean.mPaint);
            canvas.drawArc(left, top, right - offset, bottom, 90, 180, true, mDataBean.mPaint);
        } else {
            offset = mDataBean.mClickViewRect.width() - ((float) mDataBean.mClickViewRect.width()) / 1.35f;
            canvas.drawCircle(left + offset, midHeight, midHeight, mDataBean.mPaint);
            canvas.drawCircle(right - offset, midHeight, midHeight, mDataBean.mPaint);
        }
        float halfDrawWidth = (right - (left + offset)) / 2.0f;
        canvas.drawRect(halfDrawWidth, 0, right - halfDrawWidth, bottom, mDataBean.mPaint);
        TextPaint paint = mDataBean.mClickView.getPaint();
        String text = (String) mDataBean.mClickView.getText();
        float halfMeasureTextWidth = paint.measureText(text, 0, text.length()) / 2.0f;
        paint.getTextBounds(text, 0, text.length(), mTextBound);
        canvas.drawText(text, 0, text.length(), midWidth - halfMeasureTextWidth, midHeight + mTextBound.height() / 2.0f, paint);
    }

    private Rect mTextBound = new Rect();

    private void drawStickyPoints(Canvas canvas) {
        mDataBean.mPaint.setStrokeWidth(1);//只要不是stoke模式，画圆就和这个设置参数没关系
        mDataBean.mPaint.setColor(mDataBean.mIsReadyToLaunch ? mDataBean.mColorStickyPointCenterReady : mDataBean.mColorStickyPointCenter);
        mDataBean.mPaint.setStyle(Paint.Style.FILL);
        mDataBean.mPaint.setMaskFilter(mDataBean.mFilter);
        canvas.drawCircle(mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y, mDataBean.mStickyPointRaidusReady, mDataBean.mPaint);
        canvas.drawCircle(mDataBean.mStickyPointCenter1.x, mDataBean.mStickyPointCenter1.y, mDataBean.mStickyPointRaidusReady, mDataBean.mPaint);
        if (!mDataBean.mIsReadyToLaunch) {
            mDataBean.mPaint.setColor(mDataBean.mColorStickyPointCenterNotReady);
            canvas.drawCircle(mDataBean.mStickyPointCenter0.x, mDataBean.mStickyPointCenter0.y, mDataBean.mStickyPointRaidus, mDataBean.mPaint);
            canvas.drawCircle(mDataBean.mStickyPointCenter1.x, mDataBean.mStickyPointCenter1.y, mDataBean.mStickyPointRaidus, mDataBean.mPaint);
        }
    }

    /**
     * 是到到达发射距离
     *
     * @param rawX
     * @param rawY
     * @return
     */
    private boolean isReadyToLaunch(float rawX, float rawY) {
//            return !mLaunchArea.contains(rawX, rawY);
        float hypot = (float) Math.hypot(rawX - mDataBean.mLaunchArea.centerX(), rawY - mDataBean.mLaunchArea.centerY());
        return hypot <= mDataBean.mLaunchArea.width() / 2 ? false : true;
    }

    /**
     * 是否到达最大拉伸距离
     *
     * @param rawX
     * @param rawY
     * @return
     */
    private boolean isReachMaxStretch(float rawX, float rawY) {
//            return !mLaunchArea.contains(rawX, rawY);
        float hypot = (float) Math.hypot(rawX - mDataBean.mMaxStretch.centerX(), rawY - mDataBean.mMaxStretch.centerY());
        return hypot <= mDataBean.mMaxStretch.width() / 2 ? false : true;
    }

    private void launch(float startRawX, float startRawY, final float endRawX, final float endRawY) {
        if (mDataBean.mDragListener != null) {
            mDataBean.mDragListener.onOnBubbleReleaseWithLaunch(mDataBean.mClickView);
        }
        //气泡改为发射状态
        mDataBean.mBubbleState = DataBean.BUBBLE_STATE_RELEASE_LAUNCH;
        mDataBean.mIsLaunchAnimStart = true;

        ValueAnimator launchAnim = createLaunchAnim(startRawX, startRawY, endRawX, endRawY);
        launchAnim.setInterpolator(new AccelerateInterpolator(0.4f));
        launchAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDataBean.mLaunchXRaw = (float) animation.getAnimatedValue("x_path");
                mDataBean.mLaunchYRaw = (float) animation.getAnimatedValue("y_path");
                updateMoveAndControlPoints(mDataBean.mLaunchXRaw, mDataBean.mLaunchYRaw);
            }
        });

        ValueAnimator elasticAnim = null;
        {//橡皮筋的动画
            float offsetX = mDataBean.mStickyPointCenterMid.x - startRawX;
            float offsetY = mDataBean.mStickyPointCenterMid.y - startRawY;
            float endX = mDataBean.mStickyPointCenterMid.x + offsetX * 0.15f;
            float endY = mDataBean.mStickyPointCenterMid.y + offsetY * 0.15f;
            PropertyValuesHolder xPro = PropertyValuesHolder.ofFloat("x_path", startRawX, endX);
            PropertyValuesHolder yPro = PropertyValuesHolder.ofFloat("y_path", startRawY, endY);
            elasticAnim = ValueAnimator.ofPropertyValuesHolder(xPro, yPro);
            elasticAnim.setInterpolator(new OvershootInterpolator(3f));
            elasticAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mDataBean.mMoveElasticDrawPointCenterWhenLaunch.x = (float) animation.getAnimatedValue("x_path");
                    mDataBean.mMoveElasticDrawPointCenterWhenLaunch.y = (float) animation.getAnimatedValue("y_path");
                    updateMoveAndControlPointsElastic(
                            mDataBean.mMoveElasticDrawPointCenterWhenLaunch.x,
                            mDataBean.mMoveElasticDrawPointCenterWhenLaunch.y);
                    invalidate();
                }
            });
        }

        AnimatorSet as = new AnimatorSet();
        elasticAnim.setDuration(300);
        launchAnim.setDuration(150);
        as.playTogether(launchAnim, elasticAnim);
        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //修改动画执行标志
                mDataBean.mIsLaunchAnimStart = false;
                endWithBubbleBurstAnim(endRawX, endRawY);
            }
        });
        as.start();
    }

    private void updateMoveAndControlPointsElastic(float currMoveX, float currMoveY) {
        //更新贝塞尔的控制点
        float innerQuadSidesOffset = mDataBean.mMovePointQuadSidesOffset;
        float outerQuadSidesOffset = mDataBean.mQuadSidesOuterOffset;
        if (!mDataBean.mIsReadyToLaunch) {
            float ratioInLaunchArea = Utils.getTwoPointsDistance(currMoveX, currMoveY, mDataBean.mStickyPointCenterMid.x, mDataBean.mStickyPointCenterMid.y) / (mDataBean.mLaunchArea.width() / 2);
            innerQuadSidesOffset = ratioInLaunchArea * mDataBean.mMovePointQuadSidesOffset;
            outerQuadSidesOffset = ratioInLaunchArea * mDataBean.mQuadSidesOuterOffset;
            mDataBean.mStickyPointRaidus = mDataBean.mStickyPointRaidusReady * (1 - ratioInLaunchArea);
        }

        float disX = Math.abs(mDataBean.mStickyPointCenterMid.x - currMoveX);
        float disY = Math.abs(mDataBean.mStickyPointCenterMid.y - currMoveY);
        double tanRadian = Math.atan(disY / disX);
        float tanDegree = 0;
        if (mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_NO_LAUNCH
                && mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_LAUNCH) {
            tanDegree = Utils.radian2Degree(tanRadian);
        }
        mDataBean.mDragBitmapOffsetX = -mDataBean.mClickViewRect.width();
        mDataBean.mDragBitmapOffsetY = -mDataBean.mClickViewRect.height() / 2.0f;
        float deltaX = (float) (Math.sin(tanRadian) * mDataBean.mHalfClickViewWidth * mDataBean.mLargerRatio);
        float deltaY = (float) (Math.cos(tanRadian) * mDataBean.mHalfClickViewWidth * mDataBean.mLargerRatio);

        float bezierInnerMidLineDisX = (float) (Math.cos(tanRadian) * mDataBean.mMovePointQuadMidLineOffset);
        float bezierInnerMidLineDisY = (float) (Math.sin(tanRadian) * mDataBean.mMovePointQuadMidLineOffset);
        float bezierInnerSidesDisX = (float) (Math.sin(tanRadian) * innerQuadSidesOffset);
        float bezierInnerSidesDisY = (float) (Math.cos(tanRadian) * innerQuadSidesOffset);

        float bezierOuterMidLineDisX = (float) (Math.cos(tanRadian) * (mDataBean.mMovePointQuadMidLineOffset - mDataBean.mQuadMidLineOuterOffset));
        float bezierOuterMidLineDisY = (float) (Math.sin(tanRadian) * (mDataBean.mMovePointQuadMidLineOffset - mDataBean.mQuadMidLineOuterOffset));
        float bezierOuterSidesDisX = (float) (Math.sin(tanRadian) * (innerQuadSidesOffset + outerQuadSidesOffset));
        float bezierOuterSidesDisY = (float) (Math.cos(tanRadian) * (innerQuadSidesOffset + outerQuadSidesOffset));
        if (mDataBean.mStickyPointCenterMid.x <= currMoveX && mDataBean.mStickyPointCenterMid.y > currMoveY) {
            //第1象限 + 负y轴
            if (mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_NO_LAUNCH
                    && mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_LAUNCH)
                mDataBean.mRotateDegrees = -tanDegree;
            //得到两个桩点坐标
            mDataBean.mStickyPointCenter0.x = mDataBean.mStickyPointCenterMid.x - deltaX;
            mDataBean.mStickyPointCenter0.y = mDataBean.mStickyPointCenterMid.y - deltaY;
            mDataBean.mStickyPointCenter1.x = mDataBean.mStickyPointCenterMid.x + deltaX;
            mDataBean.mStickyPointCenter1.y = mDataBean.mStickyPointCenterMid.y + deltaY;
            //根据贝塞尔offset确定4个控制点
            float bezierInnerMidLineX = currMoveX - bezierInnerMidLineDisX;
            float bezierInnerMidLineY = currMoveY + bezierInnerMidLineDisY;

            mDataBean.mSidesInnerBesierCtrlPoint0.x = bezierInnerMidLineX - bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint0.y = bezierInnerMidLineY - bezierInnerSidesDisY;
            mDataBean.mSidesInnerBesierCtrlPoint1.x = bezierInnerMidLineX + bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint1.y = bezierInnerMidLineY + bezierInnerSidesDisY;

            float bezierOuterMidLineX = currMoveX - bezierOuterMidLineDisX;
            float bezierOuterMidLineY = currMoveY + bezierOuterMidLineDisY;

            mDataBean.mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX - bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY - bezierOuterSidesDisY;
            mDataBean.mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX + bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY + bezierOuterSidesDisY;
        } else if (mDataBean.mStickyPointCenterMid.x > currMoveX && mDataBean.mStickyPointCenterMid.y >= currMoveY) {
            //第2象限 + 负x轴
            if (mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_NO_LAUNCH
                    && mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_LAUNCH)
                mDataBean.mRotateDegrees = -(180 - tanDegree);
            //得到两个桩点坐标
            mDataBean.mStickyPointCenter0.x = mDataBean.mStickyPointCenterMid.x - deltaX;
            mDataBean.mStickyPointCenter0.y = mDataBean.mStickyPointCenterMid.y + deltaY;
            mDataBean.mStickyPointCenter1.x = mDataBean.mStickyPointCenterMid.x + deltaX;
            mDataBean.mStickyPointCenter1.y = mDataBean.mStickyPointCenterMid.y - deltaY;
            //根据贝塞尔offset确定4个控制点
            float bezierMidLineX = currMoveX + bezierInnerMidLineDisX;
            float bezierMidLineY = currMoveY + bezierInnerMidLineDisY;

            mDataBean.mSidesInnerBesierCtrlPoint0.x = bezierMidLineX - bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint0.y = bezierMidLineY + bezierInnerSidesDisY;
            mDataBean.mSidesInnerBesierCtrlPoint1.x = bezierMidLineX + bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint1.y = bezierMidLineY - bezierInnerSidesDisY;

            float bezierOuterMidLineX = currMoveX + bezierOuterMidLineDisX;
            float bezierOuterMidLineY = currMoveY + bezierOuterMidLineDisY;

            mDataBean.mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX - bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY + bezierOuterSidesDisY;
            mDataBean.mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX + bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY - bezierOuterSidesDisY;
        } else if (mDataBean.mStickyPointCenterMid.x >= currMoveX && mDataBean.mStickyPointCenterMid.y < currMoveY) {
            //第3象限 + 正y轴
            if (mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_NO_LAUNCH
                    && mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_LAUNCH)
                mDataBean.mRotateDegrees = 180 - tanDegree;
            //得到两个桩点坐标
            mDataBean.mStickyPointCenter0.x = mDataBean.mStickyPointCenterMid.x + deltaX;
            mDataBean.mStickyPointCenter0.y = mDataBean.mStickyPointCenterMid.y + deltaY;
            mDataBean.mStickyPointCenter1.x = mDataBean.mStickyPointCenterMid.x - deltaX;
            mDataBean.mStickyPointCenter1.y = mDataBean.mStickyPointCenterMid.y - deltaY;
            //根据贝塞尔offset确定4个控制点
            float bezierMidLineX = currMoveX + bezierInnerMidLineDisX;
            float bezierMidLineY = currMoveY - bezierInnerMidLineDisY;

            mDataBean.mSidesInnerBesierCtrlPoint0.x = bezierMidLineX + bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint0.y = bezierMidLineY + bezierInnerSidesDisY;
            mDataBean.mSidesInnerBesierCtrlPoint1.x = bezierMidLineX - bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint1.y = bezierMidLineY - bezierInnerSidesDisY;

            float bezierOuterMidLineX = currMoveX + bezierOuterMidLineDisX;
            float bezierOuterMidLineY = currMoveY - bezierOuterMidLineDisY;

            mDataBean.mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX + bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY + bezierOuterSidesDisY;
            mDataBean.mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX - bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY - bezierOuterSidesDisY;
        } else if (mDataBean.mStickyPointCenterMid.x < currMoveX && mDataBean.mStickyPointCenterMid.y <= currMoveY) {
            //第4象限 + 正x轴
            if (mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_NO_LAUNCH
                    && mDataBean.mBubbleState != DataBean.BUBBLE_STATE_RELEASE_LAUNCH)
                mDataBean.mRotateDegrees = tanDegree;
            //得到两个桩点坐标
            mDataBean.mStickyPointCenter0.x = mDataBean.mStickyPointCenterMid.x + deltaX;
            mDataBean.mStickyPointCenter0.y = mDataBean.mStickyPointCenterMid.y - deltaY;
            mDataBean.mStickyPointCenter1.x = mDataBean.mStickyPointCenterMid.x - deltaX;
            mDataBean.mStickyPointCenter1.y = mDataBean.mStickyPointCenterMid.y + deltaY;
            //根据贝塞尔offset确定4个控制点
            float bezierMidLineX = currMoveX - bezierInnerMidLineDisX;
            float bezierMidLineY = currMoveY - bezierInnerMidLineDisY;

            mDataBean.mSidesInnerBesierCtrlPoint0.x = bezierMidLineX + bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint0.y = bezierMidLineY - bezierInnerSidesDisY;
            mDataBean.mSidesInnerBesierCtrlPoint1.x = bezierMidLineX - bezierInnerSidesDisX;
            mDataBean.mSidesInnerBesierCtrlPoint1.y = bezierMidLineY + bezierInnerSidesDisY;

            float bezierOuterMidLineX = currMoveX - bezierOuterMidLineDisX;
            float bezierOuterMidLineY = currMoveY - bezierOuterMidLineDisY;

            mDataBean.mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX + bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY - bezierOuterSidesDisY;
            mDataBean.mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX - bezierOuterSidesDisX;
            mDataBean.mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY + bezierOuterSidesDisY;
        }
    }

    private void updateMoveAndControlPoints(float xRaw, float yRaw) {
        {//更新移动点
            mDataBean.mMovePointCenter.x = xRaw;
            mDataBean.mMovePointCenter.y = yRaw;
            if (mDataBean.mIsReachMaxStretch && !mDataBean.mIsLaunchAnimStart/*launch动画开始的时候还是按照移动点来显示*/) {//超过最大区域就停留在最大区域的边缘
                //距离
                float dis = Utils.getTwoPointsDistance(xRaw, yRaw, mDataBean.mStickyPointCenterMid.x, mDataBean.mStickyPointCenterMid.y);
                float disDraw = mDataBean.mMaxStretch.width() / 2.0f;
                float ratio = dis / disDraw;
                mDataBean.mMoveDrawPointCenter.x = mDataBean.mStickyPointCenterMid.x + (xRaw - mDataBean.mStickyPointCenterMid.x) / ratio;
                mDataBean.mMoveDrawPointCenter.y = mDataBean.mStickyPointCenterMid.y + (yRaw - mDataBean.mStickyPointCenterMid.y) / ratio;
            } else {//如果没有就保持和移动点一致
                mDataBean.mMoveDrawPointCenter.x = xRaw;
                mDataBean.mMoveDrawPointCenter.y = yRaw;
            }
        }
        //控制点在launch动画开始的时候，由另一个方法来更新
        // see {@link updateMoveAndControlPointsElastic}
        if (!mDataBean.mIsLaunchAnimStart)
            updateMoveAndControlPointsElastic(mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y);
    }

    private void bounce(float startRawX, float startRawY) {
        if (mDataBean.mDragListener != null) {
            mDataBean.mDragListener.onOnBubbleReleaseWithoutLaunch(mDataBean.mClickView);
        }
        //气泡改为弹性状态
        mDataBean.mBubbleState = DataBean.BUBBLE_STATE_RELEASE_NO_LAUNCH;
        mDataBean.mIsBounceAnimStart = true;
        float offsetX = mDataBean.mStickyPointCenterMid.x - startRawX;
        float offsetY = mDataBean.mStickyPointCenterMid.y - startRawY;
        ValueAnimator anim = createLaunchAnim(startRawX, startRawY,
                mDataBean.mStickyPointCenterMid.x + offsetX * 0.15f, mDataBean.mStickyPointCenterMid.y + offsetY * 0.15f);
        anim.setInterpolator(new OvershootInterpolator(3f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDataBean.mLaunchXRaw = (float) animation.getAnimatedValue("x_path");
                mDataBean.mLaunchYRaw = (float) animation.getAnimatedValue("y_path");
                updateMoveAndControlPoints(mDataBean.mLaunchXRaw, mDataBean.mLaunchYRaw);
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //修改动画执行标志
                if (mDataBean != null) {
                    mDataBean.mIsBounceAnimStart = false;
                    if (mDataBean.mClickView != null) {
                        mDataBean.mClickView.setVisibility(View.VISIBLE);
                    }
                    clear();
                }
            }
        });
        anim.start();
    }

    private ValueAnimator createLaunchAnim(float startRawX, float startRawY, float endRawX, float endRawY) {
        PropertyValuesHolder xPro = PropertyValuesHolder.ofFloat("x_path", startRawX, endRawX);
        PropertyValuesHolder yPro = PropertyValuesHolder.ofFloat("y_path", startRawY, endRawY);
        ValueAnimator anim = ValueAnimator.ofPropertyValuesHolder(xPro, yPro);
        anim.setDuration(250);
        return anim;
    }

    private void endWithBubbleBurstAnim(float rawX, float rawY) {
        mDataBean.mBurstRectF.offsetTo(rawX - mDataBean.mBurstRectFRadius, rawY - mDataBean.mBurstRectFRadius);
        //气泡改为消失状态
        mDataBean.mBubbleState = DataBean.BUBBLE_STATE_BURSRT;
        mDataBean.mIsBurstAnimStart = true;
        //做一个int型属性动画，从0~mBurstDrawablesArray.length结束
        ValueAnimator anim = ValueAnimator.ofInt(0, mDataBean.mBitmapsExplodes.length - 1);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //设置当前绘制的爆炸图片index
                mDataBean.mCurDrawableIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //修改动画执行标志
                mDataBean.mIsBurstAnimStart = false;
                clear();
            }
        });
        anim.start();
    }

    /**
     * call when action_up/cancel
     *
     * @param rawX
     * @param rawY
     */
    public void prepareForAnim(float rawX, float rawY) {
//            if (isInResetArea(rawX, rawY)) {
//                resetToDefault();
//                return;
//            }
        //得到过两点直线的斜率和偏移
        float[] k_h = Utils.getTwoPointLine(mDataBean.mMoveDrawPointCenter, mDataBean.mStickyPointCenterMid);
        float explodeRawX = rawX;
        float explodeRawY = rawY;
        switch (mDataBean.mLaunchDire) {
            case DataBean.LAUNCH_TO_RIGHT:
            case DataBean.LAUNCH_TO_LEFT:
                //最终爆炸位置在左边和右边
                explodeRawX = mDataBean.mLaunchDire == DataBean.LAUNCH_TO_LEFT ? 0 : mDataBean.mScreenSize.x;
                explodeRawY = Utils.getYFromLine(k_h[0], k_h[1], explodeRawX);
                break;
            case DataBean.LAUNCH_TO_BOTTOM:
            case DataBean.LAUNCH_TO_TOP:
                //最终爆炸位置在上边和下边
                explodeRawY = mDataBean.mLaunchDire == DataBean.LAUNCH_TO_TOP ? 0 : mDataBean.mScreenSize.y;
                explodeRawX = Utils.getXFromLine(k_h[0], k_h[1], explodeRawY);
                break;
            case DataBean.LAUNCH_DIRE_NONE:
                explodeRawX = rawX;
                explodeRawY = rawY;
                break;
        }
        if (isReadyToLaunch(rawX, rawY)) {
            launch(mDataBean.mMoveDrawPointCenter.x, mDataBean.mMoveDrawPointCenter.y, explodeRawX, explodeRawY);
        } else {
            bounce(rawX, rawY);
        }
    }

    public void clear() {
        if (mDataBean.mBitmapsExplodes != null && mDataBean.mBitmapsExplodes.length > 0) {
            for (int i = 0; i < mDataBean.mBitmapsExplodes.length; i++) {
                mDataBean.mBitmapsExplodes[i].recycle();
                mDataBean.mBitmapsExplodes[i] = null;
            }
            mDataBean.mBitmapsExplodes = null;
        }
        setWillNotDraw(true);
        mDataBean.mClickView.invalidate();
        mDataBean = null;
    }

    public void setPaintMaskFilter(int mode) {
        switch (mode) {
            case UsoppBubble.MODE_NONE:
                break;
            case UsoppBubble.MODE_GLOW:
                mDataBean.mFilter = new BlurMaskFilter(10, BlurMaskFilter.Blur.OUTER);
                break;
            case UsoppBubble.MODE_EMBOSS:
                float[] direction = new float[]{10, 10, 10};
                float ambient = 0.5f;
                float specular = 1;
                float blurRadius = 1;
                mDataBean.mFilter = new EmbossMaskFilter(direction, ambient, specular, blurRadius);
                break;
        }
        mDataBean.mPaint.setMaskFilter(mDataBean.mFilter);
    }

}
