package org.dreamfly.positionsystem.Thread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.dreamfly.positionsystem.CommonParameter.ComParameter;
import org.dreamfly.positionsystem.Database.DefinedShared;
import org.dreamfly.positionsystem.Utils.HttpUtils;
import org.dreamfly.positionsystem.Utils.UserInfoUtils;

import java.util.HashMap;
import java.util.Map;
import java.lang.InterruptedException;
/**
 * Created by lzw on 2015/3/5.
 * 轮训网络模块的线程实现
 */
public class QueryThread extends  Thread {

            private Context mContext;
            private Map<String,String> params;
            private String URLrequest;
            private Handler mHandler;
            private Message msg=new Message();
            private Bundle bd=new Bundle();
            private boolean isSendMyLocation;


            public QueryThread(Context mContext,Handler mHandler){
                   this.mContext=mContext;
                   this.mHandler=mHandler;
                   this.params=new HashMap<String,String>();
                   this.URLrequest= ComParameter.HOST+"query.action";
                   this.isSendMyLocation=false;
                   UserInfoUtils userIdUtils=new UserInfoUtils(mContext);
                   this.params.put("id",userIdUtils.getServerId()+"");
                   Log.i("lzw","轮询线程启动中");
            }

            public void  run(){
                while (true){
                    this.prepareParams(this.isSendMyLocation);
                    try{
                        String reponseStr= HttpUtils.requestHttpServer(this.URLrequest,
                                this.params,ComParameter.ENCODE_UTF_8,ComParameter.ENCODE_UTF_8);
                        this.dealResponseStr(reponseStr);
                        Log.i("lzw",reponseStr);
                        this.sleep(5 * 1000);
                    }catch(InterruptedException e) {
                           e.printStackTrace();
                    }

                }

            }

            /**
             * 根据状态来选择是否上传地理位置
             * @param isSendLocation
             */
            private void prepareParams(boolean isSendLocation){
                  if(isSendLocation){
                      //需要获取地理位位置的时候再去获取，不然就不去向服务器提交

                      this.isSendMyLocation=false;
                      DefinedShared mdata=new DefinedShared(this.mContext);
                      String longtitud=mdata.getString("longitude","longitude");
                      String latitude=mdata.getString("latitude","latitude");
                      params.put("location",longtitud+" "+latitude);
                  }
            }

            /**
             * 处理服务器返回的字符串的情况
             * @param reponseStr
             */
            private void dealResponseStr(String reponseStr){
                if(reponseStr!=null)
                  if(reponseStr.equals("n"))
                  {
                    Log.i("lzw",reponseStr);
                  }else{
                     Log.i("lzw",reponseStr);
                     String[] strArr=reponseStr.split(":");
                     if(strArr[0].equals("call"))
                     {
                          this.sendCallMsgToService(strArr[1]);
                     }else if(strArr[0].equals("location")){

                          callServiceGetLocation();
                          this.isSendMyLocation=true;

                         //改变向服务器发送地理位置的状态标志
                         if(strArr.length>1) {
                             if (!(strArr[1].equals(" ") || strArr[1].equals("null"))) {
                                 sendLocationToService();
                             }
                         }
                     }
                  }
            }

    /**
     * 向queryservice发送获取本地位置请求
     */
    private void callServiceGetLocation() {
        bd.putInt("ACTION", ComParameter.ACTION_LOCATION);
        msg.setData(bd);
        this.mHandler.sendMessage(msg);
    }

    /**
     * 得到将对方的地理位置
     */
    private void sendLocationToService(){
        bd.putString("getlocation",ComParameter.USER_LOCATION);
        msg.setData(bd);
        this.mHandler.sendMessage(msg);
    }

    /**
     * 向QuertSerivce发送拨打电话的请求
     *
     * @param callNum
     */
    private void sendCallMsgToService(String callNum) {
        if (callNum != null) {

            bd.putInt("ACTION", ComParameter.ACTION_CALLPHONE);
            bd.putString("callNum", callNum);
            msg.setData(bd);
            this.mHandler.sendMessage(msg);
        }
    }


}
