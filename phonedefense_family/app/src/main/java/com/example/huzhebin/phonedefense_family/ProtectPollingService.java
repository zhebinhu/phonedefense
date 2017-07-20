package com.example.huzhebin.phonedefense_family;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ProtectPollingService extends Service {
    public ProtectPollingService() {
    }
    @Override
    public void onCreate() {
        Intent i = new Intent(this, PollingService.class);
        startService(i);
        Log.i("ProtectPollingService", "ProtectPollingService.守护进程");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
