package xyz.tsumugu2626.app.fami;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RelativeLayout;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BootActivity extends AppCompatActivity {

    /*
     * 一番最初に起動するActivity, メンテナンスの確認とログインチェックなどを行う
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);

        sc();
    }

    public void sc() {
        //サーバと通信してメンテナンス中か確認する
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/is_mnt.php").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RelativeLayout v = findViewById(R.id.rl);
                Context c = BootActivity.this;
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sc();
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int res_code = response.code();
                String res = response.body().string();
                if (res_code == 200) {
                    try {
                        JSONObject json = new JSONObject(res);
                        String is_mnt = json.getString("is_mnt");
                        final String mnt_title = json.getString("mnt_title");
                        final String mnt_mes = json.getString("mnt_mes");
                        if (is_mnt.equals("true")) {
                            //メンテナンス中ダイアログ表示
                            final Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(BootActivity.this)
                                            .setTitle(mnt_title)
                                            .setMessage(mnt_mes)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //アプリ終了
                                                    finish();
                                                }
                                            })
                                            .show();
                                }
                            });
                        } else{
                            SP s = new SP();
                            String t = s.loadString(BootActivity.this,"token");
                            String fid = s.loadString(BootActivity.this,"family_id");
                            String type = s.loadString(BootActivity.this,"type");
                            if (t.equals("")) {
                                //ログインしていなかったらログイン画面へ
                                Intent intent = new Intent(BootActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                if (type.equals("")) {
                                    //type(どのActivityに飛ぶか) が設定されていなかった場合, 選択画面へ
                                    Intent intent = new Intent(BootActivity.this, SelectTypeActivity.class);
                                    startActivity(intent);
                                } else {
                                    //アプリのホーム画面へ
                                    if (type.equals("home")) {
                                        Intent intent = new Intent(BootActivity.this,MainCam2Activity.class);
                                        startActivity(intent);
                                    } else if (type.equals("self")) {
                                        //ログインユーザが選択されていなかった場合, 選択画面へ
                                        if (fid.equals("")) {
                                            Intent intent = new Intent(BootActivity.this,SelectLoginUserActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(BootActivity.this, TopActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        //その他の場合はとりあえず選択画面へ
                                        Intent intent = new Intent(BootActivity.this,SelectTypeActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //Http200以外が帰ってきた場合はエラー表示
                    RelativeLayout v = findViewById(R.id.rl);
                    Context c = BootActivity.this;
                    final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                    snackbar.setAction("再試行", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sc();
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }
}
