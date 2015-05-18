package cz.kubaspatny.opendays.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.ref.WeakReference;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.app.AppConstants;
import cz.kubaspatny.opendays.sync.DataFetcher;
import cz.kubaspatny.opendays.sync.SyncHelper;
import cz.kubaspatny.opendays.adapter.GuidedGroupsAdapter;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.ui.activity.RouteActivity;
import cz.kubaspatny.opendays.util.AccountUtil;
import cz.kubaspatny.opendays.util.ConnectionUtils;
import cz.kubaspatny.opendays.util.PrefsUtil;
import cz.kubaspatny.opendays.util.ToastUtil;

public class GroupListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    public final static String TAG = GroupListFragment.class.getSimpleName();

    private ListView listView;
    private View footerView;
    private View footerViewText;
    private View footerViewProgressBar;
    private SwipeRefreshLayout swipeContainer;
    private LinearLayout emptyView;
    private GuidedGroupsAdapter cursorAdapter;
    boolean mDestroyed = false;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int sync_code = intent.getIntExtra(AppConstants.KEY_SYNC_STATUS_CODE, -1);
            switch(sync_code){
                case AppConstants.SYNC_STATUS_CODE_START:
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
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_group_list, container, false);
        listView = (ListView) result.findViewById(R.id.guided_groups_list);
        swipeContainer = (SwipeRefreshLayout) result.findViewById(R.id.swipe_container);
        emptyView = (LinearLayout) result.findViewById(R.id.empty_state);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshList();
            }
        });

        listView.setEmptyView(emptyView);

        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setColorSchemeResources(
                R.color.blue_600,
                R.color.red_600,
                R.color.green_600);
        swipeContainer.setEnabled(true);

        footerView = inflater.inflate(R.layout.load_more_footer, null, false);
        footerViewText = footerView.findViewById(R.id.load_more_text);
        footerViewProgressBar = footerView.findViewById(R.id.load_more_progressBar);

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = cursorAdapter.getCursor();

                if(position >= cursor.getCount()){
                    if(ConnectionUtils.isConnected(getActivity())){

                        if(footerViewText.getVisibility() == View.VISIBLE){
                            new LoadMoreGroupsTask(GroupListFragment.this).execute();
                        }

                    } else {
                        ToastUtil.error(getActivity(), getString(R.string.no_internet));
                    }
                } else {
                    cursor.moveToPosition(position);
                    String routeName = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME));
                    String routeColor = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR));
                    String routeId = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID));
                    String groupId = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID));
                    int groupStartingPosition = cursor.getInt(cursor.getColumnIndexOrThrow(DataContract.GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION));

                    Log.d(TAG, "Clicked on: " + routeName);

                    Intent i = new Intent(getActivity(), RouteActivity.class);
                    i.putExtra(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME, routeName);
                    i.putExtra(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR, routeColor);
                    i.putExtra(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID, routeId);
                    i.putExtra(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID, groupId);
                    i.putExtra(DataContract.GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION, groupStartingPosition);
                    startActivity(i);
                }

            }

        });

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
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
    }

    @Override
    public void onRefresh() {
        refreshList();
    }

    private void refreshList(){
        if(ConnectionUtils.isConnected(getActivity())){
            SyncHelper.requestManualSync(getActivity(), AccountUtil.getAccount(getActivity()));
        } else {
            ToastUtil.error(getActivity(), getString(R.string.no_internet));
            swipeContainer.setRefreshing(false);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {DataContract.GuidedGroups._ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID,
                DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID,
                DataContract.GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), DbContentProvider.CONTENT_URI.buildUpon().path(DataContract.GuidedGroups.TABLE_NAME).build(), projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "swapping cursor");
        toggleListFooterVisibility();
        cursorAdapter.swapCursor(data);
    }

    /**
     * Shows/Hides "load more" button.
     */
    private void toggleListFooterVisibility(){
        if(PrefsUtil.getCachedGroupsCount(getActivity()) < PrefsUtil.getRemoteGroupsCount(getActivity())){
            Log.d(TAG, "Displaying load more.");
            if(listView.getFooterViewsCount() == 0) listView.addFooterView(footerView, null, true);
        } else {
            Log.d(TAG, "Hiding load more.");
            if(listView.getFooterViewsCount() != 0) listView.removeFooterView(footerView);
        }
    }

    /**
     * Shows/Hides footer "loading" animation.
     * @param loading
     */
    private void toggleListFooterState(boolean loading){
        if(loading){
            footerViewText.setVisibility(View.GONE);
            footerViewProgressBar.setVisibility(View.VISIBLE);
        } else {
            footerViewText.setVisibility(View.VISIBLE);
            footerViewProgressBar.setVisibility(View.GONE);
        }
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

    boolean hasBeenDestroyed() {
        return mDestroyed;
    }

    private class LoadMoreGroupsTask extends AsyncTask<Void, Void, Void> {

        Exception e = null;
        WeakReference<GroupListFragment> weakRefToParent;

        private LoadMoreGroupsTask(GroupListFragment fragment) {
            this.weakRefToParent = new WeakReference<GroupListFragment>(fragment);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                int page = PrefsUtil.getCachedGroupsCount(getActivity()) / AppConstants.PAGE_SIZE;
                new DataFetcher(getActivity()).loadGuidedGroups(AccountUtil.getAccount(getActivity()), page, AppConstants.PAGE_SIZE);
            } catch (Exception e){
                this.e = e;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            toggleListFooterState(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(e != null){
                Log.e(TAG, "Error loading more groups!", e);
            }

            GroupListFragment parent = weakRefToParent.get();
            if(parent != null && !parent.hasBeenDestroyed()){
                toggleListFooterState(false);
                toggleListFooterVisibility();
            }

        }
    }

}
