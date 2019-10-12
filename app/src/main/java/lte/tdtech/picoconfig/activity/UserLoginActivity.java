package lte.tdtech.picoconfig.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import lte.tdtech.picoconfig.R;
import lte.tdtech.picoconfig.config.Config;
import lte.tdtech.picoconfig.util.MessageEvent;
import lte.tdtech.picoconfig.util.SshServerUtils;
import lte.tdtech.picoconfig.view.DialogThridUtils;

/**
 * Created by lWX453051 on 2019/5/5.
 */

public class UserLoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "UserLoginActivity";
    private EditText loginipaddress;
    private EditText loginport;
    private EditText loginuserid;
    private EditText loginpwd;

    private static String loginipaddress_str;
    private static String loginport_str;
    private static String loginuserid_str;
    private static String loginpwd_str;
    private Dialog dialog;
    //    private LoginTask task;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    private Context mContext;
    // 存储用户名、密码
    SharedPreferences shared, shareuserlogin;
    SharedPreferences.Editor editor, editoruserlogin;
    //保存登录状态
    AlertDialog.Builder disconnectdialogbuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userlogin);
        EventBus.getDefault().register(this);
        mContext = this;
        initview();
        shared = getSharedPreferences("userlogindata", MODE_PRIVATE);
        editor = shared.edit();
        setuserlogintext();
    }


    private void setuserlogintext() {
        if (shared.getString("loginip", "") != null) {
            loginipaddress.setText(shared.getString("loginip", "").toString());
        }
        if (shared.getString("loginport", "") != null) {
            loginport.setText(shared.getString("loginport", "").toString());
        }
        if (shared.getString("loginuserid", "") != null) {
            loginuserid.setText(shared.getString("loginuserid", "").toString());
        }
        if (shared.getString("loginpwd", "") != null) {
            loginpwd.setText(shared.getString("loginpwd", "").toString());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals(Config.UserLoginEvent)) {
            shareuserlogin = getSharedPreferences("firstloginfile", MODE_PRIVATE);
            editoruserlogin = shareuserlogin.edit();
            editoruserlogin.putBoolean("isfirstlogin", false);
            editoruserlogin.commit();
            writesharepre();
            DialogThridUtils.closeshowWaitDialog(dialog);
            Intent intent = new Intent(this, ListConfigActivity.class);
            startActivity(intent);
            this.finish();
        } else if (messageEvent.getMessage().equals(Config.Userdisconnected)) {
            DialogThridUtils.closeshowWaitDialog(dialog);
            DialogThridUtils.showdisconnectdialog(getResources().getString(R.string.str_userdisconnected), this);
        } else if (messageEvent.getMessage().equals(Config.UsernotAuthenticated)) {
            DialogThridUtils.closeshowWaitDialog(dialog);
            DialogThridUtils.showdisconnectdialog(getResources().getString(R.string.str_usernotAuthenticated), this);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initview() {
        loginipaddress = (EditText) findViewById(R.id.loginipaddress);
        loginport = (EditText) findViewById(R.id.loginport);
        loginuserid = (EditText) findViewById(R.id.loginuserid);
        loginpwd = (EditText) findViewById(R.id.loginpwd);
    }

    private void inittext() {
        loginipaddress_str = loginipaddress.getText().toString();
        loginport_str = loginport.getText().toString();
        loginuserid_str = loginuserid.getText().toString();
        loginpwd_str = loginpwd.getText().toString();
    }

    private void writesharepre() {
        editor.putString("loginip", loginipaddress_str);
        editor.putString("loginport", loginport_str);
        editor.putString("loginuserid", loginuserid_str);
        editor.putString("loginpwd", loginpwd_str);
        editor.commit();
    }

    /**
     * 检测str是否可用
     */
    private boolean accountCheck() {

        return ((!TextUtils.isEmpty(loginipaddress_str)) && (!TextUtils.isEmpty(loginport_str)) && (!TextUtils
                .isEmpty(loginuserid_str)) && (!TextUtils.isEmpty(loginpwd_str)));
    }

    private void startlogin() {
        inittext();
        if (accountCheck()) {
            dialog = DialogThridUtils.showWaitDialog(this, false, false);
            Thread connectionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    SshServerUtils.getsshserverinstance().SSHConnect(loginipaddress_str, loginuserid_str,
                            loginpwd_str, Integer.parseInt(loginport_str), true);
                }
            });
            connectionThread.setName("Connection");
//            connectionThread.setDaemon(true);
            connectionThread.start();
        } else {
            Toast.makeText(this, getString(R.string.str_plscheckstr), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 模拟back按键
     */
    private void onback() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Instrumentation instrumentation = new Instrumentation();
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.logincon: {
                startlogin();
                break;
            }
            case R.id.logincancel: {
                onback();
            }
            default:
                break;
        }

    }

}
