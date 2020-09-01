package xyz.tsumugu2626.app.fami;

import android.content.Context;
import android.content.SharedPreferences;

public class SP {

    /*
     * 情報を保存するClass
    */

    //保存
    public static void saveString(Context ctx, String key, String val) {
        SharedPreferences prefs = ctx.getSharedPreferences("xyz.tsumugu2626.app.fami", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, val);
        editor.apply();
    }

    //取得
    public static String loadString(Context ctx, String key) {
        SharedPreferences prefs = ctx.getSharedPreferences("xyz.tsumugu2626.app.fami", Context.MODE_PRIVATE);
        return prefs.getString(key, ""); // 第２引数はkeyが存在しない時に返す初期値
    }
}
