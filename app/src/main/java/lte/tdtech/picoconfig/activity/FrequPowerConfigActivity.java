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

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import lte.tdtech.picoconfig.R;
import lte.tdtech.picoconfig.config.Config;
import lte.tdtech.picoconfig.util.MessageEvent;
import lte.tdtech.picoconfig.util.SshServerUtils;
import lte.tdtech.picoconfig.view.DialogThridUtils;

/**
 * Created by lWX453051 on 2019/5/5.
 */

public class FrequPowerConfigActivity extends Activity implements View.OnClickListener {


    private EditText frepowercellid;
    private EditText frepowercenterfre;
    private EditText frepowerband;
    private EditText frepowertotalpower;

    private String frepowercellid_str;
    private String frepowercenterfre_str;
    private String frepowerband_str;
    private String frepowertotalpower_str;

    private String PB;
    private String E_RS;
    private String PA;
    private String Total_Power;
    private static final String TAG = "FrequPowerConfigActivity";
    private Dialog dialog;
    private LinkedHashMap<String, String> frequpowermap = new LinkedHashMap<>();


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frequpower_config);
        EventBus.getDefault().register(this);
        initview();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoonEvent(MessageEvent messageEvent) {
        if (messageEvent.getMessage().equals(Config.FremodifiedEvent)) {
            if (dialog != null) {
                DialogThridUtils.closeshowWaitDialog(dialog);
            }
            Toast.makeText(this, getResources().getString(R.string.str_alreadymodified), Toast.LENGTH_LONG).show();
        } else if (messageEvent.getMessage().equals(Config.FrequeryEvent)) {
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
        frepowercellid = (EditText) findViewById(R.id.frepowercellid);
        frepowercenterfre = (EditText) findViewById(R.id.frepowercenterfre);
        frepowerband = (EditText) findViewById(R.id.frepowerband);
        frepowertotalpower = (EditText) findViewById(R.id.frepowertopower);
    }

    private void initext() {
        frepowercellid_str = frepowercellid.getText().toString();
        frepowerband_str = frepowerband.getText().toString();
        frepowercenterfre_str = frepowercenterfre.getText().toString();
        frepowertotalpower_str = frepowertotalpower.getText().toString();
    }

    /**
     * 检测str是否可用
     */
    private boolean accountCheck() {
        boolean strnotnull = ((!TextUtils.isEmpty(frepowercellid_str)) && (!TextUtils.isEmpty(frepowerband_str)) &&
                (!TextUtils.isEmpty(frepowercenterfre_str)));
        boolean bwlegal = (frepowerband_str.equals("5")) || (frepowerband_str.equals("10")) || (frepowerband_str
                .equals("20"));
        double frepowertotalpower_strtoint = Double.parseDouble(frepowertotalpower_str);
        boolean totalpowerlegal = (frepowertotalpower_strtoint > 0.0) && (frepowertotalpower_strtoint < 20.0);
        if (strnotnull && bwlegal && totalpowerlegal) {
            return true;
        }
        return false;
    }

    private String tranDlEarfcn(String str) {
        if (str != null) {
            return String.valueOf(Integer.parseInt(frepowercenterfre_str) - 5660 + 19260);
        }
        return "";
    }

    private String tranDlBandWidthToCmd(String str) {
        if (str != null) {
            if (str.equals("5")) {
                return "CELL_BW_N25";
            } else if (str.equals("10")) {
                return "CELL_BW_N50";
            } else if (str.equals("20")) {
                return "CELL_BW_N100";
            }
        }
        return "";
    }

    private final Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                getfrequpower();
            }
        }
    };


    private void frequpowerquery() {
        dialog = DialogThridUtils.showWaitDialog(this, false, false);
        final Thread frequpowerquerythread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (SshServerUtils.getsshserverinstance().isCmdExcuted()) {
                    frequpowermap = SshServerUtils.getsshserverinstance().SSHExcmdquery(Config.Cmdfrepowerquerycmd,
                            Config.Sshquerycmd_LST_CELL, Config.FrequeryEvent);
                    Message message = mhandler.obtainMessage();
                    message.what = 1;
                    mhandler.sendMessage(message);
                }
            }
        });
        frequpowerquerythread.setName("frequpowerquerythread");
//        routeconfigquerythread.setDaemon(true);
        frequpowerquerythread.start();
    }

    private void frequpowermodify() {
        initext();
        if (accountCheck()) {
            String E_RS = caculattransmittingpower(frepowertotalpower_str, PB, PA);
            final String MODIFIED_FREPOWER = "MOD CELL:LocalCellId=" + frepowercellid_str + "," + "DlBandWidth=" +
                    tranDlBandWidthToCmd(frepowerband_str) + "," + "UlBandWidth=" + tranDlBandWidthToCmd
                    (frepowerband_str) + "," + "DlEarfcn=" + tranDlEarfcn(frepowercenterfre_str) + ";" + "MOD " +
                    "PDSCHCFG:LocalCellId=" + frepowercellid_str + "," + "ReferenceSignalPwr=" + E_RS + ";";
            dialog = DialogThridUtils.showWaitDialog(this, false, false);
            final Thread frequpowermodifythread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (SshServerUtils.getsshserverinstance().isCmdExcuted()) {
                        SshServerUtils.getsshserverinstance().SSHExcmdmodify(MODIFIED_FREPOWER, Config
                                .FremodifiedEvent);
                    }
                }
            });
            frequpowermodifythread.setName("frequpowermodifythread");
