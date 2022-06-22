package com.example.xxfile.magicindicator;

import android.content.Context;
import android.graphics.Typeface;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

public class TabPagerTitleView extends SimplePagerTitleView {

    public TabPagerTitleView(Context context) {
        super(context);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        setSelected(true);
        setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        setTextColor(getSelectedColor());
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        setSelected(false);
        setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        setTextColor(getNormalColor());
    }
}
