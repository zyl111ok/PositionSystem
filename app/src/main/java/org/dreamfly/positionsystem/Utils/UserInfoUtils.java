package org.dreamfly.positionsystem.Utils;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by asus on 2015/1/17.
 * 管理个人信息的文件工具类
 */
public class UserInfoUtils {


    private File userInfoFile = null;

    private FileUitls mFileUitls;

    private FileOutputStream writeUserInfo;

    private FileInputStream readUserInfo;

    private String userInfoTag = "loginstate:unlogin#";//初始化的个人信息


    private Map<String, String> userInfoMap;

    /**
     * 构造函数，如果本地缓存的文件不存在就先创建
     */
    public UserInfoUtils(Context mContext) {
        this.mFileUitls = new FileUitls(mContext);
        File cachedir = this.mFileUitls.getCacheFileDir();
        if (cachedir != null) {
            this.userInfoFile = new File(cachedir, "userinfo.txt");
            if (!this.userInfoFile.exists()) {
                try {
                    this.userInfoFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.initialUserInfo();//没有文件的时候才写入未登录状态,没有就不写
            }
        } else {
            this.userInfoFile = null;
        }
    }

    /**
     * 这个函数是在默认没有本地文件的时候写入的文件是状态是没有登录的
     */
    private void initialUserInfo() {
        try {
            this.writeUserInfo = new FileOutputStream(this.userInfoFile);
            this.writeUserInfo.write(this.userInfoTag.getBytes());
            this.writeUserInfo.flush();
            this.writeUserInfo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 去读本地的文件,返回一个字符串,如果文件不存在,或者读取失误,直接返回空引用
     *
     * @return
     */
    private String getUserInfo() {
        StringBuffer mStrBuf = new StringBuffer();
        String tmpStr;
        try {
            if (this.userInfoFile != null) {//针对文件建立不成功的情况
                this.readUserInfo = new FileInputStream(this.userInfoFile);
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(this.readUserInfo));
                //字节流传化为字符流
                while ((tmpStr = bufReader.readLine()) != null) {
                    mStrBuf.append(tmpStr);
                }
                this.readUserInfo.close();
                bufReader.close();
            } else {
                return null;
            }
            return (mStrBuf.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 通过传递个HASHMAP来遍历得到MAP的所有键值对，并且写入文件,以“#”区分
     *
     * @param userInfo
     */
    public void updateUserInfo(Map<String, String> userInfo) {
        StringBuffer strBuf = new StringBuffer();
        Iterator iterator = userInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            strBuf.append(entry.getKey() + ":" + entry.getValue() + "#");
        }
        try {
            this.writeUserInfo = new FileOutputStream(this.userInfoFile);
            this.writeUserInfo.write(strBuf.toString().getBytes());
            this.writeUserInfo.flush();
            this.writeUserInfo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 验证是否登录的函数,如果并且将文件的字符串切割开来，作为成员空间的字符串数组
     *
     * @return
     */
    public boolean isLogin() {
        boolean islogin = false;
        String userinfo = this.getUserInfo();
        if (userinfo != null) {
            this.userInfoMap = this.buildUserInfoMap(userinfo);
            if (this.userInfoMap != null) {
                String loginstate = this.userInfoMap.get("loginstate");
                if (loginstate != null) {
                    if (loginstate.equals("login")) {
                        islogin = true;
                    } else if (loginstate.equals("unlogin")) {
                        islogin = false;
                    }
                }
            }
        }
        return (islogin);
    }

    /**
     * 返回类的成员的哈希表,如果数组为空，则返回空引用
     *
     * @return
     */
    public Map<String, String> getUserInfoMap() {
        String userInfo = this.getUserInfo();
        if (userInfo != null) {
            return (this.buildUserInfoMap(userInfo));
        }
        return (null);
    }

    /**
     * 注销登录状态,将文件改成未登录状态
     */
    public void clearUserInfo() {
        this.initialUserInfo();
        this.userInfoMap = null;
        //释放资源
    }

    /**
     * 从文件中得到的字符串来构建哈希表
     *
     * @param fileInfo
     * @return
     */
    private Map<String, String> buildUserInfoMap(String fileInfo) {
        String[] strArr = fileInfo.split("#");
        this.userInfoMap = new HashMap<String, String>();
        for (String tmpStr : strArr) {
            String tmpArr[] = tmpStr.split(":");
            this.userInfoMap.put(tmpArr[0], tmpArr[1]);
        }
        return (this.userInfoMap);
    }

    /**
     * 判断是否是管理者
     *
     * @return
     */
    public boolean isManager() {
        boolean ismanager = true;
        String userinfo = this.getUserInfo();
        if (userinfo != null) {
            this.userInfoMap = this.buildUserInfoMap(userinfo);
            if (this.userInfoMap != null) {
                String managerstate = this.userInfoMap.get("managerstate");
                if (managerstate != null) {
                    if (managerstate.equals("manager")) {
                        ismanager = true;
                    } else if (managerstate.equals("unmanager")) {
                        ismanager = false;
                    }
                }
            }
        }
        return (ismanager);
    }

    /**
     * 获取服务器给你的ID号码
     *
     * @return
     */
    public int getServerId() {
        int severId = -1;
        String userinfo = this.getUserInfo();
        if (userinfo != null) {
            Map<String, String> tmpMap = this.buildUserInfoMap(userinfo);
            String databaseId = tmpMap.get("userrID");
            if (databaseId != null) {
                severId = Integer.parseInt(databaseId);
            }
        }
        return (severId);
    }

    /**
     * 返回文件缓存保存的密码字符
     *
     * @return
     */
    public String getPassword() {
        String password = null;
        String userinfo = this.getUserInfo();
        if (userinfo != null) {
            Map<String, String> tmpMap = this.getUserInfoMap();
            password = tmpMap.get("password");
            if (password != null) {
                return (password);
            }

        }
        return (password);
    }

    /**
     * 获取用户帐号
     *
     * @return
     */
    public String getFamilyName() {
        String familyName = "null";
        String userinfo = this.getUserInfo();
        if (userinfo != null) {
            Map<String, String> tmpMap = this.buildUserInfoMap(userinfo);
            familyName = tmpMap.get("famliyName");
        }
        return (familyName);
    }

}
