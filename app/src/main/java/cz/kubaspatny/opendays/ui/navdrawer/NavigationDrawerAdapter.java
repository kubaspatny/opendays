package cz.kubaspatny.opendays.ui.navdrawer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cz.kubaspatny.opendays.R;

/**
 * Created by Kuba on 14/3/2015.
 */
public class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerItem> {

    private Activity context;
    private NavigationDrawerItem[] items;

    class ViewHolder {
        public TextView text;
    }

    public NavigationDrawerAdapter(Activity context, NavigationDrawerItem[] items) {
        super(context, R.layout.drawer_row, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.drawer_row, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.item_name);
            convertView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        NavigationDrawerItem item = items[position];
        holder.text.setText(item.getText());
        holder.text.setCompoundDrawablesWithIntrinsicBounds(item.getDrawable(), null, null, null);

        return convertView;

    }

}
