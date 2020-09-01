package xyz.tsumugu2626.app.fami;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class FamilyStructureActivity extends AppCompatActivity {

    /*
     * ・家族の情報を追加, 編集する
    */

    private EditText name;
    private Spinner spn;
    private ListView listView;
    private SnackbarClass sncbar;
    private RelativeLayout v;
    private int famid;
    private ProgressDialog progressDialog;
    private TextView help_textview;
    private ArrayList list_array;
    private ArrayList name_array;
    private ArrayList position_array;
    private ArrayList family_id_array;
    private ArrayList<FSListItem> listItems;
    private long boot_timestamp;
    private String where_from;
    private BubbleLayout FSBubbleLayout;
    private boolean is_first;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_structure);

        sncbar = new SnackbarClass();
        v = findViewById(R.id.rl_family_s);
        name = findViewById(R.id.editTextName);
        FSBubbleLayout = findViewById(R.id.FSBubbleLayout);
        spn = findViewById(R.id.fs);
        listView = findViewById(R.id.familyList);
        help_textview = findViewById(R.id.helpTextView);
        list_array = new ArrayList<>();
        name_array = new ArrayList<>();
        position_array = new ArrayList<>();
        family_id_array = new ArrayList<>();
        listItems = new ArrayList<>();
        boot_timestamp = System.currentTimeMillis();
        where_from = "";
        is_first = false;
        Intent i = getIntent();
        String from = i.getStringExtra("from");
        if (from != null) {
            where_from = from;
            if (where_from.equals("mcmk")) {
                FSBubbleLayout.setVisibility(View.VISIBLE);
                help_textview.setText("リストをタップして顔写真を登録");
            }
            /*
            Log.d("family_debug",where_from);
            if (where_from.equals("mcmk")) {
                if (SP.loadString(FamilyStructureActivity.this,"family_id").equals("")) {
                    FSBubbleLayout.setVisibility(View.VISIBLE);
                }
            } else if (where_from.equals("slu-k")) {
                FSBubbleLayout.setVisibility(View.VISIBLE);
                help_textview.setText("最後に、戻るボタンを押してログインユーザを選択してください");
            }
            */
        }

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                famid = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //デザイン変更
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.family_list)
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spn.setAdapter(adapter);
        // リストが押された時
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String family_id = family_id_array.get(position).toString();
                final String selectedItem = name_array.get(position).toString();
                if (!family_id.equals("0")) {
                    //処理を問うダイアログを表示
                    new AlertDialog.Builder(FamilyStructureActivity.this)
                            .setMessage("処理を選択してください")
                            .setPositiveButton("顔写真を登録", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //ユーザIDを設定して顔写真撮影ページに飛ばす
                                    //SP.saveString(FamilyStructureActivity.this,"family_id",family_id);
                                    Intent intent = new Intent(FamilyStructureActivity.this, CameraActivity.class);
                                    intent.putExtra("userid",family_id);
                                    if (is_first) {
                                        intent.putExtra("is_first","true");
                                    }
                                    if (where_from.equals("mcmk") || where_from.equals("mcm")) {
                                        intent.putExtra("from", "mcmk");
                                    } else if (where_from.equals("slu")) {
                                        intent.putExtra("from", "slu");
                                    }
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("このユーザを削除する", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AlertDialog.Builder(FamilyStructureActivity.this)
                                            .setTitle("確認")
                                            .setMessage("本当に「"+selectedItem+"」さんを削除しますか？")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //削除する
                                                    delete_familyinfo(family_id);
                                                }
                                            })
                                            .setNegativeButton("キャンセル", null)
                                            .show();
                                }
                            })
                            .show();
                }
            }
        });
        FSBubbleLayout.setVisibility(View.INVISIBLE);
        //サーバから家族構成取得
        get_family_s_from_server();
    }

    @Override
    protected void onResume() {
        Intent i = getIntent();
        String is_f = i.getStringExtra("is_first");
        Log.d("family_debug","onBack"+is_f);
        if (is_f != null) {
            if (is_f.equals("true") && SP.loadString(FamilyStructureActivity.this,"family_id").isEmpty()) {
                Intent intent = new Intent(FamilyStructureActivity.this, SelectLoginUserActivity.class);
                intent.putExtra("is_first","true");
                startActivity(intent);
            }
        }
        if (where_from.equals("mcmk")) {
            FSBubbleLayout.setVisibility(View.VISIBLE);
            help_textview.setText("リストをタップして顔写真を登録");
        }
        super.onResume();
    }

    //家族情報を削除する関数
    public void delete_familyinfo(final String uid) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/delete_family.php?uid="+uid+"&token="+SP.loadString(this,"token")).build();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RelativeLayout v = findViewById(R.id.rl_login);
                Context c = FamilyStructureActivity.this;
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delete_familyinfo(uid);
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
                                    //成功
                                    String fid = SP.loadString(FamilyStructureActivity.this,"family_id");
                                    if (fid.equals(uid)) {
                                        //もしログイン中のユーザが削除された場合はログインユーザ選択ページへ
                                        SP.saveString(FamilyStructureActivity.this,"family_id","");
                                        Intent intent = new Intent(FamilyStructureActivity.this,SelectLoginUserActivity.class);
                                        intent.putExtra("from","fs-d");
                                        startActivity(intent);
                                    } else {
                                        //家族情報を再ロード
                                        get_family_s_from_server();
                                        sncbar.dispInfo(json_res_message, FamilyStructureActivity.this, v);
                                    }
                                } else  if (json_res_id == 3) {
                                    //ログイン情報に問題があった場合, 再ログインさせる
                                    Intent intent = new Intent(FamilyStructureActivity.this, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //その他の問題が発生した場合エラーを表示
                                    sncbar.dispWarning(json_res_message, FamilyStructureActivity.this, v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            RelativeLayout v = findViewById(R.id.rl_login);
                            Context c = FamilyStructureActivity.this;
                            final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                            snackbar.setAction("再試行", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    delete_familyinfo(uid);
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

    public void get_family_s_from_server() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        list_array = new ArrayList<>();
        name_array = new ArrayList<>();
        position_array = new ArrayList<>();
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
                        RelativeLayout v = findViewById(R.id.rl_login);
                        Context c = FamilyStructureActivity.this;
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
                                            list_array.add("家族が登録されていません。下から追加してください");
                                            name_array.add("0");
                                            position_array.add("0");
                                            family_id_array.add("0");
                                            FSListItem item = new FSListItem(MyURLManager.get_main_url()+"nty_login/notfound.png", "家族が登録されていません。","下から追加してください");
                                            listItems.add(item);
                                            FSBubbleLayout.setVisibility(View.VISIBLE);
                                        } else {
                                            int icon_notfound_count = 0;
                                            for(int i=0;i<len;i++){
                                                JSONObject info = array.getJSONObject(i);
                                                String name = info.getString("name");
                                                String position = info.getString("position");
                                                String family_id = info.getString("family_id");
                                                String img_url = info.getString("img_url");
                                                String sub_text = "顔写真アップロード済み";
                                                if (img_url.equals("notfound")) {
                                                    img_url = MyURLManager.get_main_url()+"nty_login/notfound.png";
                                                    sub_text = "★ 顔写真未アップロード";
                                                    icon_notfound_count++;
                                                } else {
                                                    img_url = MyURLManager.get_vps_url()+"uploads/"+SP.loadString(FamilyStructureActivity.this,"token")+"-"+family_id+".jpeg";
                                                    img_url = img_url+"?time="+boot_timestamp;
                                                }
                                                name_array.add(name);
                                                position_array.add(position);
                                                family_id_array.add(family_id);
                                                list_array.add(name+"("+position+")");
                                                FSListItem item = new FSListItem(img_url, name+"("+position+")",sub_text);
                                                listItems.add(item);
                                            }
                                            /*
                                            if (len == icon_notfound_count) {
                                                FSBubbleLayout.setVisibility(View.VISIBLE);
                                            }
                                            */
                                        }
                                    } else{
                                        //サーバから何も帰ってこなかった場合
                                        list_array.add("取得に失敗しました");
                                        name_array.add("0");
                                        position_array.add("0");
                                        family_id_array.add("0");

                                        FSListItem item = new FSListItem(MyURLManager.get_main_url()+"nty_login/notfound.png", "取得に失敗しました","");
                                        listItems.add(item);
                                    }
                                    ArrayAdapter adapter = new FSListAdapter(getApplicationContext(), R.layout.fs_list_item, listItems);
                                    listView.setAdapter(adapter);
                                } else  if (json_res_id == 2) {
                                    //ログイン情報に問題があった場合, 再ログインさせる
                                    Intent intent = new Intent(FamilyStructureActivity.this, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //何らかの問題
                                    sncbar.dispWarning(json_res_message, FamilyStructureActivity.this, v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            RelativeLayout v = findViewById(R.id.rl_login);
                            Context c = FamilyStructureActivity.this;
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

    //家族追加ボタンが押された時
    public void add_func(View v) {
        send_familyinfo();
    }

    //家族情報を追加する関数
    public void send_familyinfo() {
        //キーボード下げる
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        //
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/add_family.php?name="+name.getText().toString()+"&id="+String.valueOf(famid)+"&token="+SP.loadString(this,"token")).build();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RelativeLayout v = findViewById(R.id.rl_login);
                Context c = FamilyStructureActivity.this;
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        send_familyinfo();
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
                                String json_is_first_reg = json.getString("is_first");
                                Log.d("family_debug",res);
                                Log.d("family_debug","fid_"+SP.loadString(FamilyStructureActivity.this,"family_id"));
                                Log.d("family_debug","token_"+SP.loadString(FamilyStructureActivity.this,"token"));
                                if (json_res_id == 1) {
                                    //成功
                                    if (json_is_first_reg.equals("true") && SP.loadString(FamilyStructureActivity.this,"family_id").isEmpty()) {
                                        //初回追加時はメッセージ出す
                                        FSBubbleLayout.setVisibility(View.VISIBLE);
                                        help_textview.setText("次にリストをタップして顔写真を登録してみましょう。");
                                        is_first = true;
                                        //
                                    } else {
                                        sncbar.dispInfo(json_res_message, FamilyStructureActivity.this, v);
                                    }
                                    //editText空にする
                                    name.setText("");
                                    get_family_s_from_server();
                                } else  if (json_res_id == 3) {
                                    //ログイン情報に問題があった場合, 再ログインさせる
                                    Intent intent = new Intent(FamilyStructureActivity.this, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //何らかの問題
                                    sncbar.dispWarning(json_res_message, FamilyStructureActivity.this, v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            RelativeLayout v = findViewById(R.id.rl_login);
                            Context c = FamilyStructureActivity.this;
                            final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                            snackbar.setAction("再試行", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    send_familyinfo();
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

    @Override
    public void onBackPressed() {
        if (where_from.equals("slu")) {
            //ログインユーザ選択画面から来た
            SP.saveString(FamilyStructureActivity.this, "this", "true");
            Intent intent = new Intent(FamilyStructureActivity.this, SelectLoginUserActivity.class);
            startActivity(intent);
        } else if (where_from.equals("mcmk")) {
            //MainCam2から不備で飛ばされた場合は戻れなく
        } else if (where_from.equals("mcm")) {
            //MainCam2から来た
            Intent intent = new Intent(FamilyStructureActivity.this, MainCam2Activity.class);
            startActivity(intent);
        } else if (where_from.equals("top")) {
            //MainCam2から来た
            Intent intent = new Intent(FamilyStructureActivity.this, TopActivity.class);
            startActivity(intent);
        } else {
            //設定画面から来たと思われる
            Intent intent = new Intent(FamilyStructureActivity.this, TopActivity.class);
            startActivity(intent);
        }
    }
}