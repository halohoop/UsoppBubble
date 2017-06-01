package com.halohoop.usoppbubble.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
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
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.halohoop.usoppbubble.R;
import com.halohoop.usoppbubble.utils.Utils;

/**
 * Created by Pooholah on 2017/5/26.
 */

public class UsoppBubble extends AppCompatTextView implements DraggableListener {
    private BubblesInternalView mBubblesView = null;
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
        mBubblesView = new BubblesInternalView(getContext(), this, rawX, rawY, contentOffset);
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

    /**
     * 将要加入wm的view
     */
    private static class BubblesInternalView extends View {

        private boolean DEBUG = true;
        private Paint debugPaint;

        public void setDEBUG(boolean DEBUG) {
            this.DEBUG = DEBUG;
            debugPaint = null;
        }

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
        private float mLaunchYRaw;
        private float mLaunchXRaw;
        private Path mElasticPath;
        private int mElasticColor = Color.rgb(255, 78, 18);

        public BubblesInternalView(Context context, UsoppBubble clickView, float rawX, float rawY,
                                   float contentOffset) {
            super(context, null, 0);
            if (DEBUG) {
                debugPaint = new Paint();
            }
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            this.mDragListener = clickView.getDragListener();
            init(clickView, rawX, rawY, contentOffset);
        }

        private void init(UsoppBubble clickView, float rawX, float rawY, float contentOffset) {
            this.mClickView = clickView;
            mScreenSize = Utils.getContentSize(Utils.scanForActivity(getContext()), contentOffset);

            mResetSensorRaidus = Math.min(mScreenSize.x, mScreenSize.y) / 14.4f;//经验值
            mLaunchThreadhold = Math.min(mScreenSize.x, mScreenSize.y) / 9f;//经验值

            //创建全屏的container，并且将自己的加进去
            createFullScreenContainerAndAddSelf();

            //确定固定的中点
            mClickViewRect = new Rect();
//            clickView.getGlobalVisibleRect(mClickViewRect);
            clickView.getLocalVisibleRect(mClickViewRect);
            int[] location = new int[2];
//            clickView.getLocationOnScreen(location);
            clickView.getLocationInWindow(location);
            int left = location[0];
            int top = location[1];
            mClickViewRect.offset(left, top);
            mStickyPointCenterMid.x = mClickViewRect.centerX();
            mStickyPointCenterMid.y = mClickViewRect.centerY() - contentOffset;
            mHalfClickViewWidth = mClickViewRect.width() / 2.0f;

            mStickyPointRaidus = Math.max(mClickViewRect.width(), mClickViewRect.height()) / 7.0f;//经验值
            mStickyPointRaidusReady = Math.max(mClickViewRect.width(), mClickViewRect.height()) / 7.0f;//经验值

            mMovePointQuadMidLineOffset = Math.min(mClickViewRect.width(), mClickViewRect.height()) / 11.6f;//经验值
            mMovePointQuadSidesOffset = Math.min(mClickViewRect.width(), mClickViewRect.height()) / 1.16f;//经验值
            mQuadSidesOuterOffset = Math.min(mClickViewRect.width(), mClickViewRect.height()) / 2.32f;//经验值
            mQuadMidLineOuterOffset = Math.min(mClickViewRect.width(), mClickViewRect.height()) / 1.9f;//经验值

            mDragBitmapOffsetX = -mClickViewRect.width();
            mDragBitmapOffsetY = -mClickViewRect.height() / 2.0f;

            mLaunchArea = new RectF(mStickyPointCenterMid.x - mLaunchThreadhold,
                    mStickyPointCenterMid.y - mLaunchThreadhold,
                    mStickyPointCenterMid.x + mLaunchThreadhold,
                    mStickyPointCenterMid.y + mLaunchThreadhold);
            float oneOf3OfScreenWidth = mScreenSize.x / 3.0f;
            mMaxStretch = new RectF(mStickyPointCenterMid.x - oneOf3OfScreenWidth,
                    mStickyPointCenterMid.y - oneOf3OfScreenWidth,
                    mStickyPointCenterMid.x + oneOf3OfScreenWidth,
                    mStickyPointCenterMid.y + oneOf3OfScreenWidth);

            //当手指在这个区域内就不做任何旋转
            mStickySensor = new RectF(mClickViewRect.centerX() - mResetSensorRaidus, mClickViewRect.centerY() - mResetSensorRaidus,
                    mClickViewRect.centerX() + mResetSensorRaidus, mClickViewRect.centerY() + mResetSensorRaidus);

            mMovePointCenter.x = rawX;
            mMovePointCenter.y = rawY;
            mMoveDrawPointCenter.x = rawX;
            mMoveDrawPointCenter.y = rawY;

            updatePointsDispatch(rawX, rawY);

            //初始化爆炸动画
            initEndAnimationBitmaps();

            //创建画笔
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(1);
            mPaint.setColor(mElasticColor);
            mElasticPath = new Path();

            setPaintMaskFilter(mClickView.getMode());
        }

