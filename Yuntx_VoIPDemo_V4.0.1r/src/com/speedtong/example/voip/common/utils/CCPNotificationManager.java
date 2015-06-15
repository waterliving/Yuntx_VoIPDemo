package com.speedtong.example.voip.common.utils;

import com.speedtong.example.voip.ECApplication;
import com.speedtong.example.voip.R;
import com.speedtong.sdk.ECDevice.CallType;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class CCPNotificationManager {
	
	public static final int CCP_NOTIFICATOIN_ID_CALLING = 0x1;
	
	private static NotificationManager mNotificationManager;

	public static void showInCallingNotication(Context context ,CallType callType ,String topic, String text) {
		
		try {
			
			checkNotification(context);
			
			Notification notification = new Notification(R.drawable.icon_call_small, text,
					System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.tickerText = topic;
			Intent intent = null;
			if(callType == CallType.VIDEO) {
				intent = new Intent(ECApplication.ACTION_VIDEO_INTCALL);
			} else {
				intent = new Intent(ECApplication.ACTION_VOIP_INCALL);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);  
			
			PendingIntent contentIntent = PendingIntent.getActivity(context, 
					R.string.app_name, 
					intent, 
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			notification.setLatestEventInfo(context, 
					topic, 
					text, 
					contentIntent);
			
			mNotificationManager.notify(CCP_NOTIFICATOIN_ID_CALLING, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void showOutCallingNotication(Context context ,CallType callType ,String topic, String text) {
		
		try {
			
			checkNotification(context);
			
			Notification notification = new Notification(R.drawable.icon_call_small, text,
					System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL; 
			notification.tickerText = topic;
			Intent intent = null;
			if(callType == CallType.VIDEO) {
				intent = new Intent(ECApplication.ACTION_VIDEO_OUTCALL);
			} else {
				intent = new Intent(ECApplication.ACTION_VOIP_OUTCALL);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);  
			
			PendingIntent contentIntent = PendingIntent.getActivity(
					context, 
					R.string.app_name, 
					intent, 
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			notification.setLatestEventInfo(context, 
					topic, 
					text, 
					contentIntent);
			
			mNotificationManager.notify(CCP_NOTIFICATOIN_ID_CALLING, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	public static void cancleCCPNotification(Context context , int id) {
		checkNotification(context);
		
		mNotificationManager.cancel(id);
	}

	private static void checkNotification(Context context) {
		if(mNotificationManager == null) {
			mNotificationManager = (NotificationManager) context.getSystemService(
					Context.NOTIFICATION_SERVICE);
		}
	}
}
