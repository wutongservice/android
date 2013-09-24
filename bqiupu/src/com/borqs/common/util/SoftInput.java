package com.borqs.common.util;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-5-2
 * Time: 下午4:35
 * To change this template use File | Settings | File Templates.
 */
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class SoftInput
{
  public static InputMethodManager getInputMethodManager(Context paramContext)
  {
    return (InputMethodManager)paramContext.getSystemService("input_method");
  }

  public static void hide(View paramView)
  {
    InputMethodManager localInputMethodManager = getInputMethodManager(paramView.getContext());
    if (localInputMethodManager != null)
      localInputMethodManager.hideSoftInputFromWindow(paramView.getWindowToken(), 0);
  }

  public static void restart(View paramView)
  {
    InputMethodManager localInputMethodManager = getInputMethodManager(paramView.getContext());
    if (localInputMethodManager != null)
      localInputMethodManager.restartInput(paramView);
  }

  public static void show(View paramView)
  {
    InputMethodManager localInputMethodManager = getInputMethodManager(paramView.getContext());
    if (localInputMethodManager != null)
      localInputMethodManager.showSoftInput(paramView, 0);
  }
}
