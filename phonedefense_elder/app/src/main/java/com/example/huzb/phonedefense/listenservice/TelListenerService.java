package com.example.huzb.phonedefense.listenservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.huzb.phonedefense.Activity.PhonedefenseDB;
import com.example.huzb.phonedefense.Activity.ScrollingActivity;
import com.example.huzb.phonedefense.R;
import com.example.huzb.phonedefense.Tools.AudioRecordFunc;
import com.example.huzb.phonedefense.Tools.ConnectWeb;
import com.example.huzb.phonedefense.Util.Constant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.huzb.phonedefense.Util.Constant.elderid;

import com.android.internal.telephony.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by 34494 on 2017/1/19.
 */

public class TelListenerService extends Service {
    // 电话管理器
    private TelephonyManager telephonyManager;
    // 监听器对象
    private MyListener listener;
    //声明录音机
    AudioRecordFunc audiorecorder;
    //声明音频存储路径
    public static final String storeDirectory=Environment.getExternalStorageDirectory()+"/ISvideo";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 服务创建的时候调用的方法
     */
    @Override
    public void onCreate() {
        // 后台监听电话的呼叫状态。
        // 得到电话管理器
        telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        listener = new MyListener();
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        File videoDir=new File(storeDirectory);
        if(!videoDir.exists())
        {
            videoDir.mkdir();
        }
        super.onCreate();
    }

    private class MyListener extends PhoneStateListener {
        // 当电话的呼叫状态发生变化的时候调用的方法
        String inComingNumber;
        String storepath=null;
        Call call;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (incomingNumber != null&&(!isfilter()||getContactNameFromPhoneBook(incomingNumber)==null)) {
                try {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE://空闲状态。
                            Log.i("Telstate", "电话挂断");
                            Constant.iscalling = false;
                            if (call != null) {
                                call.cancel();
                            }
                            new NetworkTask().execute(elderid, "stop");
                            if (audiorecorder != null) {
                                //8.停止捕获
                                audiorecorder.stop();
                                //9.释放资源
                                //mediaRecorder.release();
                                audiorecorder = null;
                                //TODO 这个地方你可以将录制完毕的音频文件上传到服务器，这样就可以监听了
                                Log.i("TelProtectService", "音频文件录制完毕");
                            }
                            break;
                        case TelephonyManager.CALL_STATE_RINGING://零响状态。
                            inComingNumber = incomingNumber;
                            Log.i("CALL_STATE_RINGING", incomingNumber);
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK://通话状态
                            //4.指定录音文件的名称
                            Constant.iscalling = true;
                            new NetworkTask().execute(elderid, "calling"+incomingNumber);
                            call = polling(Constant.elderid);
                            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));//当前时间
                            if (inComingNumber != null) {
                                Log.i("TelListenerService", "来电电话：" + inComingNumber);
                                storepath = storeDirectory + "/" + inComingNumber + "-" + time;
                            }

                            audiorecorder = AudioRecordFunc.getInstance(TelListenerService.this, storepath);
                            audiorecorder.start();
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 服务销毁的时候调用的方法
     * 保护监听服务
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消电话的监听,采取线程守护的方法，当一个服务关闭后，开启另外一个服务，除非你很快把两个服务同时关闭才能完成
        Intent i = new Intent(this,TelProtectService.class);
        startService(i);
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;
        Log.i("telisten","服务停止");
    }
    /**
     * 改变通话状态时调用
     */
    class NetworkTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            ConnectWeb cw = new ConnectWeb();
            return cw.calling(params[0],params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
//    /**
//     * 通话时轮询
//     */
//    class PollingTask extends AsyncTask<String, Integer, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            ConnectWeb cw = new ConnectWeb();
//            return cw.polling(params[0]);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            System.out.println(result);
//            if(result.equals("stopcalling"))
//            {
//                rejectCall();
//            }
//            else if(Constant.iscalling)
//            {
//                new PollingTask().execute(Constant.elderid);
//            }
//        }
//    }
    public Call polling(String elderid) {
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();
        String TAG="polling";
        final Request.Builder reqBuilder = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.BASE_URL+"/elderpolling").newBuilder();
        urlBuilder.addQueryParameter("elderid",elderid);
        Request request = reqBuilder
                .tag(TAG)
                .get()
                .url(urlBuilder.build())
                .build();
        Call call =  mOkHttpClient.newCall(request);
        Log.i(TAG,Constant.BASE_URL);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultValue = response.body().string();
                    if(resultValue.equals("stopcalling"))
                    {
                        rejectCall();
                        addNotificaction();
                    }
                    else if(resultValue.equals("startlisten")) {
                        Constant.islistenning = true;
                        polling(Constant.elderid);
                    }
                    else if(resultValue.equals("stoplisten")){
                        Constant.islistenning = false;
                        polling(Constant.elderid);
                    }
                    else if(Constant.iscalling) {
                        polling(Constant.elderid);
                    }
                }
            }
        });
        return call;
    }
    public void rejectCall() {
        try {
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.endCall();
        } catch (NoSuchMethodException e) {
        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
        }
    }
    private void addNotificaction() {
        NotificationManager mNotifyMgr =
                (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, ScrollingActivity.class), 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.micon)
                .setContentTitle("提醒：您的通话已被亲友挂断")
                .setContentText("触摸可举报")
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();// getNotification()

        mNotifyMgr.notify(1, notification);
    }
    //用于选择是否过滤通讯录
    private boolean isfilter()
    {
        PhonedefenseDB db = new PhonedefenseDB(this);
        Cursor cursor;
        cursor = db.getReadableDatabase().query(PhonedefenseDB.ISFILTER_TABLE, new String[]{"isfilter"}, null, null, null, null, null);
        if(cursor.moveToFirst())
        {
            return cursor.getInt(0)>0;
        }
        return false;
    }
    //获取通讯录名字
    public String getContactNameFromPhoneBook(String phoneNum) {
        String contactName = null;
        ContentResolver cr = this.getContentResolver();
        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[] { phoneNum }, null);
        if (pCur.moveToFirst()) {
            contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            pCur.close();
        }
        return contactName;
    }
}
