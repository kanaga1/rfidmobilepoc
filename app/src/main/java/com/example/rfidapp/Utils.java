package com.example.rfidapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.rscja.deviceapi.DeviceConfiguration;
import com.rscja.utility.StringUtility;

import java.util.HashMap;



import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by Administrator on 2019-3-13.
 */

public class Utils {
    private static final String TAG = "Utils";

    private static HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private static SoundPool soundPool;
    private static float volumnRatio;
    private static AudioManager am;

    public static void initSound(Context context){

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(context, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(context, R.raw.serror, 1));
        am = (AudioManager) context.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象

    }
    public static void freeSound(){
        System.out.println("lllllllllllllllllllllll"+soundPool);
        if(soundPool!=null)
            soundPool.release();
        soundPool=null;
    }
    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public static void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {

            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0无不循环，-1无永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void alert(Activity act, int titleInt, String message, int iconInt) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(titleInt);
            builder.setMessage(message);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean vailHexInput(String str) {

        if (str == null || str.length() == 0) {
            return false;
        }
        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
    }

    /**
     * 判断是否横屏
     * @return
     */
    public static boolean isLandscape() {
        String model = DeviceConfiguration.getModel();
        Log.e(TAG, "model=" + model);
        return model.contains("P80")
                || model.contains("A8")
                || model.equals("CWJ600");
    }

}
