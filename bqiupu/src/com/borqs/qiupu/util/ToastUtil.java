package com.borqs.qiupu.util;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.borqs.common.util.DataConnectionUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;

public class ToastUtil {

	public static void showShortToast(final Context context, final Handler handler, final int resId) {
        if (null != handler) {
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
                }
            });
        }

	}

	public static void showShortToast(final Context context, final Handler handler, final CharSequence message) {
        if (null != handler) {
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

	}

	public static void showLongToast(final Context context, final Handler handler, final int resId) {
        if (null != handler) {
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
                }
            });
        }
	}

	public static void showLongToast(final Context context, final Handler handler, final CharSequence message) {
        if (null != handler) {
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
        }
	}
    public static void showOperationOk(Context context, final Handler handler, boolean isShort) {
        final String prompt = context.getString(R.string.operate_succeed);
        if (isShort) {
            showShortToast(context, handler, prompt);
        } else {
            showLongToast(context, handler, prompt);
        }
    }

    public static void showOperationFailed(Context context, final Handler handler, boolean isShort) {
        showOperationFailed(context, handler, isShort, "");
    }

    public static void showOperationFailed(Context context, final Handler handler, boolean isShort, final CharSequence reason) {
        final String prompt = context.getString(R.string.operate_failed, TextUtils.isEmpty(reason) ? "" : reason);
        if (isShort) {
            showShortToast(context, handler, prompt);
        } else {
            showLongToast(context, handler, prompt);
        }
    }
    
    public static void showCustomToast(final Context context, final int resId) {
        if(BasicActivity.class.isInstance(context)) {
            BasicActivity bs = (BasicActivity) context;
            bs.showCustomToast(resId);
        }
    }
    
    public static void showCustomToast(final Context context, final int resId, Handler handler) {
        if(BasicActivity.class.isInstance(context)) {
            BasicActivity bs = (BasicActivity) context;
            bs.showCustomToast(resId, handler);
        }
    }
    
    public static void showCustomToast(final Context context, final String res) {
        if(BasicActivity.class.isInstance(context)) {
            BasicActivity bs = (BasicActivity) context;
            bs.showCustomToast(res);
        }
    }
    
    public static void showCustomToast(final Context context, final String res, Handler handler) {
        if(BasicActivity.class.isInstance(context)) {
            BasicActivity bs = (BasicActivity) context;
            bs.showCustomToast(res, handler);
        }
    }
    
    public static boolean testValidConnectivity(Context context) {
        final boolean isConnectivityActive = DataConnectionUtils.testValidConnection(context);

        if (!isConnectivityActive) {
            showCustomToast(context, R.string.dlg_msg_no_active_connectivity);
        }

        return isConnectivityActive;
    }
}
