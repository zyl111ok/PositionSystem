package org.dreamfly.positionsystem.Activity;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.dreamfly.positionsystem.CommonParameter.ComParameter;
import org.dreamfly.positionsystem.Custom.DefineDialog;
import org.dreamfly.positionsystem.R;
import org.dreamfly.positionsystem.Thread.BaseThread;
import org.dreamfly.positionsystem.Thread.FirstLoginRequestThread;
import org.dreamfly.positionsystem.Utils.CurrentInformationUtils;
import org.dreamfly.positionsystem.Utils.FileUitls;
import org.dreamfly.positionsystem.Utils.ToastUtils;
import org.dreamfly.positionsystem.Utils.UserInfoUtils;


import android.app.Activity;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengyl on 15-1-13.
 * 登陆界面Activity类
 */
public class LoginActivity extends Activity {

    private TextView txtLoginactivityRegister;
    private Button btnLoginactivityLogin;
    private EditText edittextLoginactivityUsername;
    private EditText editextLoginactivityPassword;
    private DefineDialog mIsManagerDialog;

    private CurrentInformationUtils mInformation = new CurrentInformationUtils(this);
    private BaseThread loginReuquestThread;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.login_layout);
        this.initial();


    }

    /**
     * @deprecated 初始化函数
     * 对组件进行视图的ID绑定
     */
    private void initial() {
        this.txtLoginactivityRegister = (TextView)
                findViewById(R.id.txt_loginactivity_register);
        this.btnLoginactivityLogin = (Button)
                findViewById(R.id.btn_loginactivity_login);
        this.edittextLoginactivityUsername = (EditText)
                findViewById(R.id.edtext_loginactivity_username);
        this.editextLoginactivityPassword = (EditText)
                findViewById(R.id.edtext_loginactivity_password);
        this.bindListener();


    }

    /**
     * 注册组件的监听器
     */
    private void bindListener() {

        //跳入注册界面的事件监听
        this.txtLoginactivityRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent registerIntent = new Intent();
                registerIntent.setClass(getApplicationContext(), RegistActivity.class
                );
                startActivity(registerIntent);


            }
        });
        this.btnLoginactivityLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showIsManagerDialog();
            }
        });
    }

    /**
     * 提示未登录用户的在登录成功的情况下选择管理者和被管理者对话框
     */
    private void showIsManagerDialog() {
        this.mIsManagerDialog = new DefineDialog(LoginActivity.this).buiider(false).
                setTitle("是否成为管理者").setPosBtnTxt("是").setNegBtnTxt("否")
                .setNegBtnClickListenr(regulatorClickListener).setPosBtnClickListener(managerClickListener)
                .setDefineDialogCanceable(false).show();
    }

    /**
     * 成为管理者的话,写入本地文件是管理这身份
     */
    private View.OnClickListener managerClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            writeUserInfo("manager",mInformation,"2","Tree");
            mIsManagerDialog.dismiss();
            sendLoginInfoToServer(true);
            Intent in = new Intent(LoginActivity.this, ManagerActivity.class);
            startActivity(in);
            finish();
        }
    };
    /**
     * 成为被管理者的话,写入本地文件是被管理者身份
     */
    private View.OnClickListener regulatorClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            writeUserInfo("unmanager",mInformation,"2","Tree");
            mIsManagerDialog.dismiss();
            sendLoginInfoToServer(false);
            Intent in = new Intent(LoginActivity.this, RegulatorActivity.class);
            startActivity(in);
            finish();
        }
    };

    /**
     * 这是在登录界面中，按钮所确定的本地信息的缓存,
     * 如果选择是的话,则写入被身份是管理者
     * 如果选择否的话，则写入身份是管理者
     *
     * @param isManager
     * @param mInformation 得到的本机信息
     */
    private void writeUserInfo(String isManager,CurrentInformationUtils mInformation
       ,String userId,String userName) {
        UserInfoUtils mUserInfoUitls = new UserInfoUtils(LoginActivity.this);
        HashMap<String, String> hashmap = new HashMap<String, String>();
        hashmap.put("loginstate", "login");
        //记录登录状态
        hashmap.put("managerstate", isManager);
        //记录是否是管理者
        hashmap.put("userrID", userId);
        //记录服务器中数据库的主键的数值
        hashmap.put("famliyName",userName);
        //记录用户登录的帐号名字
        hashmap.put("devName", mInformation.getCurrentDeviceName());
        //记录本机的设备名字
        mUserInfoUitls.updateUserInfo(hashmap);
    }

    private void sendLoginInfoToServer(boolean isManager)
    {
          this.loginReuquestThread=new FirstLoginRequestThread(mHandler,"firstloginstate");
           String requestURL=ComParameter.HOST+"";
          this.loginReuquestThread.setRequestPrepare(requestURL,this.prepareLoginParams(isManager));
          this.loginReuquestThread.start();
    }
    private Map prepareLoginParams(boolean isManager)
    {
            Map<String,String> params=new HashMap<String,String>();
            params.put("username",edittextLoginactivityUsername.getText().toString());
            params.put("password",editextLoginactivityPassword.getText().toString());
            if(isManager)
            {
                  params.put("type","admin");
            }else{
                  params.put("type","user");
            }
            params.put("devId", this.mInformation.getDeviceId());
            params.put("devName",this.mInformation.getCurrentDeviceName());
           return(params);

    }

    private Handler mHandler=new Handler(Looper.getMainLooper())
    {
        public void handleMessage(Message msg) {
               if(msg.getData().getInt("firstloginstate")== ComParameter.STATE_RIGHT)
               {
                      this.dealFirstLogintMessage();
               }else if(msg.getData().getInt("firstloginstate")==ComParameter.STATE_ERROR){
                      ToastUtils.showToast(getApplicationContext(),ComParameter.ERRORINFO);
               }
        }

        private void dealFirstLogintMessage()
        {
                Map<String,String> resultMap=loginReuquestThread.getResultMap();
                String loginstate=resultMap.get("loginstate");
                if(loginstate!=null)
                {
                      if(loginstate.equals("login"))
                      {
                         writeUserInfo(resultMap.get("type"),
                                 mInformation,
                                 resultMap.get("dataBaseId"),
                                 edittextLoginactivityUsername.getText().toString());
                         this.dealAfterLogin(resultMap.get("type"));

                      }else if(loginstate.equals("unlogin"))
                      {
                            ToastUtils.showToast(getApplicationContext(),resultMap.get("failReason")+"");
                      }
                }
        }

        private void dealAfterLogin(String type)
        {
            Intent in=null;
            if(type.equals("manager"))
            {
                in=new Intent().setClass(LoginActivity.this,ManagerActivity.class);
                startActivity(in);
            }else if(type.equals("unmanager"))
            {
                 in=new Intent().setClass(LoginActivity.this,RegulatorActivity.class);
                 startActivity(in);
            }
        }
    };
}

