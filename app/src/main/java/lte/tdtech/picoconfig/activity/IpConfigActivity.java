package lte.tdtech.picoconfig.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lte.tdtech.picoconfig.R;
import lte.tdtech.picoconfig.config.Config;
import lte.tdtech.picoconfig.util.MessageEvent;
import lte.tdtech.picoconfig.util.SshServerUtils;
import lte.tdtech.picoconfig.view.DialogThridUtils;

/**
 * Created by lWX453051 on 2019/5/5.
 */

public class IpConfigActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "IpConfigActivity";
    private EditText currentip;
    private EditText currentmask;

    private String currentip_str;
    private String currentmask_str;

    private LinkedHashMap<String, String> ipconfigmap = new LinkedHashMap<>();
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_config);
        EventBus.getDefault().register(this);
        initview();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals(Config.IpmodifiedEvent)) {
            if (dialog != null) {
                DialogThridUtils.closeshowWaitDialog(dialog);
            }
            Toast.makeText(this, getResources().getString(R.string.str_alreadymodified), Toast.LENGTH_LONG).show();
        } else if (messageEvent.getMessage().equals(Config.IpqueryEvent)) {
            if (dialog != null) {
                DialogThridUtils.closeshowWaitDialog(dialog);
            }
        } else if (messageEvent.getMessage().equals(Config.Userdisconnected)) {
            if (dialog != null) {
                DialogThridUtils.closeshowWaitDialog(dialog);
            }
            DialogThridUtils.showdisconnectdialog(getResources().getString(R.string.str_userdisconnected), this);
        } else if (messageEvent.getMessage().equals(Config.UsernotAuthenticated)) {
            if (dialog != null) {
                DialogThridUtils.closeshowWaitDialog(dialog);
            }
            DialogThridUtils.showdisconnectdialog(getResources().getString(R.string.str_usernotAuthenticated), this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initview() {
        currentip = (EditText) findViewById(R.id.currentip);
        currentmask = (EditText) findViewById(R.id.currentmask);
    }

    private void inittext() {
        currentip_str = currentip.getText().toString();
        currentmask_str = currentmask.getText().toString();
    }

    /**
     * 检测str是否可用
     */
    private boolean accountCheck() {
//        inittext();
        return ((!TextUtils.isEmpty(currentip_str)) && (!TextUtils.isEmpty(currentmask_str)));
    }

    private final Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                getipconfig();
            }
        }
    };


    private void ipconfigquery() {
        dialog = DialogThridUtils.showWaitDialog(this, false, false);
        final Thread routeconfigquerythread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (SshServerUtils.getsshserverinstance().isCmdExcuted()) {
                    ipconfigmap = SshServerUtils.getsshserverinstance().SSHExcmdquery(Config.Cmdipconfigquery, Config
                            .Sshquerycmd_LST_IPINTERFACE, Config.IpqueryEvent);
                    Message message = mhandler.obtainMessage();
                    message.what = 1;
                    mhandler.sendMessage(message);
                }
            }
        });
        routeconfigquerythread.setName("ipconfigquerythread");
//        routeconfigquerythread.setDaemon(true);
        routeconfigquerythread.start();
    }

    private void ipconfigmodify() {
        inittext();
//        final String modifyip = "MOD IPINTERFACE: ipInterfaceNo=2,ipAddress=\"10.1.1.9\";";
        if (accountCheck()) {
            final String Modifyip = "MOD IPINTERFACE:ipInterfaceNo=2" + "," + "ipAddress=" + "\"" + currentip_str +
                    "\"" + "," + "ipMask=" + "\"" + currentmask_str + "\"" + ";";
            dialog = DialogThridUtils.showWaitDialog(this, false, false);
            final Thread routeconfigmodifythread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (SshServerUtils.getsshserverinstance().isCmdExcuted()) {
                        SshServerUtils.getsshserverinstance().SSHExcmdmodify(Modifyip, Config.IpmodifiedEvent);
                    }
                }
            });
            routeconfigmodifythread.setName("ipconfigmodifythread");
//        routeconfigmodifythread.setDaemon(true);
            routeconfigmodifythread.start();
        } else {
            Toast.makeText(this, getString(R.string.str_plscheckstr), Toast.LENGTH_LONG).show();
        }

    }


    private void getipconfig() {
        if (ipconfigmap != null) {
            Set<Map.Entry<String, String>> ms = ipconfigmap.entrySet();
            for (Map.Entry entry : ms) {
                if (entry.getKey() != null) {
                    currentip.setText(entry.getKey().toString());
                    currentmask.setText(entry.getValue().toString());
                }
            }
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ipconfigquery:
                new AlertDialog.Builder(this).setMessage(getString(R.string.str_confirmquery)).setPositiveButton
                        (getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ipconfigquery();
                    }
                }).setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.ipconfigmodify:
                new AlertDialog.Builder(this).setMessage(getString(R.string.str_confirmmodified)).setPositiveButton
                        (getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ipconfigmodify();

                    }
                }).setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }

    }


}
