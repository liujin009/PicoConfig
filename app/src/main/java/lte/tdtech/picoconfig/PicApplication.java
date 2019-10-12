package lte.tdtech.picoconfig;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by lWX453051 on 2019/5/24.
 */

public class PicApplication extends Application {
    private static final String TAG = "PicApplication";
    private static PicApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getPicContext() {
        return instance;
    }
}
