package com.example.xxfile.magicindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.blankj.utilcode.util.ConvertUtils;

import net.lucode.hackware.magicindicator.FragmentContainerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.model.PositionData;

import java.util.List;

/**
 * 参照WrapPagerIndicator修改后的定制版（用于主界面顶部TAB）
 */

public class WrapPagerIndicator2 extends View implements IPagerIndicator {
    private int mFillColor;
    private float mRoundRadius;
    private Interpolator mStartInterpolator = new LinearInterpolator();
    private Interpolator mEndInterpolator = new LinearInterpolator();

    private List<PositionData> mPositionDataList;
    private Paint mPaint;

    private RectF mRect = new RectF();
    private boolean mRoundRadiusSet;

    public WrapPagerIndicator2(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mFillColor);
        canvas.drawRoundRect(mRect, mRoundRadius, mRoundRadius, mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { //若展示滑动过程，需要设置Tab字体大小不变，否则indicator块会因字体大小变化而跳动
//        if (mPositionDataList == null || mPositionDataList.isEmpty()) {
//            return;
//        }
//
//        // 计算锚点位置
//        PositionData current = FragmentContainerHelper.getImitativePositionData(mPositionDataList, position);
//        PositionData next = FragmentContainerHelper.getImitativePositionData(mPositionDataList, position + 1);
//
//        mRect.left = current.mContentLeft + (current.contentWidth() / 2.0f) + (next.mContentLeft - current.mContentLeft) * mEndInterpolator.getInterpolation(positionOffset);
//        mRect.top = current.mContentTop - ConvertUtils.dp2px(3);
//        mRect.right = current.mContentRight + ConvertUtils.dp2px(6) + (next.mContentRight - current.mContentRight) * mStartInterpolator.getInterpolation(positionOffset);
//        mRect.bottom = current.mContentBottom - ConvertUtils.dp2px(2);
//
//        if (!mRoundRadiusSet) {
//            mRoundRadius = mRect.height() / 3; //设置圆弧半径
//        }
//
//        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        if (mPositionDataList == null || mPositionDataList.isEmpty()) {
            return;
        }

        // 计算锚点位置
        PositionData current = FragmentContainerHelper.getImitativePositionData(mPositionDataList, position);

        mRect.left = current.mContentLeft + (current.contentWidth() / 2.0f);
        mRect.top = current.mContentTop - ConvertUtils.dp2px(3);
        mRect.right = current.mContentRight + ConvertUtils.dp2px(8);
        mRect.bottom = current.mContentBottom - ConvertUtils.dp2px(1);

        if (!mRoundRadiusSet) {
            mRoundRadius = mRect.height() / 3; //设置圆弧半径
        }

        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPositionDataProvide(List<PositionData> dataList) {
        mPositionDataList = dataList;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(int fillColor) {
        mFillColor = fillColor;
    }

    public float getRoundRadius() {
        return mRoundRadius;
    }

    public void setRoundRadius(float roundRadius) {
        mRoundRadius = roundRadius;
        mRoundRadiusSet = true;
    }

    public Interpolator getStartInterpolator() {
        return mStartInterpolator;
    }

    public void setStartInterpolator(Interpolator startInterpolator) {
        mStartInterpolator = startInterpolator;
        if (mStartInterpolator == null) {
            mStartInterpolator = new LinearInterpolator();
        }
    }

    public Interpolator getEndInterpolator() {
        return mEndInterpolator;
    }

    public void setEndInterpolator(Interpolator endInterpolator) {
        mEndInterpolator = endInterpolator;
        if (mEndInterpolator == null) {
            mEndInterpolator = new LinearInterpolator();
        }
    }
}
