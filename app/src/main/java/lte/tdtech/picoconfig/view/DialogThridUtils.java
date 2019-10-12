package lte.tdtech.picoconfig.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import lte.tdtech.picoconfig.R;

/**
 * Created by lWX453051 on 2019/5/22.
 */

public class DialogThridUtils {

    private static final String TAG = "DialogThridUtils";
    private static Dialog loadingDialog;
    private static AlertDialog.Builder disconnectdialogbuilder;

    /**
     * 显示Dialog
     *
     * @param context      上下文
     * @param msg          显示内容
     * @param isTransBg    是否透明
     * @param isCancelable 是否可以点击取消
     * @return
     */

    public static void showdisconnectdialog(String status, Context context) {
        disconnectdialogbuilder = new AlertDialog.Builder(context).setMessage(status).setPositiveButton(context
                .getResources().getString(R.string.str_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                disconnectdialogbuilder.create().dismiss();
            }
        });
        disconnectdialogbuilder.create();
        disconnectdialogbuilder.create().setCancelable(false);
        disconnectdialogbuilder.create().setCanceledOnTouchOutside(false);
        disconnectdialogbuilder.create().show();
    }

    public static Dialog showWaitDialog(Context context, boolean isTransBg, boolean isCancelable) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.thridlogin_dialog_loading, null);         // 得到加载view
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
//        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);   // 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
//        tipTextView.setText(msg);// 设置加载信息
        loadingDialog = new Dialog(context, isTransBg ? R.style.TransDialogStyle : R.style.WhiteDialogStyle);
        // 创建自定义样式dialog
        loadingDialog.setContentView(layout);
        loadingDialog.setCancelable(isCancelable);
        loadingDialog.setCanceledOnTouchOutside(false);

        Window window = loadingDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.PopWindowAnimStyle);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loadingDialog.show();
        return loadingDialog;
    }

    /**
     * 关闭dialog
     * @param mDialogUtils
     */
    public static void closeshowWaitDialog(Dialog mDialogUtils) {
        if (mDialogUtils != null && mDialogUtils.isShowing()) {
            mDialogUtils.dismiss();
            loadingDialog = null;
        }
    }
}
