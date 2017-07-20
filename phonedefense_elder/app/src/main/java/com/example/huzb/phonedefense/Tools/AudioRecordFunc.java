package com.example.huzb.phonedefense.Tools;

/**
 * Created by 34494 on 2017/2/18.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.database.DatabaseUtilsCompat;
import android.util.Log;

import com.example.huzb.phonedefense.Activity.ScrollingActivity;
import com.example.huzb.phonedefense.R;
import com.example.huzb.phonedefense.Util.Constant;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.huzb.phonedefense.listenservice.TelListenerService.storeDirectory;

public class AudioRecordFunc {
    // 缓冲区字节大小
    private int audiobufferSize = 0;
    private int dialoguebufferSize = 0;
    private int realtimebufferSize = 0;

    private final int AUDIO_SAMPLE_RATE=16000;//采样频率
    private final int DIALOGUE_SAMPLE_RATE=16000;//采样频率
    private final int REALTIME_SAMPLE_RATE=8000;//采样频率
    //AudioName裸音频数据文件 ，麦克风
    private String AudioName = "";

    private AudioRecord audioRecord;
    private AudioRecord dialogueRecord;
    private AudioRecord realtimeRecord;
    private boolean isRecord = false;// 设置正在录制的状态


    private static AudioRecordFunc mInstance;

    private Context mcontext;

    private AudioRecordFunc(){}

    private AudioRecordFunc(Context mcontext,String AudioName){
        this.AudioName=AudioName;
        this.mcontext=mcontext;
    }

    public synchronized static AudioRecordFunc getInstance(Context mcontext,String storepath)
    {
        if(mInstance == null) {
            mInstance = new AudioRecordFunc(mcontext,storepath);
        }
        return mInstance;
    }

    public void start() {
        // 让录制状态为true
        isRecord = true;
        Constant.islistenning = false;
        // 开启音频文件写入线程
        new Thread(new AudioThread()).start();//完整的录音
        new Thread(new DialogueThread()).start();//对话录音
        new Thread(new RealtimeThread()).start();//实时录音
    }
    public void stop() {
        isRecord = false;//停止文件写入
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
        }
        if (dialogueRecord != null) {
            dialogueRecord.stop();
            dialogueRecord.release();//释放资源
            dialogueRecord = null;
        }
        if (realtimeRecord != null) {
            realtimeRecord.stop();
            realtimeRecord.release();//释放资源
            realtimeRecord = null;
        }
        Constant.islistenning = false;
        boolean isStart = false;
    }


    private class AudioThread implements Runnable {
        @Override
        public void run() {
            // 获得缓冲区字节大小
            audiobufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            // 创建AudioRecord对象,用于录取完整音频
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, audiobufferSize);
            audioRecord.startRecording();
            writeAudioTOFile(AudioName);
        }
    }
    private class DialogueThread implements Runnable {
        //用于录取对话
        @Override
        public void run() {
            // 获得缓冲区字节大小
            dialoguebufferSize = AudioRecord.getMinBufferSize(DIALOGUE_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            // 创建AudioRecord对象，用于录取对话音频
            dialogueRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, DIALOGUE_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, dialoguebufferSize);
            dialogueRecord.startRecording();
            writeDialogueTOFile(AudioName);
        }
    }
    private class RealtimeThread implements Runnable {
        //用于录取对话
        @Override
        public void run() {
            // 获得缓冲区字节大小
            realtimebufferSize = AudioRecord.getMinBufferSize(REALTIME_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            // 创建AudioRecord对象，用于录取实时音频
            realtimeRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, REALTIME_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, realtimebufferSize);
            realtimeRecord.startRecording();
            try {
                sendRealtime();
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
    //完整音频写入
    private void writeAudioTOFile(String audioName) {
        //将完整的音频保存到文件
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        System.out.println("开始录音");
        byte[] audiodata = new byte[audiobufferSize];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File dir = new File(storeDirectory);
            if (!dir.exists()) {
                dir.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            File file = new File(audioName);
            if(file.exists()){
                file.delete();
            }
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (isRecord) {
            readsize = audioRecord.read(audiodata, 0, audiobufferSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null)
            {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if(fos != null){
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        copyWaveFile(audioName,AUDIO_SAMPLE_RATE,0);//将临时的文件改写成可播放wav文件
    }
    //实时音频写入
    private void sendRealtime() throws SocketException, UnknownHostException {
        //将完整的音频保存到文件
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        System.out.println("sendRealtimestart!!!!!!!!!!!!!!!!");
        byte[] audiodata = new byte[realtimebufferSize];
        byte[] tenaudiodata = new byte[10*realtimebufferSize];//10帧一组
        int readsize = 0;
        int count = 0;
        DatagramSocket socket = new DatagramSocket(4567);
        InetAddress serverAddress = InetAddress.getByName(Constant.ADDRESS);
        while (isRecord) {
            readsize = realtimeRecord.read(audiodata, 0, realtimebufferSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize)
            {
                try {
                    if(Constant.islistenning) {
                        if (count == 0) {
                            for (int i = 0; i < 10 * realtimebufferSize; ++i) {
                                tenaudiodata[i] = 0;
                            }
                        }
                        System.arraycopy(audiodata, 0, tenaudiodata, count * realtimebufferSize, realtimebufferSize);
                        if (count == 9 || !isRecord) {
                            DatagramPacket bag = new DatagramPacket(tenaudiodata,tenaudiodata.length,serverAddress,4567);
                            socket.send(bag);
                            System.out.println("bagsize=="+bag.getLength());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            count=(count+1)%10;
        }
        socket.close();
    }
    //对话音频写入
    private void writeDialogueTOFile(String audioName){
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[dialoguebufferSize];
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File dir = new File(storeDirectory);
            if (!dir.exists()) {
                dir.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int page = 0;
        int[] volumes = new int[32];
        int count = 0;
        boolean isStart = false;
        while (isRecord) {
            readsize = dialogueRecord.read(audiodata, 0, dialoguebufferSize);
            volumes[count] = calculateVolume(audiodata,16);
            System.out.println("volumes=="+volumes[count]);
            if (isStart && count==31)
            {
                int mean = 0;
                for(int volume:volumes)
                {
                    mean+=volume;
                }
                mean = mean>>5;
                System.out.println("mean=="+mean);
                if(mean==0)
                {
                    count = (count+1)%32;
                    isStart = false;
                    copyWaveFile(audioName+"_"+page,DIALOGUE_SAMPLE_RATE,1);
                    page++;
                    continue;
                }
            }
            if(!isStart&&volumes[count]>1)
            {
                try {
                    File file = new File(audioName+"_"+page);
                    if (file.exists()) {
                        file.delete();
                    }
                    fos = new FileOutputStream(file);// 建立一个可存取字节的文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isStart=true;
            }
            count = (count+1)%32;
            if (isStart && AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null)
            {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if(fos != null){
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(new File(audioName+"_"+page).exists()) {
            copyWaveFile(audioName + "_" + page, DIALOGUE_SAMPLE_RATE,1);
        }
    }
    private void copyWaveFile(String audioName,int audioSampleRate,int type) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        int channels = 2;
        long byteRate = 16 * audioSampleRate * channels / 8;
        byte[] data = null;
        if(type == 0){
            data = new byte[audiobufferSize];
        }
        else if(type == 1){
            data = new byte[dialoguebufferSize];
        }
        else return;
        try {
            in = new FileInputStream(audioName);
            totalAudioLen = in.getChannel().size();
            System.out.println("totalAudioLen=="+totalAudioLen);
            if(totalAudioLen >= 160000) {
                totalDataLen = totalAudioLen + 36;
                out = new FileOutputStream(audioName+".wav");
                WriteWaveFileHeader(out, totalAudioLen, totalDataLen, (long) audioSampleRate, channels, byteRate);
                while (in.read(data) != -1) {
                    out.write(data);
                }
                out.close();
                if(type == 1)
                {
                    upFile(audioName+".wav",audioName.split("/")[audioName.split("/").length-1]+".wav");
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File infile = new File(audioName);
        infile.delete();
    }
    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

//    public String[] getname() {
//        String[] result= new String[2];
//        ContentResolver resolver = mcontext.getContentResolver();
//        String callDateStr=null;
//        Log.i("process","1");
//        if (ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            Log.i("process","2");
//            return null;
//        }
//        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, // 查询通话记录的URI
//                new String[]{
//                        CallLog.Calls.NUMBER// 通话记录的电话号码
//                        , CallLog.Calls.DATE// 通话记录的日期
//                }
//                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
//        );
//        Log.i("process","3");
//        if (cursor.moveToFirst()) {
//            //拨打时间
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date callDate = new Date(Long.parseLong(cursor.getString(1)));
//            callDateStr = sdf.format(callDate);
//            Log.i("process","4");
//        }
//        Log.i("process","5 "+cursor.getString(0)+"-"+callDateStr);
//        result[0]=cursor.getString(0);
//        result[1]=cursor.getString(0)+"-"+callDateStr+".wav";
//        return result;
//    }
    public static int calculateVolume(byte[] var0, int var1) {
        int[] var3 = null;
        int var4 = var0.length;
        int var2;
        if(var1 == 8) {
            var3 = new int[var4];
            for(var2 = 0; var2 < var4; ++var2) {
                var3[var2] = var0[var2];
            }
        } else if(var1 == 16) {
            var3 = new int[var4 / 2];
            for(var2 = 0; var2 < var4 / 2; ++var2) {
                byte var5 = var0[var2 * 2];
                byte var6 = var0[var2 * 2 + 1];
                int var13;
                if(var5 < 0) {
                    var13 = var5 + 256;
                } else {
                    var13 = var5;
                }
                short var7 = (short)(var13 + 0);
                if(var6 < 0) {
                    var13 = var6 + 256;
                } else {
                    var13 = var6;
                }
                var3[var2] = (short)(var7 + (var13 << 8));
            }
        }

        int[] var8 = var3;
        if(var3 != null && var3.length != 0) {
            float var10 = 0.0F;
            for(int var11 = 0; var11 < var8.length; ++var11) {
                var10 += (float)(var8[var11] * var8[var11]);
            }
            var10 /= (float)var8.length;
            float var12 = 0.0F;
            for(var4 = 0; var4 < var8.length; ++var4) {
                var12 += (float)var8[var4];
            }
            var12 /= (float)var8.length;
            var4 = (int)(Math.pow(2.0D, (double)(var1 - 1)) - 1.0D);
            double var14 = Math.sqrt((double)(var10 - var12 * var12));
            int var9;
            if((var9 = (int)(10.0D * Math.log10(var14 * 10.0D * Math.sqrt(2.0D) / (double)var4 + 1.0D))) < 0) {
                var9 = 0;
            }
            if(var9 > 10) {
                var9 = 10;
            }
            return var9;
        } else {
            return 0;
        }
    }
    /**
     * 访问网络AsyncTask,访问网络在子线程进行并返回主线程通知访问的结果
     */
//    class NetworkTask extends AsyncTask<String, Integer, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            ConnectWeb cw = new ConnectWeb();
//            return cw.upFile(params[0],params[1]);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            if(result.equals("find")) {
//                addNotificaction(result);
//            }
//        }
//    }
    public void upFile(String filePath,String filename) {
        String TAG="upfile";
        OkHttpClient mOkHttpClient =
                new OkHttpClient.Builder()
                        .readTimeout(600, TimeUnit.SECONDS)
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
                .url(Constant.BASE_URL+"/upfile")
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if(result.equals("find")) {
                    addNotificaction(result);
                }
            }
        });
    }
    private void addNotificaction(String callnum) {
        if(callnum==null)
        {
            return;
        }
        NotificationManager mNotifyMgr =
                (NotificationManager) mcontext.getSystemService(mcontext.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(
                mcontext, 0, new Intent(mcontext, ScrollingActivity.class), 0);

        Notification notification = new Notification.Builder(mcontext)
                .setSmallIcon(R.mipmap.micon)
                .setContentTitle("提醒：刚才的电话可能是诈骗电话")
                .setContentText("触摸可发送通话至亲情邮箱")
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();// getNotification()

        mNotifyMgr.notify(1, notification);
    }
}
