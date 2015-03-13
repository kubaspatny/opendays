package cz.kubaspatny.opendays.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import cz.kubaspatny.opendays.R;

/**
 * Created by Kuba on 8/3/2015.
 */
public class CircleView extends View {

    private Paint mCirclePaint;

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int min = Math.min(width, height);

        canvas.drawCircle(width / 2f, height / 2f, min * 0.5f - 8, mCirclePaint);


    }

    /**
     * Initialize the control. This code is in a separate method so that it can be
     * called from both constructors.
     */
    private void init() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(getResources().getColor(R.color.grey_400));
    }

    public void setCircleColor(String color){
        mCirclePaint.setColor(Color.parseColor(color));
        invalidate();
    }

}
