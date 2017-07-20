package com.example.huzhebin.phonedefense_family;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PollingService extends Service {
    public PollingService() {
    }
    Call call;

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
        super.onCreate();
        Constant.ispolling = true;
        call = polling(Constant.familyid);
        Log.i("polling","服务开始");
    }
    /**
     * 服务销毁的时候调用的方法
     * 保护轮询服务
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Constant.ispolling = false;
        call.cancel();
        Intent i = new Intent(this,ProtectPollingService.class);
        startService(i);
        Log.i("polling","服务停止");
    }

    public Call polling(String familyid) {
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build();
        String TAG="polling";
        final Request.Builder reqBuilder = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.BASE_URL+"/familypolling").newBuilder();
        urlBuilder.addQueryParameter("familyid",familyid);
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
                    if(resultValue.startsWith("calling"))
                    {
                        int grade=0;
                        PhonedefenseDB db = new PhonedefenseDB(PollingService.this);
                        Cursor cursor;
                        cursor = db.getReadableDatabase().query(PhonedefenseDB.PROTECTGRADE, new String[]{"grade"}, null, null, null, null, null);
                        if(cursor.moveToFirst())
                        {
                            grade = cursor.getInt(0);
                        }
                        if(grade==2) {
                            addNotificaction("提醒：亲情帐号正在进行通话", "触摸可监听或挂断");
                            Message msg = new Message();
                            msg.obj = "正在与" + resultValue.replaceFirst("calling", "") + "通话...";
                            Constant.status = (String) msg.obj;
                            MainActivity.serverhandler.sendMessage(msg);
                        }
                    }
                    else if(resultValue.startsWith("stop"))
                    {
                        Message msg = new Message();
                        msg.obj = "空闲状态";
                        Constant.status = (String)msg.obj;
                        MainActivity.serverhandler.sendMessage(msg);
                    }
                    else if(resultValue.equals("dangercalling"))
                    {
                        int grade=0;
                        PhonedefenseDB db = new PhonedefenseDB(PollingService.this);
                        Cursor cursor;
                        cursor = db.getReadableDatabase().query(PhonedefenseDB.PROTECTGRADE, new String[]{"grade"}, null, null, null, null, null);
                        if(cursor.moveToFirst())
                        {
                            grade = cursor.getInt(0);
                        }
                        if(grade < 2) {
                            addNotificaction("提醒：亲情帐号正在进行疑似诈骗通话", "触摸可监听或挂断");
                            Message msg = new Message();
                            msg.obj = "正在进行疑似诈骗通话...";
                            Constant.status = (String) msg.obj;
                            MainActivity.serverhandler.sendMessage(msg);
                        }
                    }
                    else if(resultValue.startsWith("get_"))
                    {
                        downAsynFile(resultValue.split("_",2)[1]);
                    }
                    if(Constant.ispolling) {
                        polling(Constant.familyid);
                    }
                }
            }
        });
        return call;
    }
    private void downAsynFile(final String filname) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        String url = Constant.BASE_URL+"/download/"+filname;
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    File dir = new File(Constant.storeDirectory);
                    if(!dir.exists())
                    {
                        dir.mkdir();
                    }
                    fileOutputStream = new FileOutputStream(new File(Constant.storeDirectory+"/"+filname));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.i("wangshu", "IOException");
                    e.printStackTrace();
                }

                addNotificaction("提醒：收到一个电话录音","触摸可查看详情");
            }
        });
    }
    private void addNotificaction(String title,String text) {
        NotificationManager mNotifyMgr =
                (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, new Intent(this,MainActivity.class), 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.micon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();// getNotification()

        mNotifyMgr.notify(1, notification);
    }
}
