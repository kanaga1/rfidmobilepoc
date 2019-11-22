package com.example.rfidapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;

import androidx.appcompat.app.AppCompatActivity;

public class DeviceListActivity extends BaseActivity {
    // private BluetoothAdapter mBtAdapter;
    private TextView mEmptyList;
    public static final String TAG = "DeviceListActivity";

    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private ServiceConnection onService = null;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private Handler mHandler = new Handler();
    private boolean mScanning;

    RFIDWithUHFBluetooth uhf = RFIDWithUHFBluetooth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);
        System.out.println("hhhhhhhhhhhhhhhhhhhh");
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
//--------------------------------------------------------------
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        scanLeDevice(true);
        //--------------------------------------------------------------
        mEmptyList = (TextView) findViewById(R.id.empty);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning == false)
                    scanLeDevice(true);
                else
                    finish();
            }
        });
    }

    private void scanLeDevice(final boolean enable) {
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    uhf.stopScanBTDevices();
                    cancelButton.setText(R.string.scan);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            uhf.scanBTDevices(new RFIDWithUHFBluetooth.ScanBTCallback() {
                @Override
                public void getDevices(final BluetoothDevice bluetoothDevice, final int rssi, byte[] bytes) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Successful scan");
                            addDevice(bluetoothDevice, rssi);
                            System.out.println("8555555555888888888"+rssi);
                        }
                    });
                }
            });
            cancelButton.setText(R.string.cancel);
        } else {
            mScanning = false;
            uhf.stopScanBTDevices();
            cancelButton.setText(R.string.scan);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        uhf.stopScanBTDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uhf.stopScanBTDevices();
    }

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;
        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                System.out.println(listDev.getAddress()+"sssssssss"+device.getAddress()+"ll"+rssi);
                deviceFound = true;
                break;
            }
        }
        devRssiValues.put(device.getAddress(), rssi);
        System.out.println("ikikkkkkkkkkkkkkk"+rssi);
        if (!deviceFound) {
            deviceList.add(device);
            mEmptyList.setVisibility(View.GONE);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            uhf.stopScanBTDevices();

            ///---------------------------------------
            String addr = device.getAddress();
            String name = device.getName();
            List<String[]> list = FileUtils.readxml();
            boolean isflag = true;
            for (int k = 0; k < list.size(); k++) {
                if (addr.equals(list.get(k)[0])) {
                    isflag = false;
                    break;
                }
            }
            if (isflag) {
                if (name == null) name = "";
                String[] strArra = new String[]{addr, name};
                list.add(strArra);
                FileUtils.savexml(list);
            }
            //---------------------------------------

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            System.out.println("llllllllllllllllllll"+b+"lllllllllllll"+Activity.RESULT_OK+"oooooooooo"+result);
            finish();

        }
    };

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "scanLeDevice==============>");
        scanLeDevice(false);
    }

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.getName());
                tvpaired.setText(R.string.paired);
                tvpaired.setTextColor(Color.RED);
                tvpaired.setVisibility(View.VISIBLE);
                tvrssi.setVisibility(View.VISIBLE);
            } else {
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
            }
            return vg;
        }
    }
}

