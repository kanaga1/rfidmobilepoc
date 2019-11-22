package com.example.rfidapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTabHost;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFBluetooth;

import static com.rscja.deviceapi.RFIDWithUHFBluetooth.deviceAddress;

public class ReadersListFragment extends Fragment implements View.OnClickListener  {
    private boolean loopFlag = false;
    private Button btn_connect, btn_search, btn_mac;
    public boolean scaning = false;
    public BluetoothDevice mDevice = null;
    public String remoteBTName = "";
    private FragmentTabHost mTabHost;
    public String remoteBTAdd = "";
    private TextView tvAddress;
    private FragmentManager fm;

    private MainActivity mContext;
    BTStatus btStatus = new BTStatus();
    public BluetoothAdapter mBtAdapter = null;
    private final static String TAG = "MainActivity111";
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private boolean mIsActiveDisconnect = true; // 是否主动断开连接
    private static final int RECONNECT_NUM = 3; // 重连次数
    private int mReConnectCount = RECONNECT_NUM; // 重新连接次数
    public RFIDWithUHFBluetooth uhf = RFIDWithUHFBluetooth.getInstance();

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public ReadersListFragment() {
        // Required empty public constructor
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                System.out.println("rrrrrrrrrrrrr"+mDevice);

                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    System.out.println(BluetoothDevice.EXTRA_DEVICE+"o0000o"+resultCode+"ppsss"+Activity.RESULT_OK);
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    System.out.println("oooooooooooooooooooooo"+mDevice);
                    tvAddress.setText(mDevice.getName() + "(" + deviceAddress + ")");

                    connect(deviceAddress);

                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                }
                break;


            default:

                break;
        }
    }

    protected void initUI() {

        fm = getActivity().getSupportFragmentManager();

        mTabHost.setup(getContext(), fm, R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_inventory)).setIndicator(getString(R.string.title_inventory)),
                InventoryFragment.class, null);
    }
    public static ReadersListFragment newInstance() {
        return new ReadersListFragment();
    }
    private void setConnectStatusNotice() {
        System.out.println("66666666666666666666666666666");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        System.out.println("22222222222222");


    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();

        if (mContext.uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {

            btn_connect.setText("Disconnect");
            mIsActiveDisconnect = false;
            tvAddress.setText(SharedPreference.getDeviceName(getContext())+" - Connected");
            mReConnectCount = RECONNECT_NUM;
        } else if (mContext.uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED) {
            uhf.init(getContext());

            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        } else {
            uhf.init(getContext());
//            Intent newIntent = new Intent(getContext(), DeviceListActivity.class);
//            startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("11111111111111111");
         View v = inflater.inflate(R.layout.fragment_readers_list, container, false);

        tvAddress = (TextView) v.findViewById(R.id.tvAddress);
        btn_connect = (Button) v.findViewById(R.id.btn_connect);
        btn_search = (Button) v.findViewById(R.id.btn_search);
        mTabHost =  v.findViewById(android.R.id.tabhost);
        btn_connect.setOnClickListener(this);
        btn_search.setOnClickListener(this);
         return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_connect:
                if (btn_connect.getText().equals(this.getString(R.string.Connect))) {
                    Toast.makeText(getContext(),"Please search the device",Toast.LENGTH_SHORT).show();
//                    scanBluetoothDevice();
                } else {
                    mIsActiveDisconnect = true; // 主动断开为true
                    Toast.makeText(getContext(), "The Device has disconnected", Toast.LENGTH_SHORT).show();
                    btn_connect.setText("Connect");

                    tvAddress.setText(SharedPreference.getDeviceName(getContext())+" - not Connected");


                    uhf.disconnect();
                }
                break;
            case R.id.btn_search:
                if (scaning) {

                    Toast.makeText(getContext(), getString(R.string.title_stop_read_card), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mContext.uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {


                }
                else
                {
                    setConnectStatusNotice();

                    uhf.init(getContext());
                    mBtAdapter = BluetoothAdapter.getDefaultAdapter();

                    scanBluetoothDevice();
                }
                break;
        }
    }
    public void connect(String deviceAddress) {
        if (uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTING) {

            Toast.makeText(getContext(), getString(R.string.connecting), Toast.LENGTH_SHORT).show();
        } else {
            System.out.println("lllllllllllllllllllllllllpppppppppppppppppppp"+btStatus+deviceAddress);
            uhf.connect(deviceAddress, btStatus);

        }
    }
    private boolean shouldShowDisconnected() {
        return mIsActiveDisconnect || mReConnectCount == 0;
    }

    class BTStatus implements RFIDWithUHFBluetooth.BTStatusCallback {
        @Override
        public void getStatus(final RFIDWithUHFBluetooth.StatusEnum statusEnum, final BluetoothDevice device) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    remoteBTName = "";
                    remoteBTAdd = "";

                    if (statusEnum == RFIDWithUHFBluetooth.StatusEnum.CONNECTED ) {
                        SystemClock.sleep(500);
                        btn_connect.setText(getActivity().getString(R.string.disConnect));
                        remoteBTName = device.getName();
                        System.out.println("bbbbbbbbttttttname "+ remoteBTName);
                        remoteBTAdd = device.getAddress();
                        System.out.println("bbbbbbbbttttttname "+ remoteBTAdd);
                        tvAddress.setText(remoteBTName + "(" + remoteBTAdd + ")" + "-connected");

                        SharedPreference.setDeviceName(getContext(),remoteBTName + "(" + remoteBTAdd + ")" );
                        System.out.println("bbbbbbbbttttttname "+ tvAddress.getText());
                        if (shouldShowDisconnected()) {
                            System.out.println("disssssssssss ");

                            Toast.makeText(getContext(), getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
                        }
                        mIsActiveDisconnect = false;
                        mReConnectCount = RECONNECT_NUM;
                    } else if (statusEnum == RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED) {
                        btn_connect.setText(getContext().getString(R.string.Connect));
                        SharedPreference.cleardevicename(getContext());

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
                            Toast.makeText(getContext(), getString(R.string.disconnect), Toast.LENGTH_SHORT).show();

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
        private void reConnect(String deviceAddress) {
            if (!mIsActiveDisconnect && mReConnectCount > 0) {
                connect(deviceAddress);
                mReConnectCount--;
            }
        }

    }
    IConnectStatus iConnectStatus = null;



    public interface IConnectStatus {
        void getStatus(RFIDWithUHFBluetooth.StatusEnum statusEnum);
    }

    private void scanBluetoothDevice() {
        if (mBtAdapter == null) {

            Toast.makeText(getContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mBtAdapter.isEnabled()) {

            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
//            if (btn_connect.getText().equals(this.getString(R.string.disConnect))) {
//                uhf.disconnect();
//            }
            Intent newIntent = new Intent(getContext(), DeviceListActivity.class);
            startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        }
    }

}
