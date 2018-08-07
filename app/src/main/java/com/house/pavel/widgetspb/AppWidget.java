package com.house.pavel.widgetspb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    final static String LOG_TAG = "myLogs";

    private static final String SYNC_CLICKED    = "ospb_update_action";
    private static final String WAITING_MESSAGE = "Wait...";
    private static final String ERROR_MESSAGE = "ERROR!";
    private static final String APPPackageName = "ru.spb.iac.ourspb";
    public static final int httpsDelayMs = 300;
    //private String ID    = "123123123";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp,
                                int appWidgetId) {


        // Читаем параметры Preferences
        //String widgetText = sp.getString(ConfigActivity.WIDGET_TEXT + appWidgetId, null);
        String spb_id = sp.getString(ConfigActivity.ID_PREF + appWidgetId, null);
        //Log.d(LOG_TAG, "Get text to widget: " + widgetText);
        if (spb_id == null) return;
        //int widgetColor = sp.getInt(ConfigActivity.WIDGET_COLOR + widgetID, 0);

        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextViewText(R.id.appwidget_text, WAITING_MESSAGE );

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        String output;

        //запускаем отдельный поток для получения данных с сайта биржи
        //в основном потоке делать запрос нельзя - выбросит исключение
        MyTask thread = new MyTask(spb_id);
        thread.start();
        try {
            while (true) {
                Thread.sleep(300);
                if(!thread.isAlive()) {
                    output = thread.getInfoString();
                    break;
                }
            }

        } catch (Exception e) {
            output = e.toString();
        }
        //output += spb_id;
        //выводим в виджет результат
        views.setTextViewText(R.id.appwidget_text, output);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        watchWidget = new ComponentName(context, AppWidget.class);

        SharedPreferences sp = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, sp, id);
        }


        //при клике на виджет в систему отсылается вот такой интент, описание метода ниже
        remoteViews.setOnClickPendingIntent(R.id.appwidget_text,   getPendingSelfIntent(context, SYNC_CLICKED));

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        //обновление всех экземпляров виджета
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, sp, appWidgetId);
        }

    }

    //этот метод ловит интенты, срабатывает когда интент создан нажатием на виджет и
    //запускает обновление виджета
    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);

        if (SYNC_CLICKED.equals(intent.getAction())) {

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                RemoteViews remoteViews;
                ComponentName watchWidget;

                remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                watchWidget = new ComponentName(context, AppWidget.class);

                remoteViews.setTextViewText(R.id.appwidget_text, WAITING_MESSAGE);

                //updating widget
                appWidgetManager.updateAppWidget(watchWidget, remoteViews);

                SharedPreferences sp = context.getSharedPreferences(
                        ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
                String spb_id = sp.getString(ConfigActivity.ID_PREF, null);

                String output;
                MyTask thread = new MyTask(spb_id);
                thread.start();
                try {
                    while (true) {
                        Thread.sleep(httpsDelayMs);
                        if (!thread.isAlive()) {
                            output = thread.getInfoString();
                            break;
                        }
                    }

                } catch (Exception e) {
                    output = e.toString();

                }

                // в случае отсутствия связи
                if (output.isEmpty()) {
                    remoteViews.setTextViewText(R.id.appwidget_text, ERROR_MESSAGE);
                    appWidgetManager.updateAppWidget(watchWidget, remoteViews);
                    return;
                }
                //output += spb_id;

                //Обновляем экран с полученными данными
                remoteViews.setTextViewText(R.id.appwidget_text, output);
                //widget manager to update the widget
                appWidgetManager.updateAppWidget(watchWidget, remoteViews);

                //запускаем основное приложение
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(APPPackageName);
                if (launchIntent != null) {
                    context.startActivity(launchIntent);//null pointer check in case package name was not found
                }
                else {
                    try {

                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("market://details?id=" + APPPackageName));

                        context.startActivity(intent);

                    } catch (android.content.ActivityNotFoundException anfe) {

                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + APPPackageName));
                        context.startActivity(intent);

                    }
                }
        }
    }



    //создание интента
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));

        // Удаляем Preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            //editor.remove(ConfigActivity.WIDGET_TEXT + widgetID);
            editor.remove(ConfigActivity.ID_PREF);
        }
        editor.commit();
    }
}

