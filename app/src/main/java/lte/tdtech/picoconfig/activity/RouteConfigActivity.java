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

import lte.tdtech.picoconfig.R;
import lte.tdtech.picoconfig.config.Config;
import lte.tdtech.picoconfig.util.MessageEvent;
import lte.tdtech.picoconfig.util.SshServerUtils;
import lte.tdtech.picoconfig.view.DialogThridUtils;

/**
 * Created by lWX453051 on 2019/5/5.
 */

public class RouteConfigActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "RouteConfigActivity";


    private EditText routeidentifier;
    private EditText destIp;
    private EditText destMask;
    private EditText nextJuIp;

    private String routeidentifier_str;
    private String destIp_str;
    private String destMask_str;
    private String nextJuIp_str;

    private Dialog dialog;
    private LinkedHashMap<String, String> routeconfigmap = new LinkedHashMap<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_config);
        EventBus.getDefault().register(this);
        initview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals(Config.RoutemodifiedEvent)) {
            if (dialog != null) {
                DialogThridUtils.closeshowWaitDialog(dialog);
            }
            Toast.makeText(this, getResources().getString(R.string.str_alreadymodified), Toast.LENGTH_LONG).show();
        } else if (messageEvent.getMessage().equals(Config.RoutequeryEvent)) {
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

    private final Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                getroutconfig();
            }
        }
    };

    private void initview() {
        routeidentifier = (EditText) findViewById(R.id.routeidentifier);
        destIp = (EditText) findViewById(R.id.destIp);
        destMask = (EditText) findViewById(R.id.destMask);
        nextJuIp = (EditText) findViewById(R.id.nextJuIp);
    }

    private void inittext() {
        routeidentifier_str = routeidentifier.getText().toString();
        destIp_str = destIp.getText().toString();
        destMask_str = destMask.getText().toString();
        nextJuIp_str = nextJuIp.getText().toString();
    }

    /**
     * 检测str是否可用
     */
    private boolean accountCheck() {
        return ((!TextUtils.isEmpty(routeidentifier_str)) && (!TextUtils.isEmpty(routeidentifier_str)) && (!TextUtils
                .isEmpty(routeidentifier_str)) && (!TextUtils.isEmpty(routeidentifier_str)));
    }

    private void routeconfigquery() {
        dialog = DialogThridUtils.showWaitDialog(this, false, false);
        final Thread routeconfigquerythread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (SshServerUtils.getsshserverinstance().isCmdExcuted()) {
                    routeconfigmap = SshServerUtils.getsshserverinstance().SSHExcmdquery(Config.Cmdrouteconfigquery,
                            Config.Sshquerycmd_LST_CELL, Config.RoutequeryEvent);
                    Message message = mhandler.obtainMessage();
                    message.what = 1;
                    mhandler.sendMessage(message);
                }
            }
        });
        routeconfigquerythread.setName("routeconfigquerythread");
//        routeconfigquerythread.setDaemon(true);
        routeconfigquerythread.start();
    }

    private void routeconfigmodify() {
        inittext();
//        final String modifyroute = "MOD ROUTE:routeNo=0,nextHopIp=\"10.1.1.4\";";
        if (accountCheck()) {
            final String modifyroute = "MOD ROUTE:routeNo=" + routeidentifier_str + "," + "destIpAddr=" + "\"" +
                    destIp_str + "\"" + "," + "destIpMask=" + "\"" + destMask_str + "\"" + "," + "nextHopIp=" + "\""
                    + nextJuIp_str + "\"" + ";";
            dialog = DialogThridUtils.showWaitDialog(this, false, false);
            final Thread routeconfigmodifythread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (SshServerUtils.getsshserverinstance().isCmdExcuted()) {
                        SshServerUtils.getsshserverinstance().SSHExcmdmodify(modifyroute, Config.RoutemodifiedEvent);
                    }
                }
            });
            routeconfigmodifythread.setName("routeconfigmodifythread");
//        routeconfigmodifythread.setDaemon(true);
            routeconfigmodifythread.start();
        } else {
            Toast.makeText(this, getString(R.string.str_plscheckstr), Toast.LENGTH_LONG).show();
        }

    }

    private void getroutconfig() {
        if ((routeconfigmap != null) && !(SshServerUtils.getsshserverinstance().isContainChinese(routeconfigmap))) {
            routeidentifier.setText(routeconfigmap.get("Route No."));
            destIp.setText(routeconfigmap.get("Destination IP Address"));
            destMask.setText(routeconfigmap.get("Destination IP Mask"));
            nextJuIp.setText(routeconfigmap.get("Next Hop IP Address"));
        } else if ((routeconfigmap != null) && (SshServerUtils.getsshserverinstance().isContainChinese
                (routeconfigmap))) {
            routeidentifier.setText(routeconfigmap.get(Config.Routeid_cn));
            destIp.setText(routeconfigmap.get(Config.Restiipaddr_cn));
            destMask.setText(routeconfigmap.get(Config.Destimask_cn));
            nextJuIp.setText(routeconfigmap.get(Config.Nexthopip_cn));
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.routeconfigquery:
                new AlertDialog.Builder(this).setMessage(getString(R.string.str_confirmquery)).setPositiveButton
                        (getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        routeconfigquery();
                    }
                }).setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.routeconfigmodify:
                new AlertDialog.Builder(this).setMessage(getString(R.string.str_confirmmodified)).setPositiveButton
                        (getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        routeconfigmodify();
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

    @Override
    protected void onPause() {
        super.onPause();
    }
}
