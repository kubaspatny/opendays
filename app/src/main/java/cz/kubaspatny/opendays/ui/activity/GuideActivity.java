package cz.kubaspatny.opendays.ui.activity;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.database.DataContract;
import cz.kubaspatny.opendays.ui.fragment.ManagedStationsListFragment;
import cz.kubaspatny.opendays.ui.fragment.RouteGuideFragment;
import cz.kubaspatny.opendays.ui.fragment.RouteInfoFragment;
import cz.kubaspatny.opendays.ui.layout.SlidingTabLayout;
import cz.kubaspatny.opendays.util.ColorUtil;

public class GuideActivity extends BaseActivity {

    private final static String TAG = GuideActivity.class.getSimpleName();

    private Toolbar mToolbar;

    ViewPager mViewPager = null;
    RouteViewPagerAdapter mViewPagerAdapter = null;
    SlidingTabLayout mSlidingTabLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        String routeColorString = getIntent().getStringExtra(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_COLOR);
        int routeColor = Color.parseColor(routeColorString);
        int routeColorDark = ColorUtil.darken(routeColor, 0.1f);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setBackgroundColor(routeColor);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO: How to update the title as well..
        String title = getIntent().getStringExtra(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_NAME);
        String routeId = getIntent().getStringExtra(DataContract.GuidedGroups.COLUMN_NAME_ROUTE_ID);
        String groupId = getIntent().getStringExtra(DataContract.GuidedGroups.COLUMN_NAME_GROUP_ID);
        boolean viewOnly = getIntent().getBooleanExtra(RouteGuideFragment.ARG_VIEW_ONLY, false);
        int groupStartingPosition = getIntent().getIntExtra(DataContract.GuidedGroups.COLUMN_NAME_GROUP_STARTING_POSITION, 1);

        if(title != null && !TextUtils.isEmpty(title)) getSupportActionBar().setTitle(title);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPagerAdapter = new RouteViewPagerAdapter(getSupportFragmentManager(), routeId, groupId, groupStartingPosition, viewOnly);
        mViewPager.setAdapter(mViewPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setBackgroundColor(routeColor);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(Color.WHITE);
        mSlidingTabLayout.setDistributeEvenly(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mSlidingTabLayout.setElevation(dpToPx(R.dimen.toolbar_elevation));

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(routeColorDark);
        }

        mSlidingTabLayout.setViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_guide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RouteViewPagerAdapter extends FragmentPagerAdapter {

        private String mRouteId;
        private String mGroupId;
        private int mGroupStartingPosition;
        private boolean mViewOnly;

        public RouteViewPagerAdapter(FragmentManager fm, String routeId, String groupId, int groupStartingPosition, boolean viewOnly) {
            super(fm);
            mRouteId = routeId;
            mGroupId = groupId;
            mGroupStartingPosition = groupStartingPosition;
            mViewOnly = viewOnly;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment;

            switch (position){
                case 0:
                    fragment = RouteInfoFragment.newInstance(mRouteId);
                    break;
                case 1:

                    if(mViewOnly){
                        fragment = RouteGuideFragment.newInstance(mRouteId);
                    } else {
                        fragment = RouteGuideFragment.newInstance(mRouteId, mGroupId, mGroupStartingPosition);
                    }

                    break;
                default:
                    fragment = null; // TODO: Error fragment
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title;

            switch(position){
                case 0: title = "INFO";
                    break;
                case 1: title = "ROUTE";
                    break;
                default: title = "ERROR";
            }

            return title;
        }
    }

    public int dpToPx(int dp) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp , getResources() .getDisplayMetrics());
    }

}