        //        private WindowManager mWm;
//        private WindowManager.LayoutParams mLayoutParams;
        private ViewGroup mContentContainer;
        private ViewGroup.LayoutParams mLayoutParams;

        private void createFullScreenContainerAndAddSelf() {
            //way 1----
//            mWm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//            mLayoutParams = new WindowManager.LayoutParams();
//            mLayoutParams.alpha = 1.0f;
//            mLayoutParams.format = PixelFormat.RGBA_8888;
//            mLayoutParams.width = mScreenSize.x;
//            mLayoutParams.height = mScreenSize.y;
//            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
//            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
//            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;//全屏，不设置就会排在状态栏下面
//            mLayoutParams.x = 0;
//            mLayoutParams.y = 0;
//            addViewForShowingBubble(mWm, this, mLayoutParams);

            //way 2----
            if (mContentContainer == null) {
                mContentContainer = getFullScreenContainer(Utils.scanForActivity(getContext()));
            }

            mLayoutParams = new ViewGroup.LayoutParams(mScreenSize.x, mScreenSize.y);
            addViewForShowingBubble(mContentContainer, this, mLayoutParams);
        }

        private FrameLayout getFullScreenContainer(Activity activity) {
            return (FrameLayout) activity.findViewById(android.R.id.content);
        }

        private float mRotateDegrees = 0;
        /**
         * 是否拖出达到了可以发射的区域
         */
        private boolean mIsReadyToLaunch = false;
        /**
         * 是否拖出达到了最大拉伸距离
         */
        private boolean mIsReachMaxStretch = false;

        /**
         * 更新弹弓固定位置的点
         *
         * @param currMoveX
         * @param currMoveY
         */
        public void updatePointsDispatch(float currMoveX, float currMoveY) {
            mBubbleState = BUBBLE_STATE_DRAGGING;
            mIsReadyToLaunch = isReadyToLaunch(currMoveX, currMoveY);
            mIsReachMaxStretch = isReachMaxStretch(currMoveX, currMoveY);

            //进入了重置区域
            /*if (isInResetArea(currMoveX, currMoveY)) {
                mRotateDegrees = 0;
                mIsDrawReset = true;
                mDragBitmapOffsetX = -mDragBitmap.getWidth() / 2.0f;
                mDragBitmapOffsetY = -mDragBitmap.getHeight() / 2.0f;
                return;
            }
            mIsDrawReset = false;*/

            //首先更新移动点不要错乱，因为后面可能需要用到最新的移动点
            updateMoveAndControlPoints(currMoveX, currMoveY);

            updateLaunchDirection(mStickyPointCenterMid, mMovePointCenter);
        }


        private boolean isInResetArea(float rawX, float rawY) {
            return mStickySensor.contains(rawX, rawY);
        }

