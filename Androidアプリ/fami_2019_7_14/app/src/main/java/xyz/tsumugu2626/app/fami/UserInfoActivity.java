package xyz.tsumugu2626.app.fami;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {

    /*
     * ユーザの個別ページ
    */

    private Context c;
    private RelativeLayout v;
    private SnackbarClass sncbar;
    private ProgressDialog progressDialog;
    private LinearLayout change_image_LinearLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView name_textview;
    private ImageView iv;
    private ListView lv;
    private Button edit_button;
    private String name;
    private String image_url;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        c = UserInfoActivity.this;
        v = findViewById(R.id.rl_user_info);
        sncbar = new SnackbarClass();
        change_image_LinearLayout = findViewById(R.id.ChangeImageLinearLayout);
        name_textview = findViewById(R.id.textView);
        iv = findViewById(R.id.spiImageView);
        lv = findViewById(R.id.historyListView);
        edit_button = findViewById(R.id.editButton);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_profile();
            }
        });
        Intent i = getIntent();
        name = i.getStringExtra("name");
        image_url = i.getStringExtra("image_url");
        uid = i.getStringExtra("uid");
        if (name != null && image_url != null && uid != null) {
            //描画処理
            name_textview.setText(name);
            //String replaced_img_url = image_url.replace("?","&").replace(MyURLManager.get_vps_url()+"uploads/", MyURLManager.get_main_url()+"nty_login/api/get_icon_with_border.php?url=");
            Glide.with(c).load(image_url).into(iv);
            //ListViewとかの設定
            load_io_history();
        } else {
            //エラー、Topに戻す
            Intent intent = new Intent(c, TopActivity.class);
            startActivity(intent);
        }

        mSwipeRefreshLayout = findViewById(R.id.refresh);
        mSwipeRefreshLayout.setColorScheme(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                load_io_history();
            }
        });
    }

    public void load_io_history() {
        //listView
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/get_io_history.php?family_id="+SP.loadString(this,"token")+"&user_id="+uid).build();
        client.newCall(request).enqueue(new Callback() {
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                        snackbar.setAction("再試行", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                load_io_history();
                                snackbar.dismiss();
                            }
                        });
                        progressDialog.dismiss();
                        snackbar.show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int res_code = response.code();
                final String res = response.body().string();
                mainHandler.post(new Runnable() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void run() {
                        if (res_code == 200) {
                            try {
                                JSONObject json = new JSONObject(res);
                                int json_res_id = json.getInt("id");
                                String json_res_message = json.getString("message");
                                if (json_res_id == 1) {
                                    //JSONからfamily_listを取得
                                    JSONArray io_history_list = json.getJSONArray("io_history_list");
                                    if (io_history_list != null) {
                                        int len = io_history_list.length();
                                        ArrayList<HashMap<String, String>> listData = new ArrayList<>();
                                        if (len == 0) {
                                            //履歴がなかったら
                                            HashMap<String,String> data = new HashMap<>();
                                            data.put("item","入退出履歴はありません");
                                            data.put("subItem","");
                                            listData.add(data);
                                        } else {
                                            for(int i=0;i<len;i++){
                                                JSONObject info = io_history_list.getJSONObject(i);
                                                String json_status_text = info.getString("status_text");
                                                String json_timestamp_text = info.getString("timestamp_text");
                                                HashMap<String,String> data = new HashMap<>();
                                                data.put("item",json_status_text);
                                                data.put("subItem",json_timestamp_text);
                                                listData.add(data);
                                            }
                                        }
                                        SimpleAdapter adapter = new SimpleAdapter(c,
                                                listData,
                                                R.layout.io_hitory_item,
                                                new String[]{"item","subItem"},
                                                new int[]{android.R.id.text1, android.R.id.text2}
                                        );
                                        lv.setAdapter(adapter);
                                    } else{
                                        //取得失敗
                                        sncbar.dispWarning("取得に失敗しました", c, v);
                                    }
                                } else  if (json_res_id == 2) {
                                    //問題があるので再ログイン
                                    Intent intent = new Intent(c, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //何らかの問題
                                    sncbar.dispWarning(json_res_message, c, v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                            snackbar.setAction("再試行", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    load_io_history();
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                        //
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    public void save_profile(final String new_name) {
        //空白だったらそのまま完了
        if (new_name.trim().replace("　","").equals("")) {
            Intent intent = new Intent(c, UserInfoActivity.class);
            intent.putExtra("name",name);
            intent.putExtra("image_url",image_url);
            intent.putExtra("uid",uid);
            startActivity(intent);
        }
        //http://tsumugu2626.xyz/nty_login/api/edit_family.php?name=%E3%81%A6%E3%81%99%E3%82%93%E3%82%93&family_id=c7994106e35fd6ae231c85ec077d64aa&user_id=558be02bd4f370705f61e1393adc1eaf
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/edit_family_name.php?name="+new_name+"&user_id="+uid+"&family_id="+SP.loadString(this,"token")).build();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        save_profile(new_name);
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int res_code = response.code();
                final String res = response.body().string();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (res_code == 200) {
                            try {
                                JSONObject json = new JSONObject(res);
                                int json_res_id = json.getInt("id");
                                String json_res_message = json.getString("message");
                                if (json_res_id == 1) {
                                    //削除に成功したら、再読み込みで元に戻す
                                    Intent intent = new Intent(c, UserInfoActivity.class);
                                    intent.putExtra("name",new_name);
                                    intent.putExtra("image_url",image_url);
                                    intent.putExtra("uid",uid);
                                    startActivity(intent);
                                    //
                                } else  if (json_res_id == 3) {
                                    //ログイン情報に問題があった場合, 再ログインさせる
                                    Intent intent = new Intent(c, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //その他の問題が発生した場合エラーを表示
                                    sncbar.dispWarning(json_res_message, c, v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                            snackbar.setAction("再試行", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    save_profile(new_name);
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
            }
        });
    }

    public void edit_profile() {
        LinearLayout ETLL = findViewById(R.id.TextEditLinearLayout);
        ETLL.removeAllViews();
        final EditText ETLL_edittext = new EditText(this);
        ETLL_edittext.setHint(name);
        ETLL.addView(ETLL_edittext, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        edit_button.setText("完了");
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //変更を保存
                save_profile(ETLL_edittext.getText().toString());
            }
        });
        change_image_LinearLayout.setVisibility(View.VISIBLE);
    }

    public void goto_camera(View v) {
        Intent intent = new Intent(c, CameraActivity.class);
        intent.putExtra("userid",uid);
        intent.putExtra("from","userinfo");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(c, TopActivity.class);
        startActivity(intent);
    }

}
