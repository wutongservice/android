package com.borqs.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.Log;
import android.view.*;

import android.widget.*;
import com.borqs.common.SelectionItem;
import com.borqs.common.dialog.CorpusSelectionDialog;
import com.borqs.common.view.ConversationMultiAutoCompleteTextView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.ui.QiupuBaseCommentsActivity;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;

import com.borqs.common.adapter.DialogUsersAdapter;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.FriendsListActivity;
import com.borqs.qiupu.util.StringUtil;

public class DialogUtils {
    private static final String TAG = "DialogUtils";

    public static final int CommentLike = 7;
    public static final int StreamLike = 2;

    public static void ShowDialogUserListDialog(Context context, int titleid, List<QiupuSimpleUser> users,
                                                AdapterView.OnItemClickListener itemClickListener) {
        View contentLayout = LayoutInflater.from(context).inflate(R.layout.dialog_user_list, null);
        ListView listview = (ListView) contentLayout.findViewById(R.id.user_list);

        DialogUsersAdapter useradapter = new DialogUsersAdapter(context, users);
        listview.setAdapter(useradapter);
        useradapter.notifyDataSetChanged();

        if (null != itemClickListener) {
            listview.setOnItemClickListener(itemClickListener);
        }

        AlertDialog userListDlg = new AlertDialog.Builder(context)
                .setTitle(titleid)
                .setView(contentLayout)
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();

        userListDlg.show();
    }

