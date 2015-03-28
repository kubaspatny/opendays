package cz.kubaspatny.opendays.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.database.DbHelper;
import cz.kubaspatny.opendays.sync.SyncHelper;
import cz.kubaspatny.opendays.ui.navdrawer.NavigationDrawerAdapter;
import cz.kubaspatny.opendays.ui.navdrawer.NavigationDrawerCallbacks;
import cz.kubaspatny.opendays.ui.navdrawer.NavigationDrawerItem;
import cz.kubaspatny.opendays.ui.activity.BaseActivity;
import cz.kubaspatny.opendays.util.AccountUtil;

import static cz.kubaspatny.opendays.util.ToastUtil.*;

/**
 * Created by Kuba on 14/3/2015.
 */
public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallbacks {
    private static final String PREF_USER_LEARNED_DRAWER =  "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION =   "navigation_drawer_position";
    private static final String PREFERENCES_FILE = BaseActivity.class.getSimpleName();

    private NavigationDrawerCallbacks mCallbacks;
    private ListView mDrawerList;
    private ListView mDrawerListBottom;
    private View mFragmentContainerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;

    public static void saveSharedSetting(Context context, String settingName, String settingValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context context, String settingName, String defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerList = (ListView) view.findViewById(R.id.drawerList);
        mDrawerListBottom = (ListView) view.findViewById(R.id.drawerListBottom);

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(getActivity(), getNavigationDrawerItems());
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        NavigationDrawerAdapter adapterBottom = new NavigationDrawerAdapter(getActivity(), getNavigationDrawerBottomItems());
        mDrawerListBottom.setAdapter(adapterBottom);

        mDrawerListBottom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new LogOutTask().execute();
            }
        });

        selectItem(mCurrentSelectedPosition);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    public void setActionBarDrawerToggle(ActionBarDrawerToggle actionBarDrawerToggle) {
        mActionBarDrawerToggle = actionBarDrawerToggle;
    }

    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    saveSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "true");
                }
                getActivity().invalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState)
            mDrawerLayout.openDrawer(mFragmentContainerView);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public NavigationDrawerItem[] getNavigationDrawerItems() {
        NavigationDrawerItem[] items = new NavigationDrawerItem[2];
        items[0] = new NavigationDrawerItem("Guided tours", getResources().getDrawable(R.drawable.ic_map_grey));
        items[1] = new NavigationDrawerItem("Managed stations", getResources().getDrawable(R.drawable.ic_location_history_grey));
        return items;
    }

    public NavigationDrawerItem[] getNavigationDrawerBottomItems() {
        NavigationDrawerItem[] items = new NavigationDrawerItem[1];
        items[0] = new NavigationDrawerItem("Log out", getResources().getDrawable(R.drawable.ic_person_grey));
        return items;
    }

    void selectItem(int position) {
        mCurrentSelectedPosition = position;

        if(mDrawerList != null){
            NavigationDrawerAdapter adapter = (NavigationDrawerAdapter)mDrawerList.getAdapter();
            adapter.select(position);
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        selectItem(position);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    private class LogOutTask extends AsyncTask<Void, Void, Void> {

        Exception e = null;
        ProgressDialog dialog;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SyncHelper.cancelSync(getActivity(), AccountUtil.getAccount(getActivity()));
                SyncHelper.disableSync(getActivity(), AccountUtil.getAccount(getActivity()));

                new DbHelper(getActivity()).clearUserData();
                ((BaseActivity)getActivity()).clearRegistrationId(getActivity());
                AccountUtil.removeAccount(getActivity());
            } catch (Exception e){
                this.e = e;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(getActivity(), "Logging out", "Clearing user data", true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            dialog.dismiss();

            if(e == null){
                success(getActivity(), "You have been successfully logged out!");
            } else {
                Log.e("NavigationDrawerFragment", "Error logging out!", e);
                error(getActivity(), "Logout attempt failed!");
            }
        }
    }

}

