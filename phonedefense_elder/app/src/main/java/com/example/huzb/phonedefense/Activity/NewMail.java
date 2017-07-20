package com.example.huzb.phonedefense.Activity;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.example.huzb.phonedefense.listenservice.TelListenerService;

import java.io.File;

/**
 * Created by 34494 on 2017/1/30.
 */

public class NewMail implements Runnable {
    private static final String mSender = "15528166872@163.com";//这里以163邮箱为例，根据变量名能看出来这个变量是发送者的邮箱地址
    private static final String mSenderPass = "fish11";//发送者的邮箱密码
    private static final String HostName = "smtp.163.com";//163邮箱的发送邮件服务器
    private static final String HostPort = "465";//服务器端口
    private String mReceiver = null;
    private Context mContext;

    private String mTitle,mMsg,mfileName;//邮件的标题和内容
    public NewMail(String title, String msg) {
        mTitle=title;
        mMsg=msg;
    }
    public NewMail(Context context, String title, String msg, String fileName, String familyemail) {
        mTitle=title;
        mMsg=msg;
        mfileName=fileName;
        mReceiver=familyemail;
        mContext=context;
    }
    @Override
    public void run() {
        MailUtils sender = new MailUtils().setUser(mSender).setPass(mSenderPass)
                .setFrom(mSender).setTo(mReceiver).setHost(HostName)
                .setPort(HostPort).setSubject(mTitle).setBody(mMsg);
        sender.init();
        if(mfileName!=null) {
            File file = new File(TelListenerService.storeDirectory,mfileName);
            if(file.exists()) {
                try {
                    sender.addAttachment(TelListenerService.storeDirectory, mfileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /*else
            {
                Looper.prepare();
                Toast.makeText(mContext, "未找到录音文件", Toast.LENGTH_SHORT).show();
                Looper.loop();
                return;
            }*/
        }
        try {
            sender.send();
        } catch (Exception e) {
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(mContext, "发送失败", Toast.LENGTH_SHORT).show();
            Looper.loop();
            return;
        }
        Looper.prepare();
        Toast.makeText(mContext, "发送成功！", Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

}
