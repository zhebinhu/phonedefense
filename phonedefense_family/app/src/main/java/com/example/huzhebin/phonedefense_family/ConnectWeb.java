package com.example.huzhebin.phonedefense_family;

import android.util.Log;

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
//    //长轮询监视老人端通话状态
//    public String polling(String familyid) {
//        String TAG="polling";
//        OkHttpClient mOkHttpClient =
//                new OkHttpClient.Builder()
//                        .readTimeout(60,TimeUnit.SECONDS)
//                        .build();
//
//        String result = "fail";
//        Request.Builder reqBuilder = new Request.Builder();
//        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constant.BASE_URL+"/familypolling").newBuilder();
//        urlBuilder.addQueryParameter("familyid",familyid);
//        Request request = reqBuilder
//                .get()
//                .url(urlBuilder.build())
//                .build();
//
//        Log.i(TAG, "adress" + Constant.BASE_URL);
//        try{
//            while(Constant.ispolling) {
//                Response response = mOkHttpClient.newCall(request).execute();
//                Log.i(TAG, "响应码 " + response.code());
//                if (response.isSuccessful()) {
//                    String resultValue = response.body().string();
//                    Log.d(TAG, "响应体 " + resultValue);
//                    return resultValue;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
    //远程中断通话
    public String stopcalling(String familyid) {
        String TAG="stopcalling";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .build();

        String result = "fail";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("familyid",familyid);

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(Constant.BASE_URL+"/stopcalling")
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
    //监听信号
    public String startlisten(String familyid) {
        String TAG="startlisten";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .build();

        String result = "fail";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("familyid",familyid);

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(Constant.BASE_URL+"/startlisten")
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

    //结束监听信号
    public String stoplisten(String familyid) {
        String TAG="stoplisten";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .build();

        String result = "fail";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("familyid",familyid);

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(Constant.BASE_URL+"/stoplisten")
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
}
