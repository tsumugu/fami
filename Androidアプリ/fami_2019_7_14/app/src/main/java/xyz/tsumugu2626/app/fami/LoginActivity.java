package xyz.tsumugu2626.app.fami;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    /*
     * ログイン画面
    */

    private EditText mail;
    private EditText pw;
    private SnackbarClass sncbar;
    private RelativeLayout v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mail = findViewById(R.id.editTextMailReg);
        pw = findViewById(R.id.editTextPWReg);
        sncbar = new SnackbarClass();
        v = findViewById(R.id.rl_login);

        //登録画面から飛んできてたら
        Intent i = getIntent();
        String from = i.getStringExtra("from");
        if (from != null) {
            if (from.equals("reg")) {
                //登録完了メッセージ
                sncbar.dispInfo("登録が完了しました。メールアドレスの確認を完了させてください",LoginActivity.this, v);
            } else if (from.equals("fs")) {
                sncbar.dispWarning("問題が発生しました。再ログインしてください",LoginActivity.this, v);
                SP.saveString(this,"token","");
                SP.saveString(this,"family_id","");
                SP.saveString(this,"type","");
            }
        }
    }

    //送信ボタンが押された時
    public void post_server(View v) {
        //キーボードを収納
        mail.clearFocus();
        pw.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //
        ps();
    }

    //登録画面へ
    public void goto_regist(View v) {
        Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
        startActivity(intent);
    }

    //ログイン処理を行う関数
    public void ps() {
        //
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/login.php?email="+mail.getText().toString()+"&password="+pw.getText().toString()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RelativeLayout v = findViewById(R.id.rl_login);
                Context c = LoginActivity.this;
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ps();
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int res_code = response.code();
                String res = response.body().string();
                Log.d("pw",res);
                if (res_code == 200) {
                    try {
                        JSONObject json = new JSONObject(res);
                        int json_res_id = json.getInt("id");
                        String json_res_message = json.getString("message");
                        String json_res_token = json.getString("token");
                        if (json_res_id == 1) {
                            //成功
                            //family_idを保存, ログインユーザ選択画面もしくはトップへ
                            SP s = new SP();
                            s.saveString(LoginActivity.this,"token",json_res_token);
                            String type_s = s.loadString(LoginActivity.this,"type");
                            if (type_s.equals("home")) {
                                Intent intent = new Intent(LoginActivity.this,MainCam2Activity.class);
                                startActivity(intent);
                            } else if (type_s.equals("self")) {
                                Intent intent = new Intent(LoginActivity.this,SelectLoginUserActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(LoginActivity.this,SelectTypeActivity.class);
                                startActivity(intent);
                            }
                        } else  {
                            //何らかの問題
                            sncbar.dispWarning(json_res_message,LoginActivity.this, v);
                        }
                        //Log.d("debug_mes",json_res_id+"/"+json_res_message+"/"+json_res_token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    RelativeLayout v = findViewById(R.id.rl_login);
                    Context c = LoginActivity.this;
                    final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                    snackbar.setAction("再試行", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ps();
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}