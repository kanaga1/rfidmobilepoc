package com.example.rfidapp;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidapp.Retrofit.RetrofitInterface;
import com.rscja.deviceapi.RFIDWithUHFBluetooth;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private boolean loopFlag = false;
    // private Button btInventory; "single"
    private Button InventoryLoop,  btStop;//
    private Button btInventoryPerMinute;
    // private Button btClear;
    public EditText EPCID ;
    public String item;
    private boolean isExit = false;
    private long total = 0;
    private MainActivity mContext;
    private SimpleAdapter adapter;
    private HashMap<String, String> map;
    private ArrayList<HashMap<String, String>> tagList;
    private String TAG = "DeviceAPI_UHFReadTag";
    private Retrofit retrofit;
    private ImageView tagread;

    boolean isRuning = false;
    public String EPCID1="", P_name1 = "", P_type1 = "",P_ID1="";
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://192.168.0.101:3000";
    private long mStrTime;
    public RegisterFragment() {
        // Required empty public constructor
    }
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        System.out.println("2222222222222222222222222222");

        Log.i(TAG, "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        init();
    }
    private void setConnectStatusNotice() {
        System.out.println("66666666666666666666666666666"+mContext);

//        if(mContext != null)
//            mContext.setConnectStatusNotice(new ConnectStatus());
    }

    private void init() {
        System.out.println("777777777777777777777777");

        isExit = false;
        setConnectStatusNotice();

        tagread = (ImageView) mContext.findViewById(R.id.tagread);


           tagread.setOnClickListener(this);

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
    public void startThread(boolean isStop) {
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        if (isRuning) {
            return;
        }
        isRuning = true;
        new TagThread(isStop).start();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String item1 = parent.getItemAtPosition(position).toString();
        if(position>0)
        {
            item = item1;
        }
        // Showing selected spinner item

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
                if (mContext.uhf.startInventoryTag()) {
                    loopFlag = true;
                    mContext.scaning = true;
                    mStrTime = System.currentTimeMillis();
                }

                isRuning = false;//执行完成设置成false


            }
        }
    }
    private void stopInventory() {
        cancelInventoryTask();

    }
    private void clearData() {
        total = 0;

//        adapter.notifyDataSetChanged();
//        inventoryPerMinute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


    }

        private void inventory() {
        String uii = mContext.uhf.inventorySingleTag();

            if (uii != null) {
            UHFTAGInfo uhftagInfo = new UHFTAGInfo();
            uhftagInfo.setEPC(mContext.uhf.convertUiiToEPC(uii));
            EPCID.setText(uhftagInfo.getEPC());
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmen
              View v =  inflater.inflate(R.layout.fragment_register, container, false);
        Button signupBtn = v.findViewById(R.id.tagregister);
        EPCID = v.findViewById(R.id.EPCID);
        Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add("Unit");
        categories.add("Kg");
        categories.add("mg");
        categories.add("Nos");
        categories.add("L");
        categories.add("ml");
        categories.add("sq.ft");
        categories.add("Ton");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categories){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {

                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        EPCID.setHint("Click the above to read tag");
        EPCID.setKeyListener(null);
        final EditText P_name = v.findViewById(R.id.P_name);
        final EditText P_type = v.findViewById(R.id.P_type);
        final EditText P_ID = v.findViewById(R.id.P_ID);
        ImageView readtag = v.findViewById(R.id.tagread);
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, String> map = new HashMap<>();

                map.put("EPCID", EPCID.getText().toString());
                EPCID1 = EPCID.getText().toString();

                map.put("P_ID", P_ID.getText().toString());
                P_ID1 = P_ID.getText().toString();

                map.put("P_name", P_name.getText().toString());
                P_name1 = P_name.getText().toString();

                map.put("P_type", P_type.getText().toString());
                P_type1 = P_type.getText().toString();

                map.put("unit",item);

                if (EPCID1.equals("")||P_name1.equals("")||P_type1.equals("")||P_ID1.equals(""))
                {
                    Toast.makeText(getContext(),
                            "Please enter all fields..", Toast.LENGTH_SHORT).show();

                }
                else {
                    Call<Void> call = retrofitInterface.executeSignup(map);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                            if (response.code() == 200) {
                                Toast.makeText(getContext(),
                                        "Tag registered successfully", Toast.LENGTH_SHORT).show();

                            } else if (response.code() == 400) {
                                Toast.makeText(getContext(),
                                        "Already registered", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

        return v;
    }




    private Timer mTimer = new Timer();
    private TimerTask mInventoryPerMinuteTask;
    private long period = 6 * 1000; // 每隔多少ms
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BluetoothReader" + File.separator;
    private String fileName;

    private void cancelInventoryTask() {
        if(mInventoryPerMinuteTask != null) {
            mInventoryPerMinuteTask.cancel();
            mInventoryPerMinuteTask = null;
        }
    }


    @Override
    public void onClick(View v) {
                   inventory();
    }
}
