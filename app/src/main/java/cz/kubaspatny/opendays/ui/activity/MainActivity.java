package cz.kubaspatny.opendays.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import cz.kubaspatny.opendays.R;
import cz.kubaspatny.opendays.sync.SyncHelper;
import cz.kubaspatny.opendays.ui.navdrawer.NavigationDrawerCallbacks;
import cz.kubaspatny.opendays.ui.fragment.GroupListFragment;
import cz.kubaspatny.opendays.ui.fragment.ManagedStationsListFragment;
import cz.kubaspatny.opendays.ui.fragment.NavigationDrawerFragment;
import cz.kubaspatny.opendays.util.AccountUtil;

public class MainActivity extends BaseActivity implements NavigationDrawerCallbacks {

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        if(SyncHelper.isLargeSyncTime(this)) SyncHelper.requestManualSync(this, AccountUtil.getAccount(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_guided_groups, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        Fragment fragment;

        switch (position){
            case 0:
                fragment = GroupListFragment.newInstance();
                break;
            case 1:
                fragment = ManagedStationsListFragment.newInstance();
                break;
            default:
                fragment = null; // TODO: Error fragment
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


}
