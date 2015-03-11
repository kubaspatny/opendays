package cz.kubaspatny.opendays.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.List;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.activity.BaseActivity;
import cz.kubaspatny.opendays.adapter.GuidedGroupsAdapter;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.database.DbContentProvider;
import cz.kubaspatny.opendays.domainobject.GroupDto;
import cz.kubaspatny.opendays.net.ConnectionUtils;
import cz.kubaspatny.opendays.oauth.AuthServer;

public class GroupListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    private ListView listView;
    private SwipeRefreshLayout swipeContainer;
    private LinearLayout emptyView;
    private GuidedGroupsAdapter cursorAdapter;

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
    public void onRefresh() {

        if(ConnectionUtils.isConnected(getActivity())){
            new LoadGroupsAsyncTask().execute();
        } else {
            swipeContainer.setRefreshing(false);
            Toast.makeText(getActivity(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {DataContract.GuidedGroups._ID,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP,
                DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), DbContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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

    private class LoadGroupsAsyncTask extends AsyncTask<Void, Void, Void> {

        public final String DEBUG_TAG = LoadGroupsAsyncTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                List<GroupDto> groups = AuthServer.getGroups(getActivity(), ((BaseActivity)getActivity()).getAccountManager());
                if(!groups.isEmpty()) getActivity().getContentResolver().delete(DbContentProvider.CONTENT_URI, null, null); // delete previous groups

                for(GroupDto g : groups){

                    ContentValues values = new ContentValues();
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID, g.getId());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION, g.getStartingPosition());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ACTIVE, g.isActive());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID, g.getRoute().getId());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME, g.getRoute().getName());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR, g.getRoute().getHexColor());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_INFORMATION, g.getRoute().getInformation());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_TIMESTAMP, g.getRoute().getDate().toInstant().toString());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_EVENT_ID, g.getRoute().getEvent().getId());
                    values.put(DataContract.GuidedGroups.COLUMN_NAME_EVENT_NAME, g.getRoute().getEvent().getName());
                    getActivity().getContentResolver().insert(DbContentProvider.CONTENT_URI, values);
                }

                return null;

            } catch(MalformedURLException e){
                Log.e(DEBUG_TAG, e.getLocalizedMessage());
            } catch(Exception e){
                String message = (e.getLocalizedMessage() == null) ? "Error downloading groups." : e.getLocalizedMessage();
                Log.e(DEBUG_TAG, message);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void notUsed) {
            swipeContainer.setRefreshing(false);
        }
    }

}