//        routeconfigmodifythread.setDaemon(true);
            frequpowermodifythread.start();
        } else {
            Toast.makeText(this, getString(R.string.str_plscheckstr), Toast.LENGTH_LONG).show();
        }

    }


    private void getfrequpower() {
        if (frequpowermap != null) {
            Log.d(TAG, "liujin--getfrequpower-------->>>>");
            if (!SshServerUtils.getsshserverinstance().isContainChinese(frequpowermap)) {
                frepowercellid.setText(frequpowermap.get("Local Cell ID"));
                //查询时从MML中获取DlEarfcn，显示时频段转化为：（EARFCN-19260）+5660
                frepowercenterfre.setText(String.valueOf(Integer.parseInt(frequpowermap.get("Downlink EARFCN")) -
                        19260 + 5660));
                frepowerband.setText(frequpowermap.get("Downlink bandwidth").substring(0, frequpowermap.get
                        ("Downlink" + " " + "bandwidth").length() - 1));
                E_RS = frequpowermap.get("Reference signal power(0.1dBm)");
                PA = frequpowermap.get("PA for even power distribution(dB)");
                PB = frequpowermap.get("PB");
            } else if (SshServerUtils.getsshserverinstance().isContainChinese(frequpowermap)) {
                frepowercellid.setText(frequpowermap.get(Config.Localid_cn));
                frepowercenterfre.setText(String.valueOf(Integer.parseInt(frequpowermap.get(Config.Dlearfcn_cn)) -
                        19260 + 5660));
                frepowerband.setText(frequpowermap.get(Config.Dlbandwidth_cn).substring(0, frequpowermap.get(Config
                        .Dlbandwidth_cn).length() - 1));
                PA = frequpowermap.get(Config.Pa_cn);
                E_RS = frequpowermap.get(Config.E_rs_cn);
                PB = frequpowermap.get("PB");
            }

            if (PA != null) {
                String[] PA_result = PA.split(" ");
                PA = PA_result[0];
            } else {
                PA = "";
            }
            Total_Power = caculatepower(PA, PB, E_RS);
            frepowertotalpower.setText(Total_Power);
        }
    }

    private String caculattransmittingpower(String totalpower, String pb, String pa) {
//        E_RS=100*log10 (W/2/ ( (10/14)*N*10^(PA/10)+ (4/14)/6*N + P *(4/14)*(4/6)*N*10^(PA/10) ))
        double result = 0;
        double N = 1200;
        double P = 1;

        double d_PA = Double.parseDouble(pa);
        double d_totalpower = Double.parseDouble(totalpower);

        if (frepowerband_str != null) {
            Log.d(TAG, "liujin--caculattransmittingpower---frepowerband_str----->>>>" + frepowerband_str);
            if (frepowerband_str.equals("20")) {
                N = 1200;
            } else if (frepowerband_str.equals("10")) {
                N = 600;
            } else if (frepowerband_str.equals("5")) {
                N = 300;
            }
        }

        if (pb != null) {
            if (pb.equals("0")) {
                P = 5 / 4;
            } else if (pb.equals("1")) {
                P = 1;
            } else if (pb.equals("2")) {
                P = 3 / 4;
            } else if (PB.equals("3")) {
                P = 1 / 2;
            }
        }
        result = 100 * Math.log10(d_totalpower * 1000 / 2.0 / ((10.0 / 14.0) * N * Math.pow(10.0, d_PA / 10.0) + (
                (4.0 / 14.0) / 6.0) * N + P * (4.0 / 14.0) * (4.0 / 6.0) * N * Math.pow(10.0, d_PA / 10.0)));
        DecimalFormat df = new DecimalFormat("#.00");
        String strresult = df.format(result);
        return strresult;
    }

    private String caculatepower(String pa, String pb, String e_rs) {
        double result = 10;
        double N = 1200;
        double P = 1;
        double d_PA = Double.parseDouble(pa);
        double d_E_RS = Double.parseDouble(e_rs);

        if (frepowerband_str != null) {
            if (frepowerband_str.equals("20")) {
                N = 1200;
            } else if (frepowerband_str.equals("10")) {
                N = 600;
            } else if (frepowerband_str.equals("5")) {
                N = 300;
            }
        }

        if (pb != null) {
            if (pb.equals("0")) {
                P = 5 / 4;
            } else if (pb.equals("1")) {
                P = 1;
            } else if (pb.equals("2")) {
                P = 3 / 4;
            } else if (PB.equals("3")) {
                P = 1 / 2;
            }
        }
        double resulttemp1 = (10.0 / 14.0) * N * Math.pow(10.0, d_PA / 10.0) + ((4.0 / 14.0) / 6.0) * N + P * (4.0 /
                14.0) * (4.0 / 6.0) * N * Math.pow(10, d_PA / 10);
        result = resulttemp1 * Math.pow(10, d_E_RS / 100) * 2 / 1000;
        DecimalFormat df = new DecimalFormat("#.00");
        String strresult = df.format(result);
        return strresult;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.freqpowquery:
                new AlertDialog.Builder(this).setMessage(getString(R.string.str_confirmquery)).setPositiveButton
                        (getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        frequpowerquery();
                    }
                }).setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.freqpowmodify:
                new AlertDialog.Builder(this).setMessage(getString(R.string.str_confirmmodified)).setPositiveButton
                        (getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        frequpowermodify();
                    }
                }).setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            default:
                break;

        }
    }
}
