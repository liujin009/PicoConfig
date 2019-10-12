package lte.tdtech.picoconfig.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.trilead.ssh2.ConnectionMonitor;
import com.trilead.ssh2.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.StreamGobbler;

import org.greenrobot.eventbus.EventBus;

import lte.tdtech.picoconfig.PicApplication;
import lte.tdtech.picoconfig.config.Config;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by lWX453051 on 2019/5/5.
 */

public class SshServerUtils implements ConnectionMonitor {
    private static final String TAG = "SshServerUtils";
    private volatile static SshServerUtils sshServerinstance;
    private Session sess;
    private Connection conn;
    public volatile boolean connected = false;
    private boolean isAuthenticated;

    private OutputStream stdin;
    private String command;
    private volatile boolean iscmdexcuted = true;
    private Context mContext;

    // 用户名、密码
    SharedPreferences shareuserlogin;
    SharedPreferences.Editor editoruserlogin;

    String loginip, loginport, loginuserid, loginpwd;

    private SshServerUtils() {
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static SshServerUtils getsshserverinstance() {
        if (sshServerinstance == null) {
            synchronized (SshServerUtils.class) {
                if (null == sshServerinstance) {
                    sshServerinstance = new SshServerUtils();
                }
            }
        }
        return sshServerinstance;
    }

    private void getsharedata() {
        shareuserlogin = PicApplication.getPicContext().getSharedPreferences("userlogindata", MODE_PRIVATE);
        editoruserlogin = shareuserlogin.edit();
        if (shareuserlogin.getString("loginip", "") != null) {
            loginip = shareuserlogin.getString("loginip", "").toString();
        }
        if (shareuserlogin.getString("loginport", "") != null) {
            loginport = (shareuserlogin.getString("loginport", "").toString());
        }
        if (shareuserlogin.getString("loginuserid", "") != null) {
            loginuserid = (shareuserlogin.getString("loginuserid", "").toString());
        }
        if (shareuserlogin.getString("loginpwd", "") != null) {
            loginpwd = (shareuserlogin.getString("loginpwd", "").toString());
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Log.d(TAG, "connectionLost");

    }

    public synchronized void SSHConnect(String host, String username, String passwd, int port, boolean isfirstlogin) {
        try {
            closessh();
            conn = new Connection(host, port);
            conn.connect();
            //conn.addConnectionMonitor((ConnectionMonitor) this);
            isAuthenticated = conn.authenticateWithPassword(username, passwd);
            if (isAuthenticated) {
                connected = true;
                if (isfirstlogin) {
                    EventBus.getDefault().post(new MessageEvent(Config.UserLoginEvent));
                }
            } else {
                Log.d(TAG, "isAuthenticated-false");
                EventBus.getDefault().post(new MessageEvent(Config.UsernotAuthenticated));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "SSHConnect:" + e);
            EventBus.getDefault().post(new MessageEvent(Config.Userdisconnected));
        }
    }


    public boolean isSSHConnected() {
        return connected;
    }

    public boolean isCmdExcuted() {
        Log.d(TAG, "iscmdexcuted" + iscmdexcuted);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return iscmdexcuted;
    }

    /**
     * 去掉首位全角空格
     *
     * @param str
     * @return
     */
    private String trimadvanced(String str) {
        str = str.trim();
        if (str.startsWith("  ")) {
            str = str.substring(1, str.length()).trim();
        }
        if (str.endsWith("  ")) {
            str = str.substring(0, str.length() - 1).trim();
        }
        return str;
    }

    /**
     * 字符串转换为Ascii
     *
     * @param value
     * @return
     */
    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

    /**
     * string contains chinese
     *
     * @param str
     * @return
     */

    public boolean isContainChinese(LinkedHashMap<String, String> linkedhashmap) {
        if (linkedhashmap != null) {
            Set<Map.Entry<String, String>> ms = linkedhashmap.entrySet();
            for (Map.Entry entry : ms) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                if ((key.matches("[\\u4E00-\\u9FA5]+")) || value.matches("[\\u4E00-\\u9FA5]+")) {
                    Log.d(TAG, "isContainChinese");
                    return true;
                }
            }
        }
        return false;
    }

    //根据iscmdexcuted的值弹dialog不可取消
    public synchronized void SSHExcmdmodify(String command, String eventtype) {
        iscmdexcuted = false;
        this.command = command;
        Log.d(TAG, "command:" + this.command);
        try {
            getsharedata();
            SSHConnect(loginip, loginuserid, loginpwd, Integer.parseInt(loginport), false);
            if (isSSHConnected()) {
                sess = conn.openSession();
                sess.startShell();
                stdin = sess.getStdin();
                writedata();
                flushdata();
                InputStream stdout = new StreamGobbler(sess.getStdout());
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout, "GBK"));
                PrintWriter out = new PrintWriter(stdin);
                Thread.sleep(1000);
                out.close();
                br.close();
                closessh();
                iscmdexcuted = true;
                EventBus.getDefault().post(new MessageEvent(eventtype));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //根据iscmdexcuted的值弹dialog不可取消
    public synchronized LinkedHashMap<String, String> SSHExcmdquery(String command, int commandtype, String eventtype) {
        iscmdexcuted = false;
        this.command = command;
        Log.d(TAG, "command:" + this.command);
        try {
            getsharedata();
            SSHConnect(loginip, loginuserid, loginpwd, Integer.parseInt(loginport), false);
            if (isSSHConnected()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                sess = conn.openSession();
                sess.startShell();
                stdin = sess.getStdin();
                writedata();
                flushdata();
                InputStream stdout = new StreamGobbler(sess.getStdout());
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout, "GBK"));
                PrintWriter out = new PrintWriter(stdin);
                out.flush();
                Thread.sleep(1000);
                out.close();
                if (commandtype == Config.Sshquerycmd_LST_CELL) {
                    while (true) {
                        String line = br.readLine();
                        if ((line != null) && (line.contains("="))) {
                            String[] strarray = line.trim().split("=");
                            for (String s : strarray) {
                                map.put(trimadvanced(strarray[0]), trimadvanced(strarray[1]));
                            }
                        }
                        if (line == null) {
                            break;
                        }
                    }
                } else if (commandtype == Config.Sshquerycmd_LST_IPINTERFACE) {
                    while (true) {
                        String line = br.readLine();
                        if ((line != null) && ((line.contains("Wan") || (line.contains("WAN"))))) {
                            String[] strarray = line.split("\\s+");
                            for (int i = 0; i < strarray.length; i++) {
                                strarray[i] = trimadvanced(strarray[i]);
                            }
                            if ((strarray[2].equals("Wan")) || (strarray[2].contains("WAN"))) {
                                String[] resultip_mask = new String[2];
                                resultip_mask[0] = strarray[5];
                                resultip_mask[1] = strarray[6];
                                map.put(resultip_mask[0], resultip_mask[1]);
                            }
                        }
                        if (line == null) {
                            break;
                        }
                    }
                }
             /*   Set<Map.Entry<String, String>> ms = map.entrySet();
                for (Map.Entry entry : ms) {
                    Log.d(TAG, "liujin--SSHExcmd-------->>>>" + "getkey:  " + entry.getKey() + "<<<<----->>>>" +
                            "getvalue: " + entry.getValue());
                }*/
                br.close();
                closessh();
                iscmdexcuted = true;
                EventBus.getDefault().post(new MessageEvent(eventtype));
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void writedata() throws IOException {
        if (stdin != null) {
            stdin.write(command.getBytes());
        }
    }

    private void flushdata() throws IOException {
        if (stdin != null) {
            stdin.flush();
        }
    }

    private void closessh() {
        connected = false;
        if (sess != null) {
            sess.close();
            sess = null;
        }
        if (conn != null) {
            conn.close();
            conn = null;
        }
    }

}
