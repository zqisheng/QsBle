package com.zqs.ble.impl;

import android.os.Handler;
import android.os.Message;

import com.zqs.ble.core.deamon.DefaultMessageLooper;

/*
 *   @author zhangqisheng
 *   @date 2022-08-06 15:23
 *   @description
 */
public class HandleMessageLooper extends DefaultMessageLooper {

   private Handler handler;
   private static final String token = "HANDLEMESSAGELOOPER_TOKEN";

   public HandleMessageLooper(Handler handler) {
      super(handler.getLooper().getThread());
      this.handler = handler;
   }

   @Override
   public void block(long awaitTime) {
      if (awaitTime!=Long.MAX_VALUE&&awaitTime>=0){
         handler.removeCallbacks(startLooper);
         Message message = Message.obtain(handler, startLooper);
         message.what = 1;
         message.obj = token;
         handler.sendMessageDelayed(message, awaitTime);
      }
   }

   @Override
   public void awake(Thread thread) {
      handler.post(startLooper);
   }
}
