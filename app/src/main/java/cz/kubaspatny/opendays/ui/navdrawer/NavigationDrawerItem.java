package cz.kubaspatny.opendays.ui.navdrawer;

import android.graphics.drawable.Drawable;

/**
 * Created by Kuba on 14/3/2015.
 */
public class NavigationDrawerItem {

    private String mText;
    private Drawable mDrawable;

    public NavigationDrawerItem(String text, Drawable drawable) {
        mText = text;
        mDrawable = drawable;
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

}
