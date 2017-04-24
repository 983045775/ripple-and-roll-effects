package com.iflytek.eduvoiceassistant.ui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.iflytek.eduvoiceassistant.R;

import java.util.LinkedList;
import java.util.List;

/**
 * 滚动的自定义View
 * Created by lc on 2017/4/21.
 */
public class RollView extends View {
    private static final int SCROLL_TIME = 500;
    private static final String DEFAULT_TEXT = "暂无数据";
    private Context mContext;
    private Scroller mScroller;

    private List<String> mLrcLines = new LinkedList<>();


    private int mLrcHeight; // lrc界面的高度
    private int mRows;      // 多少行
    private int mCurrentLine = 3; // 当前行
    private int mOffsetY;   // y上的偏移
    private int mMaxScroll; // 最大滑动距离=一行歌词高度+歌词间距
    private int mCurrentXOffset;

    private float mDividerHeight; // 行间距

    private Rect mTextBounds;

    private Paint mNormalPaint; // 常规的字体
    private Paint mCurrentPaint; // 当前歌词的大小

    private Bitmap mBackground;
    private int mDivider = 2;//上下间隔

    public RollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        mContext = context;
        mScroller = new Scroller(context, new LinearInterpolator());
    }

    public void move(int x) {
        mScroller.abortAnimation();
        mScroller.startScroll(x, 0, 0, mMaxScroll, SCROLL_TIME);
        computeScroll();
    }

    public void moveNow() {
        new Thread() {
            @Override
            public void run() {
                for (int x = mRows - mDivider; x < 100 + mLrcLines.size(); x++) {
                    if (x == mLrcLines.size() - 3) {
                        x = mRows - mDivider;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final int finalX = x;
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            move(finalX);
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.getCurrY();
            if (mScroller.isFinished()) {
                int cur = mScroller.getCurrX() + mDivider;
                mCurrentLine = cur <= 1 ? 0 : cur - 1;
                mOffsetY = 0;
            }

            postInvalidate();
        }
    }

    public void setData(List<String> data) {
        for (int x = 0; x < 10; x++) {
            mLrcLines.addAll(data);
        }
    }

    // 初始化操作
    private void init(AttributeSet attrs) {
        // <begin>
        // 解析自定义属性
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.Lrc);
        float textSize = ta.getDimension(R.styleable.Lrc_textSize, 10.0f);
        float currenttextSize = ta.getDimension(R.styleable.Lrc_currenttextSize, 10.0f);
        mRows = ta.getInteger(R.styleable.Lrc_rows, 0);
        mDividerHeight = ta.getDimension(R.styleable.Lrc_dividerHeight, 0.0f);

        int normalTextColor = ta.getColor(R.styleable.Lrc_normalTextColor, 0xffffffff);
        int currentTextColor = ta.getColor(R.styleable.Lrc_currentTextColor, 0xff00ffde);
        ta.recycle();
        // </end>

        if (mRows != 0) {
            // 计算lrc面板的高度
            mLrcHeight = (int) ((textSize + mDividerHeight) * mRows + currenttextSize + mDividerHeight);
        }

        mNormalPaint = new Paint();
        mCurrentPaint = new Paint();

        // 初始化paint
        mNormalPaint.setTextSize(textSize);
        mNormalPaint.setColor(normalTextColor);
        mNormalPaint.setAntiAlias(true);
        mCurrentPaint.setTextSize(currenttextSize);
        mCurrentPaint.setColor(currentTextColor);
        mCurrentPaint.setAntiAlias(true);

        mTextBounds = new Rect();
        mCurrentPaint.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), mTextBounds);
        computeMaxScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 如果没有设置固定行数， 则默认测量高度，并根据高度计算行数
        if (mRows == 0) {
            int width = getPaddingLeft() + getPaddingRight();
            int height = getPaddingTop() + getPaddingBottom();
            width = Math.max(width, getSuggestedMinimumWidth());
            height = Math.max(height, getSuggestedMinimumHeight());

            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec
                    .AT_MOST);
            setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                    resolveSizeAndState(height, heightMeasureSpec, 0));

            mLrcHeight = getMeasuredHeight();
            computeRows();
            return;
        }

        // 设置了固定行数，重新设置view的高度
        int measuredHeightSpec = MeasureSpec.makeMeasureSpec(mLrcHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, measuredHeightSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBackground != null) {
            mBackground = Bitmap.createScaledBitmap(mBackground, getMeasuredWidth(), mLrcHeight,
                    true);
        }
    }

    /**
     * 根据高度计算行数
     */
    private void computeRows() {
        float lineHeight = mTextBounds.height() + mDividerHeight;
        mRows = (int) (getMeasuredHeight() / lineHeight);
    }

    /**
     * 计算滚动距离
     */
    private void computeMaxScroll() {
        mMaxScroll = (int) (mTextBounds.height() + mDividerHeight);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();

        if (mBackground != null) {
            canvas.drawBitmap(mBackground, new Matrix(), null);
        }

        float centerY = (getMeasuredHeight() + mTextBounds.height()) / 2;
        if (mLrcLines.isEmpty()) {
            canvas.drawText(DEFAULT_TEXT,
                    (width - mCurrentPaint.measureText(DEFAULT_TEXT)) / 2,
                    centerY, mCurrentPaint);
            return;
        }

        float offsetY = mTextBounds.height() + mDividerHeight;
        drawCurrentLine(canvas, width, centerY - mOffsetY);

        int firstLine = mCurrentLine - mRows / 2;
        firstLine = firstLine <= 0 ? 0 : firstLine;
        int lastLine = mCurrentLine + mRows / 2 + 2;
        lastLine = lastLine >= mLrcLines.size() - 1 ? mLrcLines.size() - 1 : lastLine;

        // 画当前行上面的
        for (int i = mCurrentLine - 1, j = 1; i >= firstLine; i--, j++) {
            String lrc = mLrcLines.get(i);
            float x = (width - mNormalPaint.measureText(lrc)) / 2;
            canvas.drawText(lrc, x, centerY - j * offsetY - mOffsetY, mNormalPaint);
        }

        // 画当前行下面的
        for (int i = mCurrentLine + 1, j = 1; i <= lastLine; i++, j++) {
            String lrc = mLrcLines.get(i);
            float x = (width - mNormalPaint.measureText(lrc)) / 2;
            canvas.drawText(lrc, x, centerY + j * offsetY - mOffsetY, mNormalPaint);
        }
    }


    private void drawCurrentLine(Canvas canvas, int width, float y) {
        String currentLrc = mLrcLines.get(mCurrentLine);
        float contentWidth = mCurrentPaint.measureText(currentLrc);
        if (contentWidth > width) {
            canvas.drawText(currentLrc, mCurrentXOffset, y, mCurrentPaint);
            if (contentWidth - Math.abs(mCurrentXOffset) < width) {
                mCurrentXOffset = 0;
            } else {
            }
        } else {
            float currentX = (width - mCurrentPaint.measureText(currentLrc)) / 2;
            // 画当前行
            canvas.drawText(currentLrc, currentX, y, mCurrentPaint);
        }
    }

    /**
     * 设置背景图片
     *
     * @param bmp
     */
    public void setBackground(Bitmap bmp) {
        mBackground = bmp;
    }
}
