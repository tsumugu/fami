package xyz.tsumugu2626.app.fami;

import android.content.Context;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarClass {

    /*
     * Snackbarを表示するClass
    */

    public void dispWarning(String s, Context c, View v) {
        final Snackbar snackbar = Snackbar.make(v, s, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
        snackbar.show();
    }

    public void dispAttention(String s, Context c, View v) {
        final Snackbar snackbar = Snackbar.make(v, s, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarAttentionBG));
        snackbar.show();
    }

    public void dispInfo(String s, Context c, View v) {
        final Snackbar snackbar = Snackbar.make(v, s, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarInfoBG));
        snackbar.show();
    }
}
