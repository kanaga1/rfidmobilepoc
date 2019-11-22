package com.example.rfidapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;

import androidx.fragment.app.FragmentTabHost;
import androidx.legacy.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFBluetooth;

public class MainActivity extends AppCompatActivity  {

    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    private ListView mDrawerList;
    private String[] mOptionTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBar actionBar;
    private ScrollView scrollView;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private Button btn_connect, btn_search, btn_mac;
    public boolean scaning = false;
    public BluetoothDevice mDevice = null;
    private FragmentTabHost mTabHost;
    private FragmentManager fm;
    public String remoteBTName = "";
    public String remoteBTAdd = "";
    private TextView tvAddress;
    BTStatus btStatus = new BTStatus();

    private static final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST = 100;
    private static final int REQUEST_ACTION_LOCATION_SETTINGS = 3;
    public BluetoothAdapter mBtAdapter = null;
    private final static String TAG = "MainActivity111";
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private boolean mIsActiveDisconnect = true; // 是否主动断开连接
    private static final int RECONNECT_NUM = 3; // 重连次数
    private int mReConnectCount = RECONNECT_NUM; // 重新连接次数
    public RFIDWithUHFBluetooth uhf = RFIDWithUHFBluetooth.getInstance();
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isBluetoothEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }

        actionBar= getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#002663")));

        if(Build.VERSION.SDK_INT >= 21)
        {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));

        }
        mOptionTitles = getResources().getStringArray(R.array.options_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
//        scrollView =(ScrollView) findViewById(R.id.scrollviewid);

        //Initialize collapsed height for inventory list
        // initializeCollapsedHeight();

        // set a custom shadow that overlays the no_items content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new DrawerListAdapter(this, R.layout.drawer_list_item, DrawerListContent.ITEMS));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

//        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                int drawableRsourceId = getActionBarIcon();
                if (drawableRsourceId != -1)
                    getSupportActionBar().setIcon(drawableRsourceId);
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            private int getActionBarIcon() {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                if (fragment instanceof RapidReadFragment)
                    return R.drawable.dl_rr;
                else if (fragment instanceof InventoryFragment)
                    return R.drawable.dl_inv;
                else if (fragment instanceof LocationingFragment)
                    return R.drawable.dl_loc;
                else if (fragment instanceof RegisterFragment)
                    return R.drawable.register;

                else if (fragment instanceof ReadersListFragment)
                    return R.drawable.dl_rdl;

                else
                    return -1;
            }


            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                getSupportActionBar().setIcon(R.drawable.app_icon);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (savedInstanceState == null) {
            selectItem(0);
        }
        checkLocationEnable();

    }
    public static boolean isBluetoothEnabled() {
        System.out.println("222222222222222222222222");
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }
    private boolean shouldShowDisconnected() {
        System.out.println("333333333333333333333333");
        return mIsActiveDisconnect || mReConnectCount == 0;
    }


    class BTStatus implements RFIDWithUHFBluetooth.BTStatusCallback {
        @Override
        public void getStatus(final RFIDWithUHFBluetooth.StatusEnum statusEnum, final BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("1111111111111111111111111111111111111111");
                    remoteBTName = "";
                    remoteBTAdd = "";
                    if (statusEnum == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
                        SystemClock.sleep(500);
                        btn_connect.setText(MainActivity.this.getString(R.string.disConnect));
                        remoteBTName = device.getName();
                        remoteBTAdd = device.getAddress();
                        tvAddress.setText(remoteBTName + "(" + remoteBTAdd + ")" + "-connected");
                        if (shouldShowDisconnected()) {
                            Toast.makeText(MainActivity.this, getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
                        }
                        mIsActiveDisconnect = false;
                        mReConnectCount = RECONNECT_NUM;
                    } else if (statusEnum == RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED) {
                        btn_connect.setText(MainActivity.this.getString(R.string.Connect));
                        if (device != null) {
                            remoteBTName = device.getName();
                            remoteBTAdd = device.getAddress();
                            if (shouldShowDisconnected())
                                tvAddress.setText("(" + remoteBTAdd + ")" + "-not connected");
                        } else {
                            if (shouldShowDisconnected())
                                tvAddress.setText("-not connected");
                        }
                        if (shouldShowDisconnected())
                            Toast.makeText(MainActivity.this, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();

                        if (mDevice != null) {
                            reConnect(mDevice.getAddress()); // 重连
                        }
                    }

                    if (iConnectStatus != null) {
                        iConnectStatus.getStatus(statusEnum);
                    }
                }
            });
        }
    }
    public void connect(String deviceAddress) {
        System.out.println("dddddddddddddd"  +  uhf.getConnectStatus() .equals(RFIDWithUHFBluetooth.StatusEnum.CONNECTING) );
        if (uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTING) {

            Toast.makeText(this, getString(R.string.connecting), Toast.LENGTH_SHORT).show();
        } else {
            System.out.println("lllllllllllllllllllllllllpppppppppppppppppppp"+btStatus+deviceAddress);
            uhf.connect(deviceAddress, btStatus);

        }
    }
    IConnectStatus iConnectStatus = null;
    private void reConnect(String deviceAddress) {
        System.out.println("55555555555555555555555555555");

        if (!mIsActiveDisconnect && mReConnectCount > 0) {
            connect(deviceAddress);
            mReConnectCount--;
        }
    }
    public void setConnectStatusNotice(IConnectStatus iConnectStatus) {
        System.out.println("6666666666666666666666666666666");
        this.iConnectStatus = iConnectStatus;
    }

    public interface IConnectStatus {

        void getStatus(RFIDWithUHFBluetooth.StatusEnum statusEnum);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        if (drawerOpen) {
            //Hide the keyboard if it's showing when the drawer opens
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            if (getSupportActionBar() != null) {
                //Hide the tabs if they are visible
                getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        } else {
//            if (getSupportActionBar() != null) {
//                //If we are showing pre-filters or access options, show tabs
//                if (getSupportFragmentManager() != null && getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT) != null) {
//                    getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//                }
//            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void rrClicked(View view) {
        selectItem(1);
    }
    public void invClicked(View view) {
        selectItem(2);
    }
    public void locateClicked(View view) {
        selectItem(3);
    }
    public void RegisterClicked (View view) {
        selectItem(4);
    }
    public void expiryClicked(View view) {
        selectItem(5);
    }
    public void settClicked(View view) {
        selectItem(6);
    }


    private void selectItem(int position) {
        // update the no_items content by replacing fragments
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = HomeFragment.newInstance();
                break;

            case 1:
                fragment = RapidReadFragment.newInstance();
                break;

            case 2:
                fragment = InventoryFragment.newInstance();
                break;

            case 3:
                fragment = LocationingFragment.newInstance();
                break;
            case 4:
                fragment = RegisterFragment.newInstance();
                break;
            case 5:
                fragment = BarcodeFragment.newInstance();
                break;
            case 6:
                fragment = ReadersListFragment.newInstance();
                break;
//            case 7:
//                fragment = BeeperFragment.newInstance();
//                break;


        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (position == 0) {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Don't add the transaction to back stack since we are navigating to the first fragment
            //being displayed and adding the same to the backstack will result in redundancy
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        } else {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Add the transaction to the back stack since we want the state to be preserved in the back stack
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();
        }

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mOptionTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action buttons
        switch (item.getItemId()) {
//            case R.id.action_dpo:
//                Intent detailsIntent = new Intent(MainActivity.this, SettingsDetailActivity.class);
//                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                detailsIntent.putExtra(Constants.SETTING_ITEM_ID, 8);
//                startActivity(detailsIntent);
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position + 1);
        }
    }
    @Override
    public void onBackPressed() {
        //update the selected item in the drawer and the title
        mDrawerList.setItemChecked(0, true);
        setTitle(mOptionTitles[0]);
        //We are handling back pressed for saving pre-filters settings. Notify the appropriate fragment.
        //{@link BaseReceiverActivity # onBackPressed should be called by the fragment when the processing is done}
        //super.onBackPressed();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);

        super.onBackPressed();

    }

    //for Location

    private void checkLocationEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_REQUEST);
            }
        }
        if (!isLocationEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, REQUEST_ACTION_LOCATION_SETTINGS);
        }
    }
    private boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
}
