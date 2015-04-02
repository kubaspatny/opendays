package cz.kubaspatny.opendays.ui.navdrawer;

import android.graphics.drawable.Drawable;

/**
 * Created by Kuba on 14/3/2015.
 */
public class NavigationDrawerItem {

    private String mText;
    private Drawable mDrawable;
    private Drawable mDrawableSelected;

    public NavigationDrawerItem(String text, Drawable drawable, Drawable drawableSelected) {
        mText = text;
        mDrawable = drawable;
        mDrawableSelected = drawableSelected;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public Drawable getDrawableSelected() {
        return mDrawableSelected;
    }

    public void setDrawableSelected(Drawable mDrawableSelected) {
        this.mDrawableSelected = mDrawableSelected;
    }
}
