package lte.tdtech.picoconfig.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import lte.tdtech.picoconfig.*;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    SharedPreferences shared;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        shared = getSharedPreferences("firstloginfile", MODE_PRIVATE);
        editor = shared.edit();
    }

    @Override
    protected void onDestroy() {
        editor.clear().commit();
        deletefirstloginfile();
        super.onDestroy();
    }

    private void deletefirstloginfile() {
        File file = new File("/data/data/" + getPackageName().toString() + "/shared_prefs", "firstloginfile.xml");
        if (file.exists()) {
            file.delete();
//            Toast.makeText(this, "删除成功", Toast.LENGTH_LONG).show();
        }
    }


    private boolean userFirstLogin() {
        return shared.getBoolean("isfirstlogin", true);
    }

    /**
     * 判断app是否存在
     *
     * @param pkgName
     * @return
     */
    private boolean isAppExist(String pkgName) {
        ApplicationInfo info;
        try {
            info = getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            info = null;
        }
        return info != null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.picoconfig: {
                if (userFirstLogin()) {
                    Intent intent = new Intent(this, UserLoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent2 = new Intent(this, ListConfigActivity.class);
                    startActivity(intent2);
                }
                break;
            }
            case R.id.sweepfrequency: {
                if (isAppExist("com.tdtech.lte.ProjectMenuAssistUpdate")) {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.tdtech.lte" + "" +
                            ".ProjectMenuAssistUpdate"));
                } else {
                    Toast.makeText(this, getString(R.string.str_sweepappmiss), Toast.LENGTH_LONG).show();
                }
            }
            default:
                break;

        }

    }
}
