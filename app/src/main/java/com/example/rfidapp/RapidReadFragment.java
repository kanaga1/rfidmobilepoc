package com.example.rfidapp;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidapp.DateUtils;
import com.example.rfidapp.FileUtils;
import com.example.rfidapp.MainActivity;
import com.example.rfidapp.NumberTool;
import com.example.rfidapp.R;
import com.example.rfidapp.Retrofit.RetrofitInterface;
import com.example.rfidapp.Utils;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RapidReadFragment  extends Fragment implements View.OnClickListener {
    private boolean loopFlag = false;
    public int count=0;
    // private Button btInventory; "single"
    private Button InventoryLoop,  btStop;//
    private Button btInventoryPerMinute;
    // private Button btClear;
    private TextView tv_count;
    private boolean isExit = false;
    private long total = 0;
    private MainActivity mContext;
    private SimpleAdapter adapter;
    private HashMap<String, String> map,map1;
    private ArrayList<HashMap<String, String>> tagList;
    private String TAG = "DeviceAPI_UHFReadTag";

    //--------------------------------------获取 解析数据-------------------------------------------------
    final int FLAG_START = 0;//开始
    final int FLAG_STOP = 1;//停止
    final int FLAG_UHFINFO = 3;
    final int FLAG_SUCCESS = 10;//成功
    final int FLAG_FAIL = 11;//失败

    boolean isRuning = false;
    private long mStrTime;
    List<User> heroList;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case FLAG_STOP:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        //停止成功
//                        btClear.setEnabled(true);
                        btStop.setEnabled(false);
                        InventoryLoop.setEnabled(true);
                        // btInventory.setEnabled(true); "single"
                        btInventoryPerMinute.setEnabled(true);
                    } else {
                        //停止失败
                        Utils.playSound(2);
                        Toast.makeText(mContext, "Inventory Fail", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case FLAG_START:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        //开始读取标签成功
//                        btClear.setEnabled(false);
                        btStop.setEnabled(true);
                        InventoryLoop.setEnabled(false);
                        //  btInventory.setEnabled(false); "single"
                        btInventoryPerMinute.setEnabled(false);
                    } else {
                        //开始读取标签失败
                        Utils.playSound(2);
                    }
                    break;
                case FLAG_UHFINFO:
                    UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                    addEPCToList(info, "N/A");
                     Utils.playSound(1);
                    break;
            }
        }
    };
    public RapidReadFragment() {
        // Required empty public constructor
    }
    public static RapidReadFragment newInstance() {
        return new RapidReadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        System.out.println("2222222222222222222222222222");
        Utils.initSound(getContext());
        Log.i(TAG, "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        RetrofitInterface apiService = ApiClient.getClient().create(RetrofitInterface.class);
        Call<List<User>> call = apiService.getUsers();
        mContext.uhf.setPower(30);
        System.out.println("dddddddddddddddddddddddd"+mContext.uhf.getPower());
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                heroList = response.body();


//                //Creating an String array for the ListView
//                String[] heroes = new String[heroList.size()];
//
//                //looping through all the heroes and inserting the names inside the string array
//                for (int i = 0; i < heroList.size(); i++) {
//                    heroes[i] = heroList.get(i).getName();
//                }


            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rr, container, false);
    }
    @Override
    public void onPause() {
        System.out.println("333333333333333333333333");

        super.onPause();
        stopInventory();
    }

    @Override
    public void onDestroyView() {
        System.out.println("4444444444444444444444444");

        super.onDestroyView();
        isExit = true;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.InventoryLoop:
                clearData();
                startThread(false);
                inventoryPerMinute();
                break;
//                break;
//            case R.id.InventoryLoop:

//            case R.id.btInventory:  "single"
//                inventory();
//                break;
            case R.id.btStop:
                if (mContext.uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
                    startThread(true);
                }
                break;
        }

    }
    private void init() {
        System.out.println("777777777777777777777777");

        isExit = false;
        setConnectStatusNotice();

        //    btInventory = (Button) mContext.findViewById(R.id.btInventory); "single"
        InventoryLoop = (Button) mContext.findViewById(R.id.InventoryLoop);
        btStop = (Button) mContext.findViewById(R.id.btStop);
        btStop.setEnabled(false);
//        btClear = (Button) mContext.findViewById(R.id.btClear);
        tv_count = (TextView) mContext.findViewById(R.id.tv_count);


        InventoryLoop.setOnClickListener(this);
        //   btInventory.setOnClickListener(this); "single"
//        btClear.setOnClickListener(this);
        btStop.setOnClickListener(this);

        btInventoryPerMinute = mContext.findViewById(R.id.InventoryLoop);
        btInventoryPerMinute.setOnClickListener(this);

        tagList = new ArrayList<>();
        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagData", "tagCount", "tagRssi"},
                new int[]{R.id.TvTagUii, R.id.TvTagCount,
                        R.id.TvTagRssi});

        mContext.uhf.setKeyEventCallback(new RFIDWithUHFBluetooth.KeyEventCallback() {
            @Override
            public void getKeyEvent(int keycode) {
                Log.d("DeviceAPI_ReadTAG", "  keycode =" + keycode + "   ,isExit=" + isExit);
                if (!isExit && mContext.uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
                    startThread(loopFlag);
                }
            }
        });
        clearData();
    }

    @Override
    public void onResume() {
        System.out.println("55555555555555555555555555555");

        super.onResume();
        clearData();
        if (mContext.uhf.getConnectStatus() == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {

            InventoryLoop.setEnabled(true);
            //    btInventory.setEnabled(true); "single"
            btInventoryPerMinute.setEnabled(true);
        } else {
            InventoryLoop.setEnabled(false);
            //  btInventory.setEnabled(false); "single"
            btInventoryPerMinute.setEnabled(false);
        }
        setConnectStatusNotice();
    }

    private void setConnectStatusNotice() {
        System.out.println("66666666666666666666666666666"+mContext);

//        if(mContext != null)
//            mContext.setConnectStatusNotice(new ConnectStatus());
    }

    private void clearData() {
        total = 0;
        tv_count.setText("0");

        tagList.clear();
        adapter.notifyDataSetChanged();
//        inventoryPerMinute();
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        cancelInventoryTask();
        loopFlag = false;
        if(mContext.scaning) {
            RFIDWithUHFBluetooth.StatusEnum statusEnum = mContext.uhf.getConnectStatus();
            Message msg = handler.obtainMessage(FLAG_STOP);
            boolean result = mContext.uhf.stopInventoryTag();
            if (result || statusEnum == RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED) {
                msg.arg1 = FLAG_SUCCESS;
            } else {
                msg.arg1 = FLAG_FAIL;
            }
            if (statusEnum == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
                //在连接的情况下，结束之后继续接收未接收完的数据
                //getUHFInfoEx();
            }
            mContext.scaning = false;
            handler.sendMessage(msg);
        }
    }
//
//    class ConnectStatus implements MainActivity.IConnectStatus {
//
//        @Override
//        public void getStatus(RFIDWithUHFBluetooth.StatusEnum statusEnum) {
//            System.out.println("999999999999999999999999");
//
//            if (statusEnum == RFIDWithUHFBluetooth.StatusEnum.CONNECTED) {
//                if (!loopFlag) {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    InventoryLoop.setEnabled(true);
//                    btInventory.setEnabled(true);
//                    btInventoryPerMinute.setEnabled(true);
//                }
//            } else if (statusEnum == RFIDWithUHFBluetooth.StatusEnum.DISCONNECTED) {
//                loopFlag = false;
//                mContext.scaning = false;
//                btClear.setEnabled(true);
//                btStop.setEnabled(false);
//                InventoryLoop.setEnabled(false);
//                btInventory.setEnabled(false);
//                btInventoryPerMinute.setEnabled(false);
//            }
//        }
//    }


    public void startThread(boolean isStop) {
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        if (isRuning) {
            return;
        }
        isRuning = true;
        new TagThread(isStop).start();
    }

    class TagThread extends Thread {

        boolean isStop = false;

        public TagThread(boolean isStop) {
            this.isStop = isStop;
        }

        public void run() {
            if (isStop) {
                stopInventory();
                isRuning = false;//执行完成设置成false
            } else {
                Message msg = handler.obtainMessage(FLAG_START);
                if (mContext.uhf.startInventoryTag()) {
                    loopFlag = true;
                    mContext.scaning = true;
                    mStrTime = System.currentTimeMillis();
                    msg.arg1 = FLAG_SUCCESS;
                } else {
                    msg.arg1 = FLAG_FAIL;
                }
                handler.sendMessage(msg);
                isRuning = false;//执行完成设置成false
                while (loopFlag) {
                    getUHFInfo();
                }
            }
        }
    }

    private boolean getUHFInfo() {
        ArrayList<UHFTAGInfo> list = mContext.uhf.readTagFromBuffer();
        if (list != null) {
            for (int k = 0; k < list.size(); k++) {
                UHFTAGInfo info = list.get(k);
                Message msg = handler.obtainMessage(FLAG_UHFINFO);
                msg.obj = info;
                handler.sendMessage(msg);
                if(!loopFlag) {
                    break;
                }
            }
            if (list.size() > 0)
                return true;
        } else {
            return false;
        }
        return false;
    }

    /**
     * 添加EPC到列表中
     *
     * @param uhftagInfo
     */
    private void addEPCToList(UHFTAGInfo uhftagInfo, String rssi) {
        if (!TextUtils.isEmpty(uhftagInfo.getEPC())) {
            int index = checkIsExist(uhftagInfo.getEPC());

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("EPC:");
            stringBuilder.append(uhftagInfo.getEPC());
            if (!TextUtils.isEmpty(uhftagInfo.getTid())) {
                stringBuilder.append("\r\nTID:");
                stringBuilder.append(uhftagInfo.getTid());
            }
            if (!TextUtils.isEmpty(uhftagInfo.getUser())) {
                stringBuilder.append("\r\nUSER:");
                stringBuilder.append(uhftagInfo.getUser());
            }

            map = new HashMap<String, String>();
            map.put("tagUii", uhftagInfo.getEPC());
            map.put("tagData", stringBuilder.toString());
            map.put("tagCount", String.valueOf(1));
            map.put("tagRssi", rssi);
            // mContext.getAppContext().uhfQueue.offer(epc + "\t 1");
            if (index == -1) {
                if(heroList.size()<1)
                {
                    Toast.makeText(getContext(),"No items are available",Toast.LENGTH_SHORT);
                }
                else
                {
                    for(int i=0;i<heroList.size();i++)
                    {
                        System.out.println(";;;;;;;;;;;;;;"+index+"[[[["+heroList.size()+"ll"+heroList.get(i).getEPCID()+"pp"+uhftagInfo.getEPC());

                        if(heroList.get(i).getEPCID().equals(uhftagInfo.getEPC()))
                        {

                            map1 = new HashMap<String, String>();
                            map1.put("tagUii", uhftagInfo.getEPC());
                            map1.put("tagData", heroList.get(i).getP_ID());
                            map1.put("tagCount", heroList.get(i).getP_name());
                            map1.put("tagRssi", heroList.get(i).getP_type()+heroList.get(i).getUnit());
                            tagList.add(map1);
                            tv_count.setText("" + adapter.getCount());


                        }
                    }
                }
            } else {
//                int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;
//                System.out.println("ffffffffffff"+ tagcount);
//                map.put("tagCount", String.valueOf(tagcount));
//                tagList.set(index, map);
            }

            float useTime = (System.currentTimeMillis() - mStrTime) / 1000.0F;
            adapter.notifyDataSetChanged();
        }
    }

    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (strEPC == null || strEPC.isEmpty()) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            tempStr = tagList.get(i).get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    private Timer mTimer = new Timer();
    private TimerTask mInventoryPerMinuteTask;
    private long period = 6 * 1000; // 每隔多少ms
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BluetoothReader" + File.separator;
    private String fileName;









    private void inventoryPerMinute() {
        System.out.println("llllllllllllllll");
        cancelInventoryTask();
        btInventoryPerMinute.setEnabled(false);
//        btInventory.setEnabled(false);  "single"
        InventoryLoop.setEnabled(false);
        btStop.setEnabled(true);
        mContext.scaning = true;
        fileName = path + "battery_" + DateUtils.getCurrFormatDate(DateUtils.DATEFORMAT_FULL) + ".txt";
        mInventoryPerMinuteTask = new TimerTask() {
            @Override
            public void run() {
                String data = DateUtils.getCurrFormatDate(DateUtils.DATEFORMAT_FULL) + "\t电量：" + mContext.uhf.getBattery() + "%\n";
//                FileUtils.writeFile(fileName, data, true);
                //  inventory();
            }
        };
        mTimer.schedule(mInventoryPerMinuteTask, 0, period);
    }

    private void cancelInventoryTask() {
        if(mInventoryPerMinuteTask != null) {
            mInventoryPerMinuteTask.cancel();
            mInventoryPerMinuteTask = null;
        }
    }

//    private void inventory() {
//        String uii = mContext.uhf.inventorySingleTag();
//        mStrTime = System.currentTimeMillis();
//        if (uii != null) {
//            UHFTAGInfo uhftagInfo = new UHFTAGInfo();
//            uhftagInfo.setEPC(mContext.uhf.convertUiiToEPC(uii));
//            Message msg = handler.obtainMessage(FLAG_UHFINFO);
//            msg.obj = uhftagInfo;
//            handler.sendMessage(msg);
//        }
//    }

    private void getUHFInfoEx() {
        String strResult = "";
        long begintime = System.currentTimeMillis();
        while (!isExit) {
            ArrayList<UHFTAGInfo> list = mContext.uhf.readTagFromBuffer();
            if (list != null) {
                for (int k = 0; k < list.size() && !isExit; k++) {
                    UHFTAGInfo info = list.get(k);
                    Message msg = handler.obtainMessage(FLAG_UHFINFO);
                    msg.obj = strResult + "EPC:" + info.getEPC() + "@N/A";
                    handler.sendMessage(msg);
                }
                if (list.size() == 0) {
                    if (System.currentTimeMillis() - begintime > 1000 * 1) {
                        return;
                    }
                }
            } else {
                if (System.currentTimeMillis() - begintime > 1000 * 1) {
                    return;
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.freeSound();
    }
}
