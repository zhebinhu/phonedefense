package com.example.huzb.phonedefense.Activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.huzb.phonedefense.R;
import com.example.huzb.phonedefense.Tools.ConnectWeb;
import com.example.huzb.phonedefense.Util.Constant;
import com.example.huzb.phonedefense.listenservice.TelListenerService;
import com.example.huzb.phonedefense.listenservice.TelProtectService;
import com.example.huzb.phonedefense.zxing.activity.CaptureActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.huzb.phonedefense.listenservice.TelListenerService.storeDirectory;


public class ScrollingActivity extends AppCompatActivity {

    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        lv=(ListView)findViewById(R.id.calllistview);
        setSupportActionBar(toolbar);
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        Constant.elderid = tm.getDeviceId();
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(!new File(storeDirectory).exists())
        {
            new File(storeDirectory).mkdir();
        }
        if(!isWorked("com.example.huzb.phonedefense.listenservice.TelListenerService"))
        {
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGrey)));
        }
        else
        {
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colordeepGrey)));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isWorked("com.example.huzb.phonedefense.listenservice.TelListenerService")) {
                    Intent startIntent = new Intent(ScrollingActivity.this, TelListenerService.class);
                    startService(startIntent);
                    Toast.makeText(ScrollingActivity.this, "服务启动!", Toast.LENGTH_SHORT).show();
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colordeepGrey)));
                }
                else
                {
                    Intent stopIntent = new Intent(ScrollingActivity.this, TelListenerService.class);
                    stopService(stopIntent);
                    Log.i("mainthread","监听进程关闭");

                    TimerTask task = new TimerTask() {
                        public void run() {
                            if (isWorked("com.example.huzb.phonedefense.listenservice.TelListenerService")) {
                                Intent stopIntent = new Intent(ScrollingActivity.this, TelListenerService.class);
                                stopService(stopIntent);
                                Log.i("mainthread", "监听进程关闭");
                            }
                            if (isWorked("com.example.huzb.phonedefense.listenservice.TelProtectService")) {
                                Intent stopIntent = new Intent(ScrollingActivity.this, TelProtectService.class);
                                stopService(stopIntent);
                                Log.i("mainthread", "守护进程关闭");
                            }
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(task, 1000);
                    Toast.makeText(ScrollingActivity.this, "服务关闭", Toast.LENGTH_SHORT).show();
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGrey)));
                }
            }
        });
        updateView();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        updateView();
    }
    //更新视图
    private void updateView()
    {
        MyAdapter myAdapter = new MyAdapter(this, getCallRecords(),
                R.layout.recorder_item, new String[]{"callnumber", "calltime","callname","callTypeStr"},
                new int[]{R.id.callnumber, R.id.calltime, R.id.recorder_item_sendBtn, R.id.recorder_item_complainBtn,R.id.callTypestr});
        lv.setAdapter(myAdapter);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScrollingActivity.this);
                builder.setMessage("删除音频？");
                builder.setTitle("请注意");
                final int p =position;
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        new File(TelListenerService.storeDirectory,getfileList().get(position)).delete();
                        updateView();
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
        //设置点击事件
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent=getAudioFileIntent(storeDirectory+"/"+new File(storeDirectory).listFiles()[position].getName());
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                startActivity(intent);
//            }
//        });
        setListViewHeightBasedOnChildren(lv);
    }