        private void updateLaunchDirection(PointF stickyPointCenterMid, PointF movePointCenter) {
            //四条线，固定的中点连接四个角形成
            float[] k_h_LeftTop = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(0, 0));
            float[] k_h_RightTop = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(mScreenSize.x, 0));
            float[] k_h_LeftBottom = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(0, mScreenSize.y));
            float[] k_h_RightBottom = Utils.getTwoPointLine(stickyPointCenterMid, new PointF(mScreenSize.x, mScreenSize.y));
            if ((k_h_RightBottom[0] * movePointCenter.x + k_h_RightBottom[1]) <= movePointCenter.y
                    && (k_h_RightTop[0] * movePointCenter.x + k_h_RightTop[1]) >= movePointCenter.y) {
                mLaunchDire = LAUNCH_TO_RIGHT;
            } else if ((k_h_LeftBottom[0] * movePointCenter.x + k_h_LeftBottom[1]) >= movePointCenter.y
                    && (k_h_RightBottom[0] * movePointCenter.x + k_h_RightBottom[1]) >= movePointCenter.y) {
                mLaunchDire = LAUNCH_TO_BOTTOM;
            } else if ((k_h_LeftTop[0] * movePointCenter.x + k_h_LeftTop[1]) >= movePointCenter.y
                    && (k_h_LeftBottom[0] * movePointCenter.x + k_h_LeftBottom[1]) <= movePointCenter.y) {
                mLaunchDire = LAUNCH_TO_LEFT;
            } else if ((k_h_LeftTop[0] * movePointCenter.x + k_h_LeftTop[1]) <= movePointCenter.y
                    && (k_h_RightTop[0] * movePointCenter.x + k_h_RightTop[1]) <= movePointCenter.y) {
                mLaunchDire = LAUNCH_TO_TOP;
            } else {
                mLaunchDire = LAUNCH_DIRE_NONE;
            }
        }

        /**
         * 初始化最后的爆炸动画
         */
        private void initEndAnimationBitmaps() {
            mBitmapsExplodes = new Bitmap[5];
            for (int i = 0; i < 5; i++) {
                int identifier = getContext().getResources().getIdentifier("burst_" + (i + 1), "drawable", getContext().getPackageName());
                Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), identifier);
                mBitmapsExplodes[i] = bitmap;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (DEBUG) {
                debugPaint.setTextSize(50);
                debugPaint.setColor(Color.GREEN);
                canvas.drawText("DEBUG_MODE_UsoppBubble", 0, mScreenSize.y / 2, debugPaint);
            }
            if (mBubbleState == BUBBLE_STATE_RELEASE_NO_LAUNCH) {
                drawBounce(canvas);
            } else if (mBubbleState == BUBBLE_STATE_RELEASE_LAUNCH) {
                drawLaunch(canvas);
            } else if (mBubbleState == BUBBLE_STATE_BURSRT) {
                drawBurst(canvas);
            } else if (mBubbleState == BUBBLE_STATE_DRAGGING) {
                drawDragging(canvas);
            }
            /*//just for test
            mPaint.setMaskFilter(null);
            mPaint.setColor(Color.rgb(150, 215, 240));
            RectF rectF = new RectF(mClickViewRect);
            rectF.offset(0, 420);
            float offset = mClickViewRect.width() - ((float) mClickViewRect.width()) / 1.845f;
            rectF.left += offset;
            Utils.l("width:" + rectF.width());
            Utils.l("width:ratio:" + ((float) mClickViewRect.width()) / 1.845);
            canvas.drawArc(rectF, -90, 180, true, mPaint);
            rectF.left -= offset;
            rectF.right -= offset;
            canvas.drawArc(rectF, 90, 180, true, mPaint);
            mPaint.setMaskFilter(mFilter);*/
        }

        //        private boolean mIsDrawReset = false;
        private void drawDragging(Canvas canvas) {
            canvas.save();
            canvas.rotate(mRotateDegrees, mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
            canvas.translate(mDragBitmapOffsetX, mDragBitmapOffsetY);
//            if (mDragBitmap != null && !mDragBitmap.isRecycled())
//                canvas.drawBitmap(mDragBitmap, mMoveDrawPointCenter.x, mMoveDrawPointCenter.y, null);
//            drawBubble(canvas,mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
            //TODO
            canvas.translate(mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
//            canvas.drawLine(0,0,200,200,mPaint);
            drawBubble(canvas);
            canvas.restore();
            /*if (!mIsDrawReset) {
                canvas.save();
                canvas.rotate(mRotateDegrees, mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
            }
            canvas.translate(mDragBitmapOffsetX, mDragBitmapOffsetY);
            if (mIsDrawReset) {
                if (mDragBitmap != null && !mDragBitmap.isRecycled())
                    canvas.drawBitmap(mDragBitmap, mStickyPointCenterMid.x, mStickyPointCenterMid.y, null);
                return;
            } else {
                if (mDragBitmap != null && !mDragBitmap.isRecycled())
                    canvas.drawBitmap(mDragBitmap, mMoveDrawPointCenter.x, mMoveDrawPointCenter.y, null);
                canvas.restore();
            }*/
            mElasticPath.reset();
            mElasticPath.moveTo(mStickyPointCenter0.x, mStickyPointCenter0.y);
            mElasticPath.quadTo(mSidesInnerBesierCtrlPoint0.x, mSidesInnerBesierCtrlPoint0.y,
                    mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
            mElasticPath.quadTo(mSidesInnerBesierCtrlPoint1.x, mSidesInnerBesierCtrlPoint1.y,
                    mStickyPointCenter1.x, mStickyPointCenter1.y);

            mElasticPath.quadTo(mSidesOuterBesierCtrlPoint1.x, mSidesOuterBesierCtrlPoint1.y,
                    mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
            mElasticPath.quadTo(mSidesOuterBesierCtrlPoint0.x, mSidesOuterBesierCtrlPoint0.y,
                    mStickyPointCenter0.x, mStickyPointCenter0.y);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mElasticColor);
            mPaint.setStrokeWidth(1);
            mPaint.setMaskFilter(mFilter);
            canvas.drawPath(mElasticPath, mPaint);

            //画固定点的桩
            drawStickyPoints(canvas);
        }

        private void drawBurst(Canvas canvas) {
            if (mBitmapsExplodes != null && mBurstRectF != null) {
                canvas.drawBitmap(mBitmapsExplodes[mCurDrawableIndex], null, mBurstRectF, null);
            }
        }

        private void drawBounce(Canvas canvas) {
            canvas.save();
            canvas.translate(mLaunchXRaw, mLaunchYRaw);
            canvas.rotate(mRotateDegrees, 0, 0);
            canvas.translate(-mClickViewRect.width(), -mClickViewRect.height() / 2.0f);
            drawBubble(canvas);
            canvas.restore();
            mElasticPath.reset();
            mElasticPath.moveTo(mStickyPointCenter0.x, mStickyPointCenter0.y);
            mElasticPath.quadTo(mSidesInnerBesierCtrlPoint0.x, mSidesInnerBesierCtrlPoint0.y,
                    mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
            mElasticPath.quadTo(mSidesInnerBesierCtrlPoint1.x, mSidesInnerBesierCtrlPoint1.y,
                    mStickyPointCenter1.x, mStickyPointCenter1.y);

            mElasticPath.quadTo(mSidesOuterBesierCtrlPoint1.x, mSidesOuterBesierCtrlPoint1.y,
                    mMoveDrawPointCenter.x, mMoveDrawPointCenter.y);
            mElasticPath.quadTo(mSidesOuterBesierCtrlPoint0.x, mSidesOuterBesierCtrlPoint0.y,
                    mStickyPointCenter0.x, mStickyPointCenter0.y);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mElasticColor);
            mPaint.setStrokeWidth(1);
            mPaint.setMaskFilter(mFilter);
            canvas.drawPath(mElasticPath, mPaint);

            //画固定点的桩
            drawStickyPoints(canvas);
        }

        private void drawLaunch(Canvas canvas) {
            //TODO
            /*if (mDragBitmap != null && !mDragBitmap.isRecycled()) {
                canvas.save();
                canvas.translate(mLaunchXRaw, mLaunchYRaw);
                canvas.save();
                canvas.rotate(mRotateDegrees, 0, 0);
                canvas.translate(-mDragBitmap.getWidth(), -mDragBitmap.getHeight() / 2.0f);
                canvas.drawBitmap(mDragBitmap, 0, 0, null);
                canvas.restore();
                canvas.restore();
            }*/
            //TODO
            canvas.save();
            canvas.translate(mLaunchXRaw, mLaunchYRaw);
            canvas.rotate(mRotateDegrees, 0, 0);
            canvas.translate(-mClickViewRect.width(), -mClickViewRect.height() / 2.0f);
//            canvas.drawBitmap(mDragBitmap, 0, 0, null);
//            canvas.drawLine(0,0,200,200,mPaint);
            drawBubble(canvas);
            canvas.restore();
            mElasticPath.reset();
            mElasticPath.moveTo(mStickyPointCenter0.x, mStickyPointCenter0.y);
            mElasticPath.quadTo(mSidesInnerBesierCtrlPoint0.x, mSidesInnerBesierCtrlPoint0.y,
                    mMoveElasticDrawPointCenterWhenLaunch.x, mMoveElasticDrawPointCenterWhenLaunch.y);
            mElasticPath.quadTo(mSidesInnerBesierCtrlPoint1.x, mSidesInnerBesierCtrlPoint1.y,
                    mStickyPointCenter1.x, mStickyPointCenter1.y);

            mElasticPath.quadTo(mSidesOuterBesierCtrlPoint1.x, mSidesOuterBesierCtrlPoint1.y,
                    mMoveElasticDrawPointCenterWhenLaunch.x, mMoveElasticDrawPointCenterWhenLaunch.y);
            mElasticPath.quadTo(mSidesOuterBesierCtrlPoint0.x, mSidesOuterBesierCtrlPoint0.y,
                    mStickyPointCenter0.x, mStickyPointCenter0.y);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mElasticColor);
            mPaint.setStrokeWidth(1);
            mPaint.setMaskFilter(mFilter);
            canvas.drawPath(mElasticPath, mPaint);

            //画固定点的桩
            drawStickyPoints(canvas);
        }

        private void drawBubble(Canvas canvas) {
            //TODO
            /*//just for test
            mPaint.setMaskFilter(null);
            mPaint.setColor(Color.rgb(150, 215, 240));
            RectF rectF = new RectF(mClickViewRect);
            rectF.offset(0, 420);
            float offset = mClickViewRect.width() - ((float) mClickViewRect.width()) / 1.845f;
            rectF.left += offset;
            Utils.l("width:" + rectF.width());
            Utils.l("width:ratio:" + ((float) mClickViewRect.width()) / 1.845);
            canvas.drawArc(rectF, -90, 180, true, mPaint);
            rectF.left -= offset;
            rectF.right -= offset;
            canvas.drawArc(rectF, 90, 180, true, mPaint);
            mPaint.setMaskFilter(mFilter);*/

            float left = 0;
            float top = 0;
            float right = mClickViewRect.width();
            float bottom = mClickViewRect.height();
            float midHeight = bottom / 2.0f;
            float midWidth = right / 2.0f;

            mPaint.setMaskFilter(null);
            mPaint.setColor(mClickView.getDragBackgroundColor());
            float offset = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                offset = mClickViewRect.width() - ((float) mClickViewRect.width()) / 1.845f;
                canvas.drawArc(left + offset, top, right, bottom, -90, 180, true, mPaint);
                canvas.drawArc(left, top, right - offset, bottom, 90, 180, true, mPaint);
            } else {
                offset = mClickViewRect.width() - ((float) mClickViewRect.width()) / 1.35f;
                canvas.drawCircle(left + offset, midHeight, midHeight, mPaint);
                canvas.drawCircle(right - offset, midHeight, midHeight, mPaint);
            }
            float halfDrawWidth = (right - (left + offset)) / 2.0f;
            canvas.drawRect(halfDrawWidth, 0, right - halfDrawWidth, bottom, mPaint);
            TextPaint paint = mClickView.getPaint();
            String text = (String) mClickView.getText();
            float halfMeasureTextWidth = paint.measureText(text, 0, text.length()) / 2.0f;
            paint.getTextBounds(text, 0, text.length(), mTextBound);
            canvas.drawText(text, 0, text.length(), midWidth - halfMeasureTextWidth, midHeight + mTextBound.height() / 2.0f, paint);
        }

        private Rect mTextBound = new Rect();

        private void drawStickyPoints(Canvas canvas) {
            mPaint.setStrokeWidth(1);//只要不是stoke模式，画圆就和这个设置参数没关系
            mPaint.setColor(mIsReadyToLaunch ? mColorStickyPointCenterReady : mColorStickyPointCenter);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setMaskFilter(mFilter);
            canvas.drawCircle(mStickyPointCenter0.x, mStickyPointCenter0.y, mStickyPointRaidusReady, mPaint);
            canvas.drawCircle(mStickyPointCenter1.x, mStickyPointCenter1.y, mStickyPointRaidusReady, mPaint);
            if (!mIsReadyToLaunch) {
                mPaint.setColor(mColorStickyPointCenterNotReady);
                canvas.drawCircle(mStickyPointCenter0.x, mStickyPointCenter0.y, mStickyPointRaidus, mPaint);
                canvas.drawCircle(mStickyPointCenter1.x, mStickyPointCenter1.y, mStickyPointRaidus, mPaint);
            }
        }

        /**
         * 是到到达发射距离
         * @param rawX
         * @param rawY
         * @return
         */
        private boolean isReadyToLaunch(float rawX, float rawY) {
//            return !mLaunchArea.contains(rawX, rawY);
            float hypot = (float) Math.hypot(rawX - mLaunchArea.centerX(), rawY - mLaunchArea.centerY());
            return hypot <= mLaunchArea.width() / 2 ? false : true;
        }

        /**
         * 是否到达最大拉伸距离
         * @param rawX
         * @param rawY
         * @return
         */
        private boolean isReachMaxStretch(float rawX, float rawY) {
//            return !mLaunchArea.contains(rawX, rawY);
            float hypot = (float) Math.hypot(rawX - mMaxStretch.centerX(), rawY - mMaxStretch.centerY());
            return hypot <= mMaxStretch.width() / 2 ? false : true;
        }

        private void launch(float startRawX, float startRawY, final float endRawX, final float endRawY) {
            if (mDragListener != null) {
                mDragListener.onOnBubbleReleaseWithLaunch(mClickView);
            }
            //气泡改为发射状态
            mBubbleState = BUBBLE_STATE_RELEASE_LAUNCH;
            mIsLaunchAnimStart = true;

            ValueAnimator launchAnim = createLaunchAnim(startRawX, startRawY, endRawX, endRawY);
            launchAnim.setInterpolator(new AccelerateInterpolator(0.4f));
            launchAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mLaunchXRaw = (float) animation.getAnimatedValue("x_path");
                    mLaunchYRaw = (float) animation.getAnimatedValue("y_path");
                    updateMoveAndControlPoints(mLaunchXRaw, mLaunchYRaw);
                }
            });

            ValueAnimator elasticAnim = null;
            {//橡皮筋的动画
                float offsetX = mStickyPointCenterMid.x - startRawX;
                float offsetY = mStickyPointCenterMid.y - startRawY;
                float endX = mStickyPointCenterMid.x + offsetX * 0.15f;
                float endY = mStickyPointCenterMid.y + offsetY * 0.15f;
                PropertyValuesHolder xPro = PropertyValuesHolder.ofFloat("x_path", startRawX, endX);
                PropertyValuesHolder yPro = PropertyValuesHolder.ofFloat("y_path", startRawY, endY);
                elasticAnim = ValueAnimator.ofPropertyValuesHolder(xPro, yPro);
                elasticAnim.setInterpolator(new OvershootInterpolator(3f));
                elasticAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mMoveElasticDrawPointCenterWhenLaunch.x = (float) animation.getAnimatedValue("x_path");
                        mMoveElasticDrawPointCenterWhenLaunch.y = (float) animation.getAnimatedValue("y_path");
                        updateMoveAndControlPointsElastic(
                                mMoveElasticDrawPointCenterWhenLaunch.x,
                                mMoveElasticDrawPointCenterWhenLaunch.y);
                        invalidate();
                    }
                });
            }

            AnimatorSet as = new AnimatorSet();
            elasticAnim.setDuration(300);
            launchAnim.setDuration(150);
            as.playTogether(launchAnim,elasticAnim);
            as.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //修改动画执行标志
                    mIsLaunchAnimStart = false;
                    endWithBubbleBurstAnim(endRawX, endRawY);
                }
            });
            as.start();
        }

        private void updateMoveAndControlPointsElastic(float currMoveX, float currMoveY) {
            //更新贝塞尔的控制点
            float innerQuadSidesOffset = mMovePointQuadSidesOffset;
            float outerQuadSidesOffset = mQuadSidesOuterOffset;
            if (!mIsReadyToLaunch) {
                float ratioInLaunchArea = Utils.getTwoPointsDistance(currMoveX, currMoveY, mStickyPointCenterMid.x, mStickyPointCenterMid.y) / (mLaunchArea.width() / 2);
                innerQuadSidesOffset = ratioInLaunchArea * mMovePointQuadSidesOffset;
                outerQuadSidesOffset = ratioInLaunchArea * mQuadSidesOuterOffset;
                mStickyPointRaidus = mStickyPointRaidusReady * (1 - ratioInLaunchArea);
            }

            float disX = Math.abs(mStickyPointCenterMid.x - currMoveX);
            float disY = Math.abs(mStickyPointCenterMid.y - currMoveY);
            double tanRadian = Math.atan(disY / disX);
            float tanDegree = 0;
            if (mBubbleState != BUBBLE_STATE_RELEASE_NO_LAUNCH
                    && mBubbleState != BUBBLE_STATE_RELEASE_LAUNCH) {
                tanDegree = Utils.radian2Degree(tanRadian);
            }
            mDragBitmapOffsetX = -mClickViewRect.width();
            mDragBitmapOffsetY = -mClickViewRect.height() / 2.0f;
            float deltaX = (float) (Math.sin(tanRadian) * mHalfClickViewWidth * mLargerRatio);
            float deltaY = (float) (Math.cos(tanRadian) * mHalfClickViewWidth * mLargerRatio);

            float bezierInnerMidLineDisX = (float) (Math.cos(tanRadian) * mMovePointQuadMidLineOffset);
            float bezierInnerMidLineDisY = (float) (Math.sin(tanRadian) * mMovePointQuadMidLineOffset);
            float bezierInnerSidesDisX = (float) (Math.sin(tanRadian) * innerQuadSidesOffset);
            float bezierInnerSidesDisY = (float) (Math.cos(tanRadian) * innerQuadSidesOffset);

            float bezierOuterMidLineDisX = (float) (Math.cos(tanRadian) * (mMovePointQuadMidLineOffset - mQuadMidLineOuterOffset));
            float bezierOuterMidLineDisY = (float) (Math.sin(tanRadian) * (mMovePointQuadMidLineOffset - mQuadMidLineOuterOffset));
            float bezierOuterSidesDisX = (float) (Math.sin(tanRadian) * (innerQuadSidesOffset + outerQuadSidesOffset));
            float bezierOuterSidesDisY = (float) (Math.cos(tanRadian) * (innerQuadSidesOffset + outerQuadSidesOffset));
            if (mStickyPointCenterMid.x <= currMoveX && mStickyPointCenterMid.y > currMoveY) {
                //第1象限 + 负y轴
                if (mBubbleState != BUBBLE_STATE_RELEASE_NO_LAUNCH
                        && mBubbleState != BUBBLE_STATE_RELEASE_LAUNCH)
                    mRotateDegrees = -tanDegree;
                //得到两个桩点坐标
                mStickyPointCenter0.x = mStickyPointCenterMid.x - deltaX;
                mStickyPointCenter0.y = mStickyPointCenterMid.y - deltaY;
                mStickyPointCenter1.x = mStickyPointCenterMid.x + deltaX;
                mStickyPointCenter1.y = mStickyPointCenterMid.y + deltaY;
                //根据贝塞尔offset确定4个控制点
                float bezierInnerMidLineX = currMoveX - bezierInnerMidLineDisX;
                float bezierInnerMidLineY = currMoveY + bezierInnerMidLineDisY;

                mSidesInnerBesierCtrlPoint0.x = bezierInnerMidLineX - bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint0.y = bezierInnerMidLineY - bezierInnerSidesDisY;
                mSidesInnerBesierCtrlPoint1.x = bezierInnerMidLineX + bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint1.y = bezierInnerMidLineY + bezierInnerSidesDisY;

                float bezierOuterMidLineX = currMoveX - bezierOuterMidLineDisX;
                float bezierOuterMidLineY = currMoveY + bezierOuterMidLineDisY;

                mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX - bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY - bezierOuterSidesDisY;
                mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX + bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY + bezierOuterSidesDisY;
            } else if (mStickyPointCenterMid.x > currMoveX && mStickyPointCenterMid.y >= currMoveY) {
                //第2象限 + 负x轴
                if (mBubbleState != BUBBLE_STATE_RELEASE_NO_LAUNCH
                        && mBubbleState != BUBBLE_STATE_RELEASE_LAUNCH)
                    mRotateDegrees = -(180 - tanDegree);
                //得到两个桩点坐标
                mStickyPointCenter0.x = mStickyPointCenterMid.x - deltaX;
                mStickyPointCenter0.y = mStickyPointCenterMid.y + deltaY;
                mStickyPointCenter1.x = mStickyPointCenterMid.x + deltaX;
                mStickyPointCenter1.y = mStickyPointCenterMid.y - deltaY;
                //根据贝塞尔offset确定4个控制点
                float bezierMidLineX = currMoveX + bezierInnerMidLineDisX;
                float bezierMidLineY = currMoveY + bezierInnerMidLineDisY;

                mSidesInnerBesierCtrlPoint0.x = bezierMidLineX - bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint0.y = bezierMidLineY + bezierInnerSidesDisY;
                mSidesInnerBesierCtrlPoint1.x = bezierMidLineX + bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint1.y = bezierMidLineY - bezierInnerSidesDisY;

                float bezierOuterMidLineX = currMoveX + bezierOuterMidLineDisX;
                float bezierOuterMidLineY = currMoveY + bezierOuterMidLineDisY;

                mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX - bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY + bezierOuterSidesDisY;
                mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX + bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY - bezierOuterSidesDisY;
            } else if (mStickyPointCenterMid.x >= currMoveX && mStickyPointCenterMid.y < currMoveY) {
                //第3象限 + 正y轴
                if (mBubbleState != BUBBLE_STATE_RELEASE_NO_LAUNCH
                        && mBubbleState != BUBBLE_STATE_RELEASE_LAUNCH)
                    mRotateDegrees = 180 - tanDegree;
                //得到两个桩点坐标
                mStickyPointCenter0.x = mStickyPointCenterMid.x + deltaX;
                mStickyPointCenter0.y = mStickyPointCenterMid.y + deltaY;
                mStickyPointCenter1.x = mStickyPointCenterMid.x - deltaX;
                mStickyPointCenter1.y = mStickyPointCenterMid.y - deltaY;
                //根据贝塞尔offset确定4个控制点
                float bezierMidLineX = currMoveX + bezierInnerMidLineDisX;
                float bezierMidLineY = currMoveY - bezierInnerMidLineDisY;

                mSidesInnerBesierCtrlPoint0.x = bezierMidLineX + bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint0.y = bezierMidLineY + bezierInnerSidesDisY;
                mSidesInnerBesierCtrlPoint1.x = bezierMidLineX - bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint1.y = bezierMidLineY - bezierInnerSidesDisY;

                float bezierOuterMidLineX = currMoveX + bezierOuterMidLineDisX;
                float bezierOuterMidLineY = currMoveY - bezierOuterMidLineDisY;

                mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX + bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY + bezierOuterSidesDisY;
                mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX - bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY - bezierOuterSidesDisY;
            } else if (mStickyPointCenterMid.x < currMoveX && mStickyPointCenterMid.y <= currMoveY) {
                //第4象限 + 正x轴
                if (mBubbleState != BUBBLE_STATE_RELEASE_NO_LAUNCH
                        && mBubbleState != BUBBLE_STATE_RELEASE_LAUNCH)
                    mRotateDegrees = tanDegree;
                //得到两个桩点坐标
                mStickyPointCenter0.x = mStickyPointCenterMid.x + deltaX;
                mStickyPointCenter0.y = mStickyPointCenterMid.y - deltaY;
                mStickyPointCenter1.x = mStickyPointCenterMid.x - deltaX;
                mStickyPointCenter1.y = mStickyPointCenterMid.y + deltaY;
                //根据贝塞尔offset确定4个控制点
                float bezierMidLineX = currMoveX - bezierInnerMidLineDisX;
                float bezierMidLineY = currMoveY - bezierInnerMidLineDisY;

                mSidesInnerBesierCtrlPoint0.x = bezierMidLineX + bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint0.y = bezierMidLineY - bezierInnerSidesDisY;
                mSidesInnerBesierCtrlPoint1.x = bezierMidLineX - bezierInnerSidesDisX;
                mSidesInnerBesierCtrlPoint1.y = bezierMidLineY + bezierInnerSidesDisY;

                float bezierOuterMidLineX = currMoveX - bezierOuterMidLineDisX;
                float bezierOuterMidLineY = currMoveY - bezierOuterMidLineDisY;

                mSidesOuterBesierCtrlPoint0.x = bezierOuterMidLineX + bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint0.y = bezierOuterMidLineY - bezierOuterSidesDisY;
                mSidesOuterBesierCtrlPoint1.x = bezierOuterMidLineX - bezierOuterSidesDisX;
                mSidesOuterBesierCtrlPoint1.y = bezierOuterMidLineY + bezierOuterSidesDisY;
            }
        }
        private void updateMoveAndControlPoints(float xRaw, float yRaw) {
            {//更新移动点
                mMovePointCenter.x = xRaw;
                mMovePointCenter.y = yRaw;
                if (mIsReachMaxStretch && !mIsLaunchAnimStart/*launch动画开始的时候还是按照移动点来显示*/) {//超过最大区域就停留在最大区域的边缘
                    //距离
                    float dis = Utils.getTwoPointsDistance(xRaw, yRaw, mStickyPointCenterMid.x, mStickyPointCenterMid.y);
                    float disDraw = mMaxStretch.width() / 2.0f;
                    float ratio = dis / disDraw;
                    mMoveDrawPointCenter.x = mStickyPointCenterMid.x + (xRaw - mStickyPointCenterMid.x) / ratio;
                    mMoveDrawPointCenter.y = mStickyPointCenterMid.y + (yRaw - mStickyPointCenterMid.y) / ratio;
                } else {//如果没有就保持和移动点一致
                    mMoveDrawPointCenter.x = xRaw;
                    mMoveDrawPointCenter.y = yRaw;
                }
            }
            //控制点在launch动画开始的时候，由另一个方法来更新
            // see {@link updateMoveAndControlPointsElastic}
            if(!mIsLaunchAnimStart)
                updateMoveAndControlPointsElastic(mMoveDrawPointCenter.x,mMoveDrawPointCenter.y);
        }

        private void bounce(float startRawX, float startRawY) {
            if (mDragListener != null) {
                mDragListener.onOnBubbleReleaseWithoutLaunch(mClickView);
            }
            //气泡改为弹性状态
            mBubbleState = BUBBLE_STATE_RELEASE_NO_LAUNCH;
            mIsBounceAnimStart = true;
            float offsetX = mStickyPointCenterMid.x - startRawX;
            float offsetY = mStickyPointCenterMid.y - startRawY;
            ValueAnimator anim = createLaunchAnim(startRawX, startRawY,
                    mStickyPointCenterMid.x + offsetX * 0.15f, mStickyPointCenterMid.y + offsetY * 0.15f);
            anim.setInterpolator(new OvershootInterpolator(3f));
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mLaunchXRaw = (float) animation.getAnimatedValue("x_path");
                    mLaunchYRaw = (float) animation.getAnimatedValue("y_path");
                    updateMoveAndControlPoints(mLaunchXRaw, mLaunchYRaw);
                    invalidate();
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //修改动画执行标志
                    mIsBounceAnimStart = false;
                    resetToDefault();
                    mClickView.setVisibility(View.VISIBLE);
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
            mBurstRectF.offsetTo(rawX - mBurstRectFRadius, rawY - mBurstRectFRadius);
            //气泡改为消失状态
            mBubbleState = BUBBLE_STATE_BURSRT;
            mIsBurstAnimStart = true;
            //做一个int型属性动画，从0~mBurstDrawablesArray.length结束
            ValueAnimator anim = ValueAnimator.ofInt(0, mBitmapsExplodes.length - 1);
            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(300);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //设置当前绘制的爆炸图片index
                    mCurDrawableIndex = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //修改动画执行标志
                    mIsBurstAnimStart = false;
                    resetToDefault();
                }
            });
            anim.start();
        }

        private void resetToDefault() {
            clear();
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
            float[] k_h = Utils.getTwoPointLine(mMoveDrawPointCenter, mStickyPointCenterMid);
            float explodeRawX = rawX;
            float explodeRawY = rawY;
            switch (mLaunchDire) {
                case LAUNCH_TO_RIGHT:
                case LAUNCH_TO_LEFT:
                    //最终爆炸位置在左边和右边
                    explodeRawX = mLaunchDire == LAUNCH_TO_LEFT ? 0 : mScreenSize.x;
                    explodeRawY = Utils.getYFromLine(k_h[0], k_h[1], explodeRawX);
                    break;
                case LAUNCH_TO_BOTTOM:
                case LAUNCH_TO_TOP:
                    //最终爆炸位置在上边和下边
                    explodeRawY = mLaunchDire == LAUNCH_TO_TOP ? 0 : mScreenSize.y;
                    explodeRawX = Utils.getXFromLine(k_h[0], k_h[1], explodeRawY);
                    break;
                case LAUNCH_DIRE_NONE:
                    explodeRawX = rawX;
                    explodeRawY = rawY;
                    break;
            }
            if (isReadyToLaunch(rawX, rawY)) {
                launch(mMoveDrawPointCenter.x, mMoveDrawPointCenter.y, explodeRawX, explodeRawY);
            } else {
                bounce(rawX, rawY);
            }
        }

        public void clear() {
            if (mBitmapsExplodes != null && mBitmapsExplodes.length > 0) {
                for (int i = 0; i < mBitmapsExplodes.length; i++) {
                    mBitmapsExplodes[i].recycle();
                    mBitmapsExplodes[i] = null;
                }
                mBitmapsExplodes = null;
            }
            //way--1
//            removeViewForShowingBubble(mWm, this);//no way
            //way--2
            removeViewForShowingBubble(mContentContainer, this);
            mLayoutParams = null;
            mContentContainer = null;
        }

        private MaskFilter mFilter = null;

        public void setPaintMaskFilter(int mode) {
            switch (mode) {
                case MODE_NONE:
                    break;
                case MODE_GLOW:
                    mFilter = new BlurMaskFilter(10, BlurMaskFilter.Blur.OUTER);
                    break;
                case MODE_EMBOSS:
                    float[] direction = new float[]{10, 10, 10};
                    float ambient = 0.5f;
                    float specular = 1;
                    float blurRadius = 1;
                    mFilter = new EmbossMaskFilter(direction, ambient, specular, blurRadius);
                    break;
            }
            mPaint.setMaskFilter(mFilter);
        }
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
     * @param viewManager
     * @param view
     * @param layoutParams
     */
    private static void addViewForShowingBubble(ViewManager viewManager, View view, ViewGroup.LayoutParams layoutParams) {
        viewManager.addView(view, layoutParams);
    }

    private static void removeViewForShowingBubble(ViewManager viewManager, View view) {
        viewManager.removeView(view);
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

    final private DraggableListener getDragListener() {
        return mDragListener[0];
    }
}
