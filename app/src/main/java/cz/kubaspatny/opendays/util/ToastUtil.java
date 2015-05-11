package cz.kubaspatny.opendays.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import cz.kubaspatny.opendays.R;

/**
 * Utility class for working with Custom Toasts.
 */
public class ToastUtil {

    /**
     * Shows toast with red background.
     */
    public static void error(Context context, String message){
        showMessage(context, message, true);
    }

    /**
     * Shows toast with green background.
     */
    public static void success(Context context, String message){
        showMessage(context, message, false);
    }

    private static void showMessage(Context context, String message, boolean error){

        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);

        if(error){
            toast.getView().setBackgroundResource(R.drawable.toast_error_background);
        } else {
            toast.getView().setBackgroundResource(R.drawable.toast_success_background);
        }

        TextView text = (TextView) toast.getView().findViewById(android.R.id.message);
        text.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Body2);
        text.setTextColor(context.getResources().getColor(R.color.grey_100));
        text.setShadowLayer(0, 0, 0, 0);
        text.setGravity(Gravity.CENTER);
        toast.show();

    }

}
