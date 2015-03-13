package cz.kubaspatny.opendays.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.sync.SyncHelper;
import cz.kubaspatny.opendays.ui.activity.BaseActivity;
import cz.kubaspatny.opendays.adapter.GuidedGroupsAdapter;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.util.AccountUtil;

public class GroupListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    public final static String TAG = GroupListFragment.class.getSimpleName();

    private ListView listView;
    private SwipeRefreshLayout swipeContainer;
    private LinearLayout emptyView;
    private GuidedGroupsAdapter cursorAdapter;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int sync_code = intent.getIntExtra(AppConstants.KEY_SYNC_STATUS_CODE, -1);
            switch(sync_code){
                case AppConstants.SYNC_STATUS_CODE_START: // TODO: remove for PROD build
                    swipeContainer.setRefreshing(true);
                    return;
                case AppConstants.SYNC_STATUS_CODE_END:
                    swipeContainer.setRefreshing(false);
                    return;
                default:
                    return;
            }
        }
    };

    public static GroupListFragment newInstance() {
        GroupListFragment fragment = new GroupListFragment();
        return fragment;
    }

    public GroupListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_group_list, container, false);
        listView = (ListView) result.findViewById(R.id.guided_groups_list);
        swipeContainer = (SwipeRefreshLayout) result.findViewById(R.id.swipe_container);
        emptyView = (LinearLayout) result.findViewById(R.id.empty_state);
        listView.setEmptyView(emptyView);
        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setColorSchemeResources(
                R.color.blue_600,
                R.color.red_600,
                R.color.green_600);
        swipeContainer.setEnabled(true);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeContainer.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        getActivity().getSupportLoaderManager().initLoader(0, null, this);
        cursorAdapter = new GuidedGroupsAdapter(getActivity(), null, 0);
        listView.setAdapter(cursorAdapter);
        return result;

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(AppConstants.KEY_SYNC_STATUS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onRefresh() {
        //TODO: request sync
        SyncHelper.requestManualSync(AccountUtil.getAccount(getActivity()));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {DataContract.GuidedGroups._ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), DbContentProvider.CONTENT_URI.buildUpon().path(DataContract.GuidedGroups.TABLE_NAME).build(), projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "swapping cursor");
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getSupportLoaderManager().destroyLoader(0);
    }

}
