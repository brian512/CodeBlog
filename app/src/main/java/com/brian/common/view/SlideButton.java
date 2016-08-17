
package com.brian.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SlideButton extends View {

    private Paint paint = new Paint();
    
    /**
     * false 为关闭
     */
    private boolean mCurrState = false; // 开关当前的状态, 默认为: 关闭
    private int mCurrentX; // x轴的偏移量
    
    private boolean mIsDragable = false; // 是否可拖拽
    
    private boolean mIsSliding = false; // 是否正在滑动
    
    private OnStateChangedListener mListener;
    
    private String text;
    
    private float density; // 屏幕密度
    
    private RectF mRectBg; // 按钮背景框
    private RectF mRectSlide; // 滑块框
    
    private int mColorOn; // 打开的颜色
    private int mColorOff; // 关闭的颜色
    
    private float widthSlide; // 滑块的宽度
    private int width; // 控件的宽度
    private int height; //控件的高低，滑块的高度一样
    
    private float radio; // 控件的圆角半径

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;

        text = "开关";
        
        mColorOn = Color.parseColor("#fe2a66");
        mColorOff = Color.parseColor("#a6a6a6");
        
        widthSlide = dip2px(30);
        width = dip2px(44);
        height = dip2px(24);
        radio = height/2;
        
        mRectBg = new RectF(0, 0, width, height);
        mRectSlide = new RectF(0, 0, widthSlide, height);
    }

    /**
     * 当测量当前控件的宽高时回调
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 设置开关的宽和高
        setMeasuredDimension(width+2, height+2);
    }

    /**
     * 绘制当前控件的方法
     */
    @Override
    protected void onDraw(Canvas canvas) {
        float left = mCurrentX - (widthSlide / 2); // 滑块左偏移
        if (left < 0) { // 处理参数
            left = 0;
        } else if (left > width - widthSlide) {
            left = width - widthSlide;
        }
        float percent = left/(width-widthSlide);
        
        paint.setAntiAlias(true);

        // 把滑动开关的背景画到画布上
        paint.setStyle(Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(mRectBg, radio, radio, paint);
        // 绘制渐变边框
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2f);
        drawRoundRect(canvas, mRectBg, percent); // 绘制渐变

        // 计算滑块位置
        float textSize = height/2f;
        int textX = (int) ((widthSlide-textSize*2)/2f);
        int textY = (int) ((height+textSize-dip2px(2))/2f);
        paint.setTextSize(textSize);
        paint.setStyle(Style.FILL);
        
        // 绘制滑块
        paint.setStyle(Style.FILL);
        mRectSlide.left = left;
        mRectSlide.right = left + widthSlide;
        drawRoundRect(canvas, mRectSlide, percent); // 绘制渐变
        
        // 绘制问题
        paint.setColor(Color.WHITE);
        canvas.drawText(text, left + textX, textY, paint);
        
        if (mIsSliding) { // 是否自动滑动
            setClickable(false);
            postDelayed(mAutoSlideTask, 10);
        } else {
            setClickable(true);
        }
        
        super.onDraw(canvas);
    }
    
    private Runnable mAutoSlideTask = new Runnable() {
        
        @Override
        public void run() {
            if (mCurrState) {
                mCurrentX = mCurrentX + 3;
                if (mCurrentX > width) {
                    mCurrentX = (int) width;
                    mIsSliding = false;
                }
            } else {
                mCurrentX = mCurrentX - 3;
                if (mCurrentX < 0) {
                    mCurrentX = 0;
                    mIsSliding = false;
                }
            }
            invalidate();
        }
    };
    
    /**
     * 绘制渐变圆角矩形
     * @param canvas
     * @param rectF
     * @param percent
     */
    private void drawRoundRect(Canvas canvas, RectF rectF, float percent) {
        int alpha = paint.getAlpha();
        paint.setColor(mColorOn);
        paint.setAlpha((int)(255*percent));
        canvas.drawRoundRect(rectF, radio, radio, paint);
        paint.setColor(mColorOff);
        paint.setAlpha(255 - (int)(255*percent));
        canvas.drawRoundRect(rectF, radio, radio, paint);
        paint.setAlpha(alpha);// 还原
    }

    /**
     * 当产生触摸时间时回调此方法
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsSliding) {
            return true;
        }
        if (!mIsDragable) { // 不可拖拽时，点击就切换状态
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mCurrState = !mCurrState;
                if (mListener != null) {
                    mListener.onToggleStateChanged(mCurrState);
                }
                mIsSliding = true;
                invalidate();
            }
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下
                mCurrentX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                mCurrentX = (int) event.getX();
                break;
            case MotionEvent.ACTION_UP: // 抬起

                // 判断当前滑动块, 偏向于哪一边, 如果滑动块的中心点小于背景的中心点, 设置为关闭状态
                boolean state = mCurrentX > width / 2;; // 改变后的状态

                // 调用用户的监听事件
                if (state != mCurrState && mListener != null) {
                    mListener.onToggleStateChanged(state);
                }

                mCurrState = state;
                mIsSliding = true;
                
                break;
            default:
                break;
        }
        if (mCurrentX < 0) {
            mCurrentX = 0;
        } else if (mCurrentX > width) {
            mCurrentX = (int) width;
        }

        invalidate(); // 刷新当前控件, 会引起onDraw方法的调用
        return true;
    }

    /**
     * 设置开关的状态
     * 
     * @param b
     */
    public void setToggleState(boolean state) {
        mCurrState = state;
    }
    
    /**
     * 设置是否可以拖拽，不可拖拽时点击就切换状态
     * @param dragable
     */
    public void setDragable(boolean dragable) {
        mIsDragable = dragable;
    }

    /**
     * 设置开关状态改变的监听事件
     * 
     * @param listener
     */
    public void setOnStateChangedListener(OnStateChangedListener listener) {
        this.mListener = listener;
    }
    
    /**
     * 获取当前是否打开
     * @return
     */
    public boolean isSwitchOn() {
        return mCurrState;
    }
    
    private int dip2px(float dpValue) {
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * @author andong 开关状态改变的监听事件
     */
    public interface OnStateChangedListener {

        /**
         * 当开关的状态改变时回调此方法
         * 
         * @param state 当前开关最新的状态
         */
        void onToggleStateChanged(boolean state);
    }
}
