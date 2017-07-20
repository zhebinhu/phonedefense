package com.example.huzb.phonedefense.Activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.huzb.phonedefense.R;
import com.example.huzb.phonedefense.Tools.ConnectWeb;
import com.example.huzb.phonedefense.Util.Constant;
import com.example.huzb.phonedefense.listenservice.TelListenerService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by 34494 on 2017/1/29.
 */

public class MyAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;

    private int[] mTo;
    private String[] mFrom;
    private SimpleAdapter.ViewBinder mViewBinder;

    private List<? extends Map<String, ?>> mData;

    private int mResource;
    private int mDropDownResource;
    private Context mContext;

    public MyAdapter(Context context, List<? extends Map<String, ?>> data,
                     @LayoutRes int resource, String[] from, @IdRes int[] to) {
        mData = data;
        mResource = mDropDownResource = resource;
        mFrom = from;
        mTo = to;
        mContext=context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {

            holder=new ViewHolder();

            //可以理解为从vlist获取view  之后把view返回给ListView

            convertView = mInflater.inflate(R.layout.recorder_item, null);
            holder.callname = (TextView)convertView.findViewById(mTo[0]);
            holder.calltime = (TextView)convertView.findViewById(mTo[1]);
            holder.sendBtn = (Button)convertView.findViewById(mTo[2]);
            holder.complainBtn = (Button)convertView.findViewById(mTo[3]);
            holder.callTypestr = (TextView)convertView.findViewById(mTo[4]);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        if(mData.get(position).get(mFrom[2])==null) {
            holder.callname.setText((String) mData.get(position).get(mFrom[0]));
        }
        else
        {
            holder.callname.setText((String) mData.get(position).get(mFrom[2]));
        }
        holder.callTypestr.setText((String)mData.get(position).get(mFrom[3]));
        holder.calltime.setText((String)mData.get(position).get(mFrom[1]));
        holder.sendBtn.setTag(position);
        holder.complainBtn.setTag(position);
        final String filename=(String) mData.get(position).get(mFrom[0])+"-"+(String)mData.get(position).get(mFrom[1])+".wav";
//        String familyemail = null;
//        PhonedefenseDB db = new PhonedefenseDB(mContext);
//        Cursor cursor;
//        cursor = db.getReadableDatabase().query(PhonedefenseDB.FAMILYEMAIL_TABLE, new String[]{"FamilyEmail"}, null, null, null, null, null);
//        if(cursor.moveToFirst())
//        {
//            familyemail=cursor.getString(0);
//        }
//        final String final_familyemail=familyemail;
        //给Button添加单击事件  添加Button之后ListView将失去焦点  需要的直接把Button的焦点去掉
        holder.sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final String filePath= TelListenerService.storeDirectory+"/"+filename;
                final File file=new File(filePath);
                builder.setMessage("发送语音邮件到亲情帐号?");
                builder.setTitle("请注意");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if(!file.exists()){
                            Toast.makeText(mContext,"未找到录音文件", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            upVideo(filePath,filename);
                            Toast.makeText(mContext,"发送成功", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        holder.complainBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final String filePath= TelListenerService.storeDirectory+"/"+filename;
                final File file=new File(filePath);
                builder.setMessage("举报该通话？");
                builder.setTitle("请注意");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if(!file.exists())
                        {
                            Toast.makeText(mContext,"未找到录音文件", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            complain(filePath,filename);
                            Toast.makeText(mContext,"举报成功", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        return convertView;
    }
    public final class ViewHolder {
        public TextView callname;
        public TextView calltime;
        public TextView callTypestr;
        public Button sendBtn;
        public Button complainBtn;
    }
    public void upVideo(String filePath,String filename) {
        String TAG="upvideo";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("elderid",Constant.elderid);
        builder.addFormDataPart("video",filename,
                RequestBody.create(MediaType.parse("video"), new File(filePath)));

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .tag(TAG)
                .url(Constant.BASE_URL+"/upvideo")
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
            }
        });
    }
    public void complain(String filePath,String filename) {
        String TAG="complain";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .readTimeout(600, TimeUnit.SECONDS)
                        .build();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("video",filename,
                RequestBody.create(MediaType.parse("video"), new File(filePath)));

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .tag(TAG)
                .url(Constant.BASE_URL+"/complain")
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
            }
        });
    }
}