//    public static Intent getAudioFileIntent(String param ) {
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("oneshot", 0);
//        intent.putExtra("configchange", 0);
//        Uri uri = Uri.fromFile(new File(param ));
//        intent.setDataAndType(uri, "audio/*");
//        return intent;
//    }
//　　从数据库获取isfilter
    //用于获取文件列表
    private List<String> getfileList()
    {
        List<String> filenames = new ArrayList<>();
        File directory = new File(TelListenerService.storeDirectory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (int p = 0; p < files.length; p++) {
                filenames.add(files[p].getName());
            }
        }
        return filenames;
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
    private List<Map<String, Object>> getCallRecords(){
        List<Map<String, Object>> listitem=new ArrayList<>();
        HashMap<String, Object> showitem;
        File[] files = new File(storeDirectory).listFiles();
        for (File file:files) {
            String filename = file.getName();
            filename = getFileNameNoEx(filename);
            if(filename.contains("_"))
            {
                file.delete();
                continue;
            }
            String[] arr = filename.split("-",2);
            String callname = getContactNameFromPhoneBook(arr[0]);
            if(!isfilter()||callname==null) {
                showitem = new HashMap<>();
                showitem.put("callnumber", arr[0]);
                showitem.put("calltime", arr[1]);
                showitem.put("callname", callname);
                showitem.put("callTypeStr", "呼入");
                listitem.add(showitem);
            }
        }
        return listitem;
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
//    //获取通话记录
//    private List<Map<String, Object>> getRecord() {
//        List<Map<String, Object>> listitem=new ArrayList<>();
//        Map<String, Object> showitem;
//        ContentResolver resolver = getContentResolver();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//            return null;
//        }
//        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, // 查询通话记录的URI
//                new String[]{CallLog.Calls.CACHED_NAME// 通话记录的联系人
//                        , CallLog.Calls.NUMBER// 通话记录的电话号码
//                        , CallLog.Calls.DATE// 通话记录的日期
//                        , CallLog.Calls.DURATION// 通话时长
//                        , CallLog.Calls.TYPE//通话类型
//                }
//                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
//        );
//        int i=0;
//        for(cursor.moveToFirst();!cursor.isAfterLast() & i<20; cursor.moveToNext()){
//            String number=cursor.getString(1);
//            String name=cursor.getString(0);
//            if(isfilter() && name!=null)
//            {
//                break;
//            }
//            //通话类型
//            int callType=Integer.parseInt(cursor.getString(4));
//            String callTypeStr="";
//            switch (callType) {
//                case CallLog.Calls.INCOMING_TYPE:
//                    callTypeStr="呼入";
//                    break;
//                case CallLog.Calls.OUTGOING_TYPE:
//                    callTypeStr="呼出";
//                    break;
//                case CallLog.Calls.MISSED_TYPE:
//                    callTypeStr="未接";
//                    break;
//            }
//            //拨打时间
//            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date callDate=new Date(Long.parseLong(cursor.getString(2)));
//            String callDateStr=sdf.format(callDate);
//            //通话时长
//            int callDuration=Integer.parseInt(cursor.getString(3));
//            int min=callDuration/60;
//            int sec=callDuration%60;
//            String callDurationStr=min+"分"+sec+"秒";
//
//            i++;
//            showitem = new HashMap<>();
//            showitem.put("callnumber",number);
//            showitem.put("calltime", callDateStr);
//            showitem.put("callname",name);
//            showitem.put("callTypeStr",callTypeStr);
//
//            listitem.add(showitem);
//        }
//        return listitem;
//    }
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
    //检测服务是否启动
    private boolean isWorked(String className) {
        ActivityManager myManager = (ActivityManager) ScrollingActivity.this
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(100);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName()
                    .equals(className)) {
                return true;
            }
            //Log.i("servicename",runningService.get(i).service.getClassName().toString());
        }
        return false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        menu.getItem(1).setChecked(isfilter());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.family_bind:
                Intent intent2 = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent2,0);
                break;
            case R.id.contacts:
                item.setChecked(!item.isChecked());
                PhonedefenseDB db = new PhonedefenseDB(this);
                Cursor cursor;
                String sql;
                cursor = db.getReadableDatabase().query(PhonedefenseDB.ISFILTER_TABLE, new String[]{"isfilter"}, null, null, null, null, null);
                if(cursor.moveToFirst())
                {
                    sql="update "+PhonedefenseDB.ISFILTER_TABLE+" set isfilter='"+(item.isChecked()?1:0)+"' where Id=0";
                }
                else
                {
                    sql="insert into "+PhonedefenseDB.ISFILTER_TABLE+"(Id,isfilter) values(0,'"+(item.isChecked()?1:0)+"')";
                }
                db.getReadableDatabase().execSQL(sql);
                updateView();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            new NetworkTask().execute(Constant.elderid,scanResult);
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
            return cw.bind(params[0],params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("success")) {
                Toast.makeText(ScrollingActivity.this,"绑定成功！",Toast.LENGTH_SHORT).show();
            }
            else if(result.equals("existed")){
                Toast.makeText(ScrollingActivity.this,"帐号已绑定",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