    public static AlertDialog createDialog(final Context context, int resTitle, String msg,
                                           DialogInterface.OnClickListener listener, final int dialogId) {
        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
        replaceBuilder.setTitle(resTitle)
                .setMessage(msg)
                .setNegativeButton(R.string.label_cancel, null)
                .setPositiveButton(R.string.label_ok, listener);
        AlertDialog dialog = replaceBuilder.create();
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((Activity) context).removeDialog(dialogId);
            }
        });
        return dialog;
    }

    public static ProgressDialog createProgressDialog(final Context context, int resid,
                                                      boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(resid));
        dialog.setCanceledOnTouchOutside(CanceledOnTouchOutside);
        dialog.setIndeterminate(Indeterminate);
        dialog.setCancelable(cancelable);
        return dialog;
    }

    public static ProgressDialog createProgressDialogWithTitle(final Context context, int resid, int resTitle,
            boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(resTitle);
        dialog.setMessage(context.getString(resid));
        dialog.setCanceledOnTouchOutside(CanceledOnTouchOutside);
        dialog.setIndeterminate(Indeterminate);
        dialog.setCancelable(cancelable);
        return dialog;
    }

    public static void showConfirmDialog(final Context context, int resTitle, int resMsg, int resOk, int resCancel,
                                         DialogInterface.OnClickListener listener) {
        showConfirmDialog(context, resTitle, resMsg, resOk, resCancel, listener, null);
    }

    public static void showConfirmDialog(final Context context, int resTitle, int resMsg, int resOk, int resCancel,
                                         DialogInterface.OnClickListener positiveListener,
                                         DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
        replaceBuilder.setTitle(resTitle)
                .setMessage(resMsg)
                .setPositiveButton(resOk, positiveListener)
                .setNegativeButton(resCancel, negativeListener);
        AlertDialog dialog = replaceBuilder.create();

        dialog.show();
    }

    public static void showConfirmDialog(final Context context, String resTitle, String resMsg,
                                         DialogInterface.OnClickListener listener) {
        showConfirmDialog(context, resTitle, resMsg, listener, null);
    }

    public static AlertDialog showConfirmDialog(final Context context, String resTitle, String resMsg,
                                         DialogInterface.OnClickListener positiveListener,
                                         DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
        replaceBuilder.setTitle(resTitle)
                .setMessage(resMsg)
                .setPositiveButton(R.string.label_ok, positiveListener)
                .setNegativeButton(R.string.label_cancel, negativeListener);
        AlertDialog dialog = replaceBuilder.create();
        
        dialog.show();
        return dialog;
    }

    public static void showSingleChoiceDialog(final Context context, int resTitle, final String[] items, final int checkItem,
                                              DialogInterface.OnClickListener itemClickListener,
                                              DialogInterface.OnClickListener positiveListener,
                                              DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
        replaceBuilder.setTitle(resTitle)
                .setSingleChoiceItems(items, checkItem, itemClickListener);
        if (positiveListener != null) {
            replaceBuilder.setPositiveButton(R.string.label_ok, positiveListener);
        }
        if (negativeListener != null) {
            replaceBuilder.setNegativeButton(R.string.label_cancel, negativeListener);
        }
        AlertDialog dialog = replaceBuilder.create();

        dialog.show();
    }
    
    public static AlertDialog showSingleChoiceDialogWithAdapter(final Context context, int resTitle, final ListAdapter adapter, final int checkedItem,
    		DialogInterface.OnClickListener itemClickListener,
    		DialogInterface.OnClickListener positiveListener,
    		DialogInterface.OnClickListener negativeListener) {
    	AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
    	replaceBuilder.setTitle(resTitle).setSingleChoiceItems(adapter, checkedItem, itemClickListener);
//    	.setSingleChoiceItems(items, checkItem, itemClickListener);
    	if (positiveListener != null) {
    		replaceBuilder.setPositiveButton(R.string.label_ok, positiveListener);
    	}
    	if (negativeListener != null) {
    		replaceBuilder.setNegativeButton(R.string.label_cancel, negativeListener);
    	}
    	AlertDialog dialog = replaceBuilder.create();
    	
    	dialog.show();
    	
    	return dialog;
    }


    public static void showItemsDialog(final Context context, String resTitle, int resIcon, final String[] items,
                                       DialogInterface.OnClickListener ChooseItemClickListener) {
        AlertDialog.Builder itemsBuilder = new AlertDialog.Builder(context);
        if (StringUtil.isEmpty(resTitle) == false) {
            itemsBuilder.setTitle(resTitle);
        }
        if (resIcon > 0) {
            itemsBuilder.setIcon(resIcon);
        }
        itemsBuilder.setItems(items, ChooseItemClickListener);
        itemsBuilder.create().show();
    }

    public static void showOKDialog(final Context context, int resTitle,
                                    int resMsg, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (resTitle > 0) {
            builder.setTitle(resTitle);
        }
        builder.setMessage(resMsg)
                .setPositiveButton(R.string.label_ok, positiveListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static AlertDialog showDialogWithOnlyView(Context context, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.setView(view).create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialog.show();
        return dialog;
    }

    public static AlertDialog ShowDialogwithView(Context context, String title, int iconRes, View view,
                                                 DialogInterface.OnClickListener positiveListener,
                                                 DialogInterface.OnClickListener negativeListener) {

        AlertDialog.Builder Dialogbuilder = new AlertDialog.Builder(context);
        Dialogbuilder.setTitle(title);
        Dialogbuilder.setView(view);
        if (iconRes > 0) {
            Dialogbuilder.setIcon(iconRes);
        }

        if (positiveListener != null) {
            Dialogbuilder.setPositiveButton(R.string.label_ok, positiveListener);
        }
        if (negativeListener != null) {
            Dialogbuilder.setNegativeButton(R.string.label_cancel, negativeListener);
        }

        AlertDialog viewDialog = Dialogbuilder.create();
        viewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        viewDialog.show();
        return viewDialog;
    }

    public static AlertDialog ShowDialogwithView(Context context, int resTitle, View view,
            int resLeft, int resRight,
            DialogInterface.OnClickListener positiveListener,
            DialogInterface.OnClickListener negativeListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(resTitle);
        builder.setView(view);

        if (positiveListener != null) {
            builder.setPositiveButton(resLeft, positiveListener);
        }
        if (negativeListener != null) {
            builder.setNegativeButton(resRight, negativeListener);
        }

        AlertDialog viewDialog = builder.create();
        viewDialog.show();
        return viewDialog;
    }
    
    public static void showRetryDialog(Context context, int resTitle, int resMsg, int resPos, int resNeu,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnClickListener neutralListener) {
        String title = context.getResources().getString(resTitle);
        String msg = context.getResources().getString(resMsg);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(msg)
                .setPositiveButton(resPos, positiveListener)
                .setNeutralButton(resNeu, neutralListener)
                .setNegativeButton(R.string.label_cancel, null);
//                .setNegativeButton(textId, listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    public static void showExitPublicDialog(Context context, int resTitle, int resMsg,
            DialogInterface.OnClickListener neutralListener,
            DialogInterface.OnClickListener negativeListener) {
        String title = context.getResources().getString(resTitle);
        String msg = context.getResources().getString(resMsg);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(msg)
        .setPositiveButton(R.string.label_cancel, null)
        .setNeutralButton(R.string.public_circle_exit_dialog, neutralListener)
        .setNegativeButton(R.string.public_circle_exit_dialog_grant_mamanger, negativeListener);
//.setNegativeButton(textId, listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

//    public void showListDialog(final Context context, String title) {
//        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
//        replaceBuilder.setTitle(resTitle)
//                .setMessage(resMsg)
//                .setPositiveButton(R.string.label_ok, positiveListener)
//                .setNegativeButton(R.string.label_cancel, negativeListener);
//        AlertDialog dialog = replaceBuilder.create();
//
//        dialog.show();
//    }

    public static Dialog showProgressDialog(Context context, int titleRes, String msgRes) {
        ProgressDialog dialog = new ProgressDialog(context);
        if (titleRes > 0) {
            dialog.setTitle(titleRes);
        }
        dialog.setMessage(msgRes);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }

    public static void showCorpusSelectionDialog(Activity context, final int x, final int y,
    		final ArrayList<SelectionItem> items,int gravity,
    		final AdapterView.OnItemClickListener listener) {
    	CorpusSelectionDialog mCorpusSelectionDialog;
//        if (mCorpusSelectionDialog == null) {
	mCorpusSelectionDialog = new CorpusSelectionDialog(context, items, listener);
	mCorpusSelectionDialog.setIsdropDownMenu(true);
	
//          WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//          Display d = windowManager.getDefaultDisplay();
	
	Window window = mCorpusSelectionDialog.getWindow();
	WindowManager.LayoutParams lp = window.getAttributes();
	
	lp.gravity = gravity;
	lp.x = x;
//          lp.y = (location[1] - (d.getHeight()/2));
	lp.y = y;
	
	if (QiupuConfig.LOGD) Log.d(TAG, "dialog layout x: " + lp.x + "dialog layout y: " + lp.y);
	lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
	lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
	window.setAttributes(lp);
	
	mCorpusSelectionDialog.setCanceledOnTouchOutside(true);
	mCorpusSelectionDialog.setOwnerActivity(context);
	if (CorpusSelectionDialog.OnCorpusSelectedListener.class.isInstance(context)) {
		mCorpusSelectionDialog.setOnCorpusSelectedListener((CorpusSelectionDialog.OnCorpusSelectedListener) context);
	}
//        }
//        else
//        {
//            mCorpusSelectionDialog.refreshCorpus(items);
//        }
	mCorpusSelectionDialog.show();
    }
    
    public static void showCorpusSelectionDialog(Activity context, final int x, final int y,
                                                 final ArrayList<SelectionItem> items,
                                                 final AdapterView.OnItemClickListener listener) {
        showCorpusSelectionDialog(context, x, y, items, Gravity.LEFT | Gravity.TOP, listener);
    }

    public static interface PhotoPickInterface {
        public void doTakePhotoCallback(); // from camera
        public void doPickPhotoFromGalleryCallback(); // from  gallery
    }
    public static AlertDialog ShowPhotoPickDialog(Context context, int resTitle, final PhotoPickInterface callBack) {
        String[] items = new String[]{context.getString(R.string.take_photo),
                context.getString(R.string.phone_album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(resTitle)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            dialog.dismiss();
                            callBack.doTakePhotoCallback();
                        } else {
                            dialog.dismiss();
                            callBack.doPickPhotoFromGalleryCallback();
                        }
                    }
                });

            AlertDialog viewDialog = builder.create();
            viewDialog.show();
            return viewDialog;
        }

    public static void showTipDialog(final Context context, int resTitle, String resMsg, int resCancel) {
        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
        replaceBuilder.setTitle(resTitle)
                      .setMessage(resMsg)
                      .setNegativeButton(resCancel, null);
        AlertDialog dialog = replaceBuilder.create();
        dialog.show();
    }

    public static AlertDialog showSmileyDialog(final Context context, final TextView textView) {
        if (null != textView) {
            int[] icons = SmileyParser.DEFAULT_SMILEY_RES_IDS;
            String[] names = context.getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_NAMES);
            final String[] texts = context.getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_TEXTS);

            final int N = names.length;

            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (int i = 0; i < N; i++) {
                // We might have different ASCII for the same icon, skip it if
                // the icon is already added.
                boolean added = false;
                for (int j = 0; j < i; j++) {
                    if (icons[i] == icons[j]) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    HashMap<String, Object> entry = new HashMap<String, Object>();

                    entry.put("icon", icons[i]);
                    entry.put("name", names[i]);
                    entry.put("text", texts[i]);

                    entries.add(entry);
                }
            }

            final SimpleAdapter a = new SimpleAdapter(
                    context,
                    entries,
                    R.layout.smiley_menu_item,
                    new String[]{"icon", "name", "text"},
                    new int[]{R.id.smiley_icon, R.id.smiley_name, R.id.smiley_text});
            SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView) {
                        Drawable img = context.getResources().getDrawable((Integer) data);
                        ((ImageView) view).setImageDrawable(img);
                        return true;
                    }
                    return false;
                }
            };
            a.setViewBinder(viewBinder);

            AlertDialog.Builder b = new AlertDialog.Builder(context);

            b.setTitle(context.getString(R.string.insert_smiley_title));

            b.setCancelable(true);
            b.setAdapter(a, new DialogInterface.OnClickListener() {
                @SuppressWarnings("unchecked")
                public final void onClick(DialogInterface dialog, int which) {
                    HashMap<String, Object> item = (HashMap<String, Object>) a.getItem(which);

                    String smiley = (String) item.get("text");
                    if (textView instanceof EditText) {
                        EditText editText = (EditText)textView;
                        editText.clearComposingText();
                        final int index = editText.getSelectionStart();
                        if (index < 0 || index > editText.length()) {
                            editText.append(smiley);
                        } else {
                            Editable editable = editText.getEditableText();
                            editable.insert(index, smiley);
                        }
                    } else {
                        textView.append(smiley);
                    }
                    textView.requestFocus();
                    dialog.dismiss();
                }
            });

            AlertDialog dlg = b.create();
            dlg.show();
            return dlg;
        }

        return null;
    }

    public static void showOpenGPSDialog(final Context context) {
        DialogUtils.showConfirmDialog(context, R.string.location_title,
                R.string.location_message, R.string.label_ok, R.string.label_cancel, 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                } catch(ActivityNotFoundException e) {
                    Log.d(TAG, "ActivityNotFoundException error, no such activity.");
                }
            }
        });
    }

}