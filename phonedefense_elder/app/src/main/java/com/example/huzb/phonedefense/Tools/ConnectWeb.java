package com.example.huzb.phonedefense.Tools;

import android.util.Log;

import com.example.huzb.phonedefense.Util.Constant;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 34494 on 2017/2/16.
 */

public class ConnectWeb {
    //post上传音频
//    public String upFile(String filePath,String filename) {
//        String TAG=" ";
//        OkHttpClient mOkHttpClient =
//                new OkHttpClient.Builder()
//                .readTimeout(600,TimeUnit.SECONDS)
//                .build();
//
//        String result = "error";
//        MultipartBody.Builder builder = new MultipartBody.Builder();
//        // 这里演示添加用户ID
//        builder.setType(MultipartBody.FORM);
//        builder.addFormDataPart("video",filename,
//               RequestBody.create(MediaType.parse("video"), new File(filePath)));
//
//        RequestBody requestBody = builder.build();
//        Request.Builder reqBuilder = new Request.Builder();
//        Request request = reqBuilder
//                .url(Constant.BASE_URL+"/upfile")
//                .post(requestBody)
//                .build();
//
//        Log.i(TAG, "adress" + Constant.BASE_URL);
//        try{
//            Response response = mOkHttpClient.newCall(request).execute();
//            new File(filePath).delete();
//            Log.i(TAG, "响应码 " + response.code());
//            if (response.isSuccessful()) {
//                String resultValue = response.body().string();
//                Log.d(TAG, "响应体 " + resultValue);
//                return resultValue;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
    //绑定帐号
    public String bind(String elderid,String famliyid) {
        String TAG=" ";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .build();

        String result = "error";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("elderid",elderid);
        builder.addFormDataPart("familyid",famliyid);

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(Constant.BASE_URL+"/bind")
                .post(requestBody)
                .build();

        Log.i(TAG, "adress" + Constant.BASE_URL);
        try{
            Response response = mOkHttpClient.newCall(request).execute();
            Log.i(TAG, "响应码 " + response.code());
            if (response.isSuccessful()) {
                String resultValue = response.body().string();
                Log.d(TAG, "响应体 " + resultValue);
                return resultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    //post上传通话状态
    public String calling(String elderid,String status) {
        String TAG=" ";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .build();

        String result = "fail";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("elderid",elderid);
        builder.addFormDataPart("status",status);

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(Constant.BASE_URL+"/calling")
                .post(requestBody)
                .build();

        Log.i(TAG, "adress" + Constant.BASE_URL);
        try{
            Response response = mOkHttpClient.newCall(request).execute();
            Log.i(TAG, "响应码 " + response.code());
            if (response.isSuccessful()) {
                String resultValue = response.body().string();
                Log.d(TAG, "响应体 " + resultValue);
                return resultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    //get获取消息
    public String polling(String elderid) {
        String TAG=" ";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .readTimeout(30,TimeUnit.SECONDS)
                        .build();

        String result = "fail";
        Request.Builder reqBuilder = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.BASE_URL+"/elderpolling").newBuilder();
        urlBuilder.addQueryParameter("elderid",elderid);
        Request request = reqBuilder
                .get()
                .url(urlBuilder.build())
                .build();

        Log.i(TAG, "adress" + Constant.BASE_URL);
        try{
            while(Constant.iscalling) {
                Response response = mOkHttpClient.newCall(request).execute();
                Log.i(TAG, "响应码 " + response.code());
                if (response.isSuccessful()) {
                    String resultValue = response.body().string();
                    Log.d(TAG, "响应体 " + resultValue);
                    return resultValue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
