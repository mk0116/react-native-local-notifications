package com.github.wumke.RNLocalNotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Build;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.app.AlarmManager.INTERVAL_DAY;

public class RNLocalNotificationsModule extends ReactContextBaseJavaModule {

    ReactApplicationContext reactContext;
    AlarmManager alarmManager;
    String largeIconName = "ic_launcher";
    String largeIconType = "mipmap";
    String smallIconName = "notification_small";
    String smallIconType = "drawable";

    public RNLocalNotificationsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        alarmManager = (AlarmManager) reactContext.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public String getName() {
        return "RNLocalNotifications";
    }

    @ReactMethod
    public void createNotification(Integer id, String text, String datetime, String sound, String hiddendata) {
        this.createAlarm(id, text, datetime, sound, false, hiddendata);
    }

    @ReactMethod
    public void deleteNotification(Integer id) {
        this.deleteAlarm(id);
    }

    @ReactMethod
    public void updateNotification(Integer id, String text, String datetime, String sound, String hiddendata) {
        this.createAlarm(id, text, datetime, sound, true, hiddendata);
    }

    @ReactMethod
    public void setAndroidIcons(String largeIconNameNew, String largeIconTypeNew, String smallIconNameNew, String smallIconTypeNew) {
        largeIconName = largeIconNameNew;
        largeIconType = largeIconTypeNew;
        smallIconName = smallIconNameNew;
        smallIconType = smallIconTypeNew;
    }

    public void createAlarm(Integer id, String text, String datetime, String sound, boolean update, String hiddendata) {
        if(update){
            this.deleteAlarm(id);
        }

        final SimpleDateFormat desiredFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        Date dateToMillis = null;
        try {
            dateToMillis = desiredFormat.parse(datetime);
        } catch (ParseException e) {
            //TODO: if you want feedback...
            e.printStackTrace();
        }
        Long timeInMillis = dateToMillis.getTime();

        Intent intent = new Intent(reactContext, AlarmReceiver.class);
        intent.setAction("com.github.wumke.RNLocalNotifications.showAlarm");
        intent.putExtra("id", id);
        intent.putExtra("text", text);
        intent.putExtra("datetime", datetime);
        intent.putExtra("sound", sound);
        intent.putExtra("hiddendata", hiddendata);
        intent.putExtra("largeIconName", largeIconName);
        intent.putExtra("largeIconType", largeIconType);
        intent.putExtra("smallIconName", smallIconName);
        intent.putExtra("smallIconType", smallIconType);

        PendingIntent mAlarmSender = PendingIntent.getBroadcast(reactContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar date = Calendar.getInstance();
        if(timeInMillis > date.getTimeInMillis()) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    timeInMillis, INTERVAL_DAY, mAlarmSender);
        }
    }

    public void deleteAlarm(Integer id) {
        Intent intent = new Intent(reactContext, AlarmReceiver.class);
        intent.setAction("com.github.wumke.RNLocalNotifications.showAlarm");

        // cancel the alarm!
        PendingIntent pi = PendingIntent.getBroadcast(reactContext, id, intent, PendingIntent.FLAG_NO_CREATE);
        if(pi != null){
            pi.cancel();
            alarmManager.cancel(pi);
        }
    }
}
