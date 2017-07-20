package com.example.huzhebin.phonedefense_family;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.FileProvider;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
            holder.playBtn = (Button)convertView.findViewById(mTo[2]);
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
        holder.playBtn.setTag(position);
        holder.complainBtn.setTag(position);
        final String filename=(String) mData.get(position).get(mFrom[0])+"-"+(String)mData.get(position).get(mFrom[1])+".wav";
        holder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Intent intent = getAudioFileIntent(Constant.storeDirectory + "/" + filename);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    mContext.startActivity(intent);
//                }
//                else{
//                    File file = new File(Constant.storeDirectory + "/" + filename);
//                    Uri uri = FileProvider.getUriForFile(mContext,"com.example.huzhebin",file);
//                    Intent intent = getAudioFileIntent(Constant.storeDirectory + "/" + filename);
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                }
            }
        });
        holder.complainBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final String filePath= Constant.storeDirectory+"/"+filename;
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
    public static Intent getAudioFileIntent(String param ) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param ));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }
    public final class ViewHolder {
        public TextView callname;
        public TextView calltime;
        public TextView callTypestr;
        public Button playBtn;
        public Button complainBtn;
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
