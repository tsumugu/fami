package xyz.tsumugu2626.app.fami;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.daasuu.bl.BubbleLayout;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SelectLoginUserActivity extends AppCompatActivity {

    /*
     * ログインする家族のメンバーを選択する
     */

    private RelativeLayout v;
    private Context c;
    private ListView listView;
    private SnackbarClass sncbar;
    private ArrayList family_name_array;
    private ArrayList family_id_array;
    private BubbleLayout FSBubbleLayout;
    private ProgressDialog progressDialog;
    private ArrayList<FSListItem> listItems;
    private long boot_timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_login_user);

        family_name_array = new ArrayList<>();
        family_id_array = new ArrayList<>();
        boot_timestamp = System.currentTimeMillis();
        sncbar = new SnackbarClass();
        FSBubbleLayout = findViewById(R.id.FSBubbleLayout);
        v = findViewById(R.id.rl_slu);
        c = SelectLoginUserActivity.this;
        listView = findViewById(R.id.login_user_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String family_name = family_name_array.get(position).toString();
                final String family_id = family_id_array.get(position).toString();
                if (!family_id.equals("0")) {
                    //処理を問うダイアログを表示
                    new AlertDialog.Builder(c)
                            .setMessage(family_name+"さんとしてログインします")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //ユーザIDを設定してトップページに飛ばす
                                    SP.saveString(c,"family_id",family_id);
                                    Intent intent = new Intent(c, TopActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("キャンセル", null)
                            .show();
                }
            }
        });
        get_family_s_from_server();
        Intent i = getIntent();
        String from = i.getStringExtra("from");
        if (from != null) {
            if (from.equals("fs-d")) {
                new AlertDialog.Builder(c)
                        .setMessage("ログイン中のユーザが削除されました。再度ログインするユーザを選択してください")
                        .setPositiveButton("OK", null)
                        .setCancelable(false)
                        .show();
            }
        }
        FSBubbleLayout.setVisibility(View.GONE);
        String is_first = i.getStringExtra("is_first");
        if (is_first != null) {
            if (is_first.equals("true")) {
                FSBubbleLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void get_family_s_from_server() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        family_id_array = new ArrayList<>();
        listItems = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/get_family.php?token="+SP.loadString(this,"token")).build();
        client.newCall(request).enqueue(new Callback() {
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                        snackbar.setAction("再試行", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                get_family_s_from_server();
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int res_code = response.code();
                final String res = response.body().string();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //
                        if (res_code == 200) {
                            try {
                                JSONObject json = new JSONObject(res);
                                int json_res_id = json.getInt("id");
                                String json_res_message = json.getString("message");
                                if (json_res_id == 1) {
                                    //サーバから取得したJSONからfamily_listを取得
                                    if (json.getJSONArray("family_list") != null) {
                                        JSONArray array = json.getJSONArray("family_list");
                                        int len = array.length();
                                        if (len == 0) {
                                           //家族がいなかった場合、家族設定画面に飛ばす
                                            Intent intent = new Intent(c, FamilyStructureActivity.class);
                                            intent.putExtra("from","slu");
                                            startActivity(intent);
                                            //
                                        } else {
                                            for(int i=0;i<len;i++){
                                                JSONObject info = array.getJSONObject(i);
                                                String name = info.getString("name");
                                                String position = info.getString("position");
                                                String family_id = info.getString("family_id");
                                                String img_url = info.getString("img_url");
                                                String sub_text = "----";
                                                if (img_url.equals("notfound")) {
                                                    img_url = MyURLManager.get_main_url()+"nty_login/notfound.png";
                                                } else {
                                                    img_url = MyURLManager.get_vps_url()+"uploads/"+SP.loadString(c,"token")+"-"+family_id+".jpeg";
                                                    img_url = img_url+"?time="+boot_timestamp;
                                                }
                                                family_name_array.add(name);
                                                family_id_array.add(family_id);
                                                FSListItem item = new FSListItem(img_url, name+"("+position+")",sub_text);
                                                listItems.add(item);
                                            }
                                        }
                                    } else{
                                        //サーバから何も帰ってこなかった場合
                                        FSListItem item = new FSListItem(MyURLManager.get_main_url()+"nty_login/notfound.png", "取得に失敗しました","");
                                        listItems.add(item);
                                    }
                                    ArrayAdapter adapter = new FSListAdapter(getApplicationContext(), R.layout.fs_list_item, listItems);
                                    listView.setAdapter(adapter);
                                } else  if (json_res_id == 2) {
                                    //ログイン情報に問題があった場合, 再ログインさせる
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
                                    get_family_s_from_server();
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

    @Override
    public void onBackPressed() {
    }
}
