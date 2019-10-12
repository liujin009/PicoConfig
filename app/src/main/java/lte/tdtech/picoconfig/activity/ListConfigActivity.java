package lte.tdtech.picoconfig.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import lte.tdtech.picoconfig.R;

/**
 * Created by lWX453051 on 2019/5/5.
 */

public class ListConfigActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listconfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.listconfigroute:
            {
                Intent intent = new Intent(this, RouteConfigActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.listconfigIp:
            {
                Intent intent2 = new Intent(this, IpConfigActivity.class);
                startActivity(intent2);
                break;
            }
            case R.id.listconfigfrepower:
                Intent intent3 = new Intent(this, FrequPowerConfigActivity.class);
                startActivity(intent3);
                break;
            default:
                break;
        }

    }
}
