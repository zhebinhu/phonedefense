package com.example.huzhebin.phonedefense_family;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huzhebin.phonedefense_family.zxing.encoding.EncodingUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    ImageView imageView;
    RelativeLayout bindlayout;
    LinearLayout protectlayout;
    LinearLayout recorderlayout;
    TextView title;
    static TextView status;
    static ImageButton serverButton;
    static ImageButton stopcallingButton;
    static ImageButton listenButton;
    ListView lv;
    Toolbar toolbar;
    Spinner sp;
    TextView info;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if(listenButton.isSelected()) {
                listenButton.setSelected(false);
                Toast.makeText(MainActivity.this, "对方不在通话中", Toast.LENGTH_SHORT).show();
            }
        }
    };
    static Handler serverhandler = new Handler() {
        public void handleMessage(Message msg) {
            if(Constant.ispolling) {
                status.setText((String) msg.obj);
                if(Constant.grade>0 && serverButton.isSelected()&&!((String)msg.obj).equals("空闲状态"))
                {
                    listenButton.setEnabled(true);
                    stopcallingButton.setEnabled(true);
                }
                if(((String)msg.obj).equals("空闲状态"))
                {
                    listenButton.setSelected(false);
                    listenButton.setEnabled(false);
                    stopcallingButton.setSelected(false);
                    stopcallingButton.setEnabled(false);
                }

            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_recorder:
                    getrecordview();
                    return true;
                case R.id.navigation_protection:
                    getprotectview();
                    return true;
                case R.id.navigation_bind:
                    getbindview();
                    return true;
            }
            return false;
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_main);
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        Constant.familyid = tm.getDeviceId();
        title = (TextView)findViewById(R.id.title_main);
        serverButton = (ImageButton)findViewById(R.id.server_button);
        stopcallingButton = (ImageButton)findViewById(R.id.stopcalling_button);
        listenButton = (ImageButton) findViewById(R.id.listen_button);
        bindlayout = (RelativeLayout) findViewById(R.id.bind_layout);
        protectlayout = (LinearLayout) findViewById(R.id.protect_layout);
        recorderlayout = (LinearLayout) findViewById(R.id.recorder_layout);
        imageView = (ImageView)findViewById(R.id.family_binding_imageview);
        sp = (Spinner) findViewById(R.id.protect_spinner);
        lv = (ListView)findViewById(R.id.calllistview);
        info = (TextView) findViewById(R.id.protect_info);
        status = (TextView) findViewById(R.id.calling_status);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
//        toolbar = (Toolbar)findViewById(R.id.toolbar);
//        toolbar.inflateMenu(R.menu.protect);
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
//
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                return false;
//            }
//        });
        if(!new File(Constant.storeDirectory).exists())
        {
            new File(Constant.storeDirectory).mkdir();
        }
        getrecordview();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isWorked("com.example.huzhebin.phonedefense_family.PollingService")) {
                    Intent startIntent = new Intent(MainActivity.this, PollingService.class);
                    startService(startIntent);
                    Toast.makeText(MainActivity.this, "服务启动!", Toast.LENGTH_SHORT).show();
                    serverButton.setSelected(true);
                    status.setText(getResources().getString(R.string.status_serving));
                }
                else
                {
                    Intent stopIntent = new Intent(MainActivity.this, PollingService.class);
                    stopService(stopIntent);
                    Log.i("mainthread","监听进程关闭");

                    TimerTask task = new TimerTask() {
                        public void run() {
                            if (isWorked("com.example.huzhebin.phonedefense_family.PollingService")) {
                                Intent stopIntent = new Intent(MainActivity.this, PollingService.class);
                                stopService(stopIntent);
                                Log.i("mainthread", "监听进程关闭");
                            }
                            if (isWorked("com.example.huzhebin.phonedefense_family.ProtectPollingService")) {
                                Intent stopIntent = new Intent(MainActivity.this, ProtectPollingService.class);
                                stopService(stopIntent);
                                Log.i("mainthread", "守护进程关闭");
                            }
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(task, 1000);
                    Toast.makeText(MainActivity.this, "服务关闭", Toast.LENGTH_SHORT).show();
                    serverButton.setSelected(false);
                    listenButton.setSelected(false);
                    listenButton.setEnabled(false);
                    stopcallingButton.setSelected(false);
                    stopcallingButton.setEnabled(false);
                    status.setText(getResources().getText(R.string.status_stopserve));
                }
            }
        });
        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listenButton.isSelected())
                {
                    listenButton.setSelected(false);
                    Toast.makeText(MainActivity.this, "监听结束", Toast.LENGTH_SHORT).show();
                    Constant.islistenning = false;
                    new StopListenTask().execute(Constant.familyid);
                }
                else
                {
                    listenButton.setSelected(true);
                    Toast.makeText(MainActivity.this, "监听开始", Toast.LENGTH_SHORT).show();
                    Constant.islistenning = true;
                    new Thread(new Client()).start();
                    new ListenTask().execute(Constant.familyid);
                }
            }
        });
        stopcallingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NetworkTask().execute(Constant.familyid);
            }
        });
    }

    void getbindview()
    {
        bindlayout.setVisibility(View.VISIBLE);//这一句即隐藏布局LinearLayout区域
        protectlayout.setVisibility(View.GONE);
        recorderlayout.setVisibility(View.GONE);
        imageView.setImageBitmap(EncodingUtils.createQRCode(Constant.familyid,800,800,null));
        title.setText(R.string.title_familybind);
    }
    void getprotectview()
    {
        String grades[] = {"低", "中", "高"};
        final String infos[] = {getResources().getString(R.string.low_info),getResources().getString(R.string.middle_ingo),getResources().getString(R.string.high_ingo)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, grades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setVisibility(View.VISIBLE);
        int grade=0;
        PhonedefenseDB db = new PhonedefenseDB(MainActivity.this);
        Cursor cursor;
        String sql;
        cursor = db.getReadableDatabase().query(PhonedefenseDB.PROTECTGRADE, new String[]{"grade"}, null, null, null, null, null);
        if(cursor.moveToFirst())
        {
            grade = cursor.getInt(0);
        }
        Constant.grade = grade;
        sp.setSelection(grade);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                TextView tv = (TextView)arg1;
                tv.setTextSize(20);
                info.setText(infos[arg2]);
                tv.setTextColor(getResources().getColor(R.color.colorRedDark));
                TextPaint tp = tv.getPaint();
                tp.setFakeBoldText(true);
                PhonedefenseDB db = new PhonedefenseDB(MainActivity.this);
                Cursor cursor;
                String sql;
                cursor = db.getReadableDatabase().query(PhonedefenseDB.PROTECTGRADE, new String[]{"grade"}, null, null, null, null, null);
                if(cursor.moveToFirst())
                {
                    sql="update "+PhonedefenseDB.PROTECTGRADE+" set grade='"+arg2+"' where Id=0";
                }
                else
                {
                    sql="insert into "+PhonedefenseDB.PROTECTGRADE+"(Id,grade) values(0,'"+arg2+"')";
                }
                db.getReadableDatabase().execSQL(sql);
                Constant.grade = arg2;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        if(!isWorked("com.example.huzhebin.phonedefense_family.PollingService"))
        {
            serverButton.setSelected(false);
            listenButton.setEnabled(false);
            stopcallingButton.setEnabled(false);
            status.setText(getResources().getString(R.string.status_stopserve));
        }
        else
        {
            serverButton.setSelected(true);
            if(status.getText().toString().startsWith("正在")&&Constant.grade!=0)
            {
                listenButton.setEnabled(true);
                stopcallingButton.setEnabled(true);
            }
            else {
                listenButton.setEnabled(false);
                stopcallingButton.setEnabled(false);
            }
            status.setText(Constant.status);
        }
        protectlayout.setVisibility(View.VISIBLE);//这一句即隐藏布局LinearLayout区域
        bindlayout.setVisibility(View.GONE);
        recorderlayout.setVisibility(View.GONE);
        title.setText(R.string.title_protection);
    }
    void getrecordview()
    {
        MyAdapter myAdapter = new MyAdapter(this, getCallRecords(),
                R.layout.recorder_item, new String[]{"callnumber", "calltime","callname","callTypeStr"},
                new int[]{R.id.callnumber, R.id.calltime, R.id.recorder_item_playBtn, R.id.recorder_item_complainBtn,R.id.callTypestr});
        lv.setAdapter(myAdapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("删除音频？");
                builder.setTitle("请注意");
                final int p =position;
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        new File(Constant.storeDirectory,getfileList().get(position)).delete();
                        getrecordview();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
                return true;
            }
        });
        setListViewHeightBasedOnChildren(lv);
        protectlayout.setVisibility(View.GONE);//这一句即隐藏布局LinearLayout区域
        bindlayout.setVisibility(View.GONE);
        recorderlayout.setVisibility(View.VISIBLE);
        title.setText(R.string.title_recorder);
    }
    //用于listview适配
    private List<Map<String, Object>> getCallRecords(){
        List<Map<String, Object>> listitem=new ArrayList<>();
        HashMap<String, Object> showitem;
        File[] files = new File(Constant.storeDirectory).listFiles();
        for (File file:files) {
            String filename = file.getName();
            filename = getFileNameNoEx(filename);
            String[] arr = filename.split("-",2);
            showitem = new HashMap<>();
            showitem.put("callnumber", arr[0]);
            showitem.put("calltime", arr[1]);
            showitem.put("callname", null);
            showitem.put("callTypeStr", "来自亲情帐号");
            listitem.add(showitem);
        }
        return listitem;
    }
    //获取文件名列表
    private List<String> getfileList()
    {
        List<String> filenames = new ArrayList<>();
        File directory = new File(Constant.storeDirectory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (int p = 0; p < files.length; p++) {
                filenames.add(files[p].getName());
            }
        }
        return filenames;
    }
    //文件名去后缀
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
    //检测服务是否启动
    private boolean isWorked(String className) {
        ActivityManager myManager = (ActivityManager) MainActivity.this
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(100);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName()
                    .equals(className)) {
                return true;
            }
        }
        return false;
    }
    //设置ListView高度
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        if(listView == null) return;

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
    private class Client implements Runnable {
        public void run() {
            PCMPlayer pcmPlayer = new PCMPlayer();
            try {
                Socket s = new Socket("192.168.1.109", 12345);
                s.setSoTimeout(5000);
                InputStream inputStream = s.getInputStream();
                DataInputStream input = new DataInputStream(inputStream);
                byte[] b = new byte[2048*10];
                int length = 0;
                int buffersize = 2048*10;
                while(Constant.islistenning)
                {
                    try {
                        length = input.read(b,20480-buffersize,buffersize);
                        System.out.println("length=="+length);
                        if(length==-1)break;
                        buffersize = buffersize-length;
                        if(buffersize==0)
                        {
                            pcmPlayer.write(b);
                            buffersize = 20480;
                        }
                    }
                    catch (SocketTimeoutException E)
                    {
                        break;
                    }
                }
                s.close();
                pcmPlayer.destoryPlay();
                Constant.islistenning = false;
            }catch(Exception ex)
            {
            }
            handler.sendMessage(new Message());
        }
    }
    private final class PCMPlayer {

        private AudioTrack audioTrack;
        private int bufferSize;

        public int getBufferSize() {
            return bufferSize;
        }

        // 默认
        private int sampleRate = 8000;                          // 采样率  4000 每秒钟采集4000个点
        private int channel = AudioFormat.CHANNEL_IN_STEREO;     // 声道个数 1 单声道
        private int format = AudioFormat.ENCODING_PCM_16BIT;     // 每个采样点8bit量化 采样精度

        public PCMPlayer() {
            initPlay();
        }

        public PCMPlayer(int sampleRate, int channel, int format) {
            this.sampleRate = sampleRate;
            this.channel = channel;
            this.format = format;
            initPlay();
        }

        private void initPlay() {

            /**
             * AudioTrack支持4K-48K采样率
             * (sampleRateInHz< 4000) || (sampleRateInHz > 48000) )
             */

            // 获得缓冲流大小
            bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, format);

            // 初始化AudioTrack
            /**
             * 参数:
             * 1.streamType
             *   STREAM_ALARM：警告声
             *   STREAM_MUSCI：音乐声，例如music等
             *   STREAM_RING：铃声
             *   STREAM_SYSTEM：系统声音
             *   STREAM_VOCIE_CALL：电话声音
             *
             * 2.采样率
             * 3.声道数
             * 4.采样精度
             * 5.每次播放的数据大小
             * 6.AudioTrack中有MODE_STATIC和MODE_STREAM两种分类。
             *   STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。
             *   意味着你只需要开启播放后 后续使用write方法(AudioTrack的方法)写入buffer就行
             *
             *   STATIC的意思是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
             *   后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
             *   这种方法对于铃声等内存占用较小，延时要求较高的声音来说很适用。
             */
            audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, channel, format,
                    bufferSize, AudioTrack.MODE_STREAM);

            // 播放 后续你直接write数据就行
            audioTrack.play();
        }

        // 写入数据就能播放 但是要先初始化 开启播放器
        public void write(byte[] buffer) {
            /**
             * 1.要播放的buffer
             * 2.播放需要的位移
             * 3.播放的数据长度
             */
            audioTrack.write(buffer, 0, buffer.length);
        }

        /**
         * 释放资源
         */
        public void destoryPlay() {
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }
        }
    }

    /**
     * 访问网络AsyncTask,访问网络在子线程进行并返回主线程通知访问的结果
     */
    class NetworkTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            ConnectWeb cw = new ConnectWeb();
            return cw.stopcalling(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    class ListenTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            ConnectWeb cw = new ConnectWeb();
            return cw.startlisten(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    class StopListenTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            ConnectWeb cw = new ConnectWeb();
            return cw.stoplisten(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}
