package xyz.tsumugu2626.app.fami;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;
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

public class TopActivity extends AppCompatActivity {

    /*
    * Top画面
    */

    private RelativeLayout v;
    private Context c;
    private SnackbarClass sncbar;
    private Snackbar snackbar;
    private GridView gridView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private long boot_timestamp;
    ArrayList<TopListItem> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_new_layout);

        v = findViewById(R.id.rl_top);
        gridView = findViewById(R.id.grdView);
        navigationView = findViewById(R.id.navView);
        drawerLayout = findViewById(R.id.drawer_layout);
        boot_timestamp = System.currentTimeMillis();
        c = TopActivity.this;
        sncbar = new SnackbarClass();
        listItems = new ArrayList<>();
        snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);
        // リストが押された時
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TopListItem item = listItems.get(position);
                Log.d("top_debug",item.getName()+" / "+item.getThumbnail());
                if (item != null) {
                    Intent intent = new Intent(c, UserInfoActivity.class);
                    intent.putExtra("name",item.getName());
                    intent.putExtra("image_url",item.getThumbnail());
                    intent.putExtra("uid",item.getUid());
                    startActivity(intent);
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch(menuItem.getItemId()) {
                            case R.id.menu_add_family:
                               //家族追加削除
                                drawerLayout.closeDrawer(navigationView);
                                Intent intent = new Intent(TopActivity.this,FamilyStructureActivity.class);
                                intent.putExtra("from","top");
                                startActivity(intent);
                                return true;
                            case R.id.menu_change_mode:
                                //モード変更
                                drawerLayout.closeDrawer(navigationView);
                                intent_to(SelectTypeActivity.class);
                                return true;
                            case R.id.menu_select_login_user:
                                //ログインユーザ変更
                                drawerLayout.closeDrawer(navigationView);
                                intent_to(SelectLoginUserActivity.class);
                                return true;
                            case R.id.menu_view_voice_message:
                                //ログインユーザ変更
                                drawerLayout.closeDrawer(navigationView);
                                intent_to(ManageVoiceMessageActivity.class);
                                return true;
                            case R.id.menu_logout:
                                //ログアウト
                                drawerLayout.closeDrawer(navigationView);
                                SP.saveString(TopActivity.this,"token","");
                                SP.saveString(TopActivity.this,"family_id","");
                                SP.saveString(TopActivity.this,"type","");
                                intent_to(BootActivity.class);
                                return true;
                            case R.id.menu_about:
                                //このアプリについて
                                drawerLayout.closeDrawer(navigationView);
                                intent_to(AboutActivity.class);
                                return true;
                        }
                        return false;
                    }
                }
        );
        //不審者情報を取得する
        get_spi(SP.loadString(TopActivity.this,"token"));
        //家族情報を取得
        get_family_s_from_server();
    }

    //Listが引っ張られた時の処理
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mSwipeRefreshLayout.setRefreshing(false);
            get_family_s_from_server();
        }
    };

    //不審者情報を取得する
    public void get_spi(final String family_id) {
        //不審者情報を取得する
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_vps_url()+"diff/get-unknown.php?familyid="+family_id).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RelativeLayout v = findViewById(R.id.rl_login);
                Context c = TopActivity.this;
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        get_spi(family_id);
                    }
                });
                snackbar.show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int res_code = response.code();
                String res = response.body().string();
                if (res_code == 200) {
                    //成功
                    try {
                        JSONObject json = new JSONObject(res);
                        int json_res_count = json.getInt("count");
                        if (json_res_count>0) {
                            //移動
                            Intent i = new Intent(TopActivity.this, SuspiciousPersonInfoActivity.class);
                            i.putExtra("json",res);
                            startActivity(i);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    RelativeLayout v = findViewById(R.id.rl_login);
                    Context c = TopActivity.this;
                    final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                    snackbar.setAction("再試行", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            get_spi(family_id);
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
        //
    }

    public void get_family_s_from_server() {
        snackbar.dismiss();
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
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
                        Context c = TopActivity.this;
                        snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                        snackbar.setAction("再試行", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                get_family_s_from_server();
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
                                    if (json.getJSONArray("family_list") != null) {
                                        JSONArray array = json.getJSONArray("family_list");
                                        int len = array.length();
                                        if (len == 0) {
                                            //家族0人
                                            //sncbar.dispInfo("家族が登録されていません",TopActivity.this,v);
                                            new AlertDialog.Builder(TopActivity.this)
                                                    .setMessage("家族が登録されていません")
                                                    .setPositiveButton("設定画面へ", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent intent = new Intent(TopActivity.this, FamilyStructureActivity.class);
                                                            intent.putExtra("from","top");
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .setCancelable(false)
                                                    .show();
                                        } else {
                                            String my_family_id = SP.loadString(TopActivity.this,"family_id");
                                            ArrayList<TopListItem> listItems_tmp = new ArrayList<>();
                                            for(int i=0;i<len;i++){
                                                JSONObject info = array.getJSONObject(i);
                                                String name = info.getString("name");
                                                String img_url = info.getString("img_url");
                                                String json_family_id = info.getString("family_id");
                                                String json_io_time = info.getString("io_time");
                                                String json_status =info.getString("status");
                                                String img_post_url = MyURLManager.get_vps_url()+"uploads/";
                                                //final String img_post_url_final = img_post_url+SP.loadString(TopActivity.this,"token")+"-"+json_family_id+".jpeg";
                                                if (img_url.equals("notfound")) {
                                                    img_url = MyURLManager.get_main_url()+"nty_login/notfound.png";
                                                } else {
                                                    img_url = img_post_url+SP.loadString(TopActivity.this,"token")+"-"+json_family_id+".jpeg";
                                                    // キャッシュ削除のためtimestampをつける
                                                    img_url = img_url+"?time="+boot_timestamp;
                                                }
                                                boolean is_color_gray = false;
                                                if (json_status != null) {
                                                    if (json_status.equals("1")) {
                                                        is_color_gray = true;
                                                    }
                                                }
                                                TopListItem item = new TopListItem(img_url,name,json_family_id,is_color_gray,false);
                                                listItems_tmp.add(item);
                                            }
                                            //s: 人数
                                            int s = listItems_tmp.size();
                                            //並び順を変える
                                            ArrayList<TopListItem> listItems_tmp_true = new ArrayList<>();
                                            ArrayList<TopListItem> listItems_tmp_false = new ArrayList<>();
                                            for (int i=0; i<s; i++) {
                                                TopListItem item = listItems_tmp.get(i);
                                                if (item.getIsGettingOut()) {
                                                    //外出
                                                    listItems_tmp_true.add(new TopListItem(item.getThumbnail(),item.getName(),item.getUid(),item.getIsGettingOut(),false));
                                                } else {
                                                    //在宅
                                                    listItems_tmp_false.add(new TopListItem(item.getThumbnail(),item.getName(),item.getUid(),item.getIsGettingOut(),false));
                                                }
                                            }
                                            listItems_tmp.clear();
                                            listItems_tmp.addAll(listItems_tmp_false);
                                            listItems_tmp.addAll(listItems_tmp_true);
                                            //配置する処理
                                            //人数に応じた配置を取得
                                            int[] top_arrangement_arr = MyTopArrangementManager.get_top_arrangement(s);
                                            int alr_int = 0;
                                            if (top_arrangement_arr != null) {
                                                for (int i=0;i<9;i++) {
                                                    int num = top_arrangement_arr[i];
                                                    if (num==0||num==9) {
                                                        TopListItem item = new TopListItem("","","",false,true);
                                                        listItems.add(item);
                                                    } else {
                                                        TopListItem item_tmp = listItems_tmp.get(alr_int);
                                                        TopListItem item = new TopListItem(item_tmp.getThumbnail(),item_tmp.getName(),item_tmp.getUid(),item_tmp.getIsGettingOut(),false);
                                                        listItems.add(item);
                                                        alr_int++;
                                                    }
                                                }
                                            } else {
                                                //8人以上は減らしてくれダイアログ
                                                new AlertDialog.Builder(TopActivity.this)
                                                        .setMessage("家族を削除して、人数を8人以下にしてください")
                                                        .setPositiveButton("設定画面へ", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent(TopActivity.this, FamilyStructureActivity.class);
                                                                intent.putExtra("from","top");
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setCancelable(false)
                                                        .show();
                                            }
                                            //gridViewに設定
                                            ArrayAdapter adapter = new TopListAdapter(getApplicationContext(), R.layout.cell_top_new, listItems);
                                            gridView.setAdapter(adapter);
                                        }
                                    } else{
                                        //取得失敗
                                        sncbar.dispWarning("取得に失敗しました", TopActivity.this, v);
                                    }
                                } else  if (json_res_id == 2) {
                                    //問題があるので再ログイン
                                    Intent intent = new Intent(TopActivity.this, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //何らかの問題
                                    sncbar.dispWarning(json_res_message, TopActivity.this, v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            RelativeLayout v = findViewById(R.id.rl_login);
                            Context c = TopActivity.this;
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

    public void post_firebase_token(final String f_token, final String family_id, final String m_token) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/save_firebase_token.php?familyid="+family_id+"&userid="+m_token+"&token="+f_token).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RelativeLayout v = findViewById(R.id.rl_login);
                Context c = TopActivity.this;
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post_firebase_token(f_token,family_id,m_token);
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
                    //成功
                } else {
                    RelativeLayout v = findViewById(R.id.rl_login);
                    Context c = TopActivity.this;
                    final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                    snackbar.setAction("再試行", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            post_firebase_token(f_token,family_id,m_token);
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

    //マイクボタンが押された時の処理
    public void goto_record(View v) {
        intent_to(VoiceRecord.class);
    }

    //3本線アイコンが押された時の処理
    public void close_button(View v) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    public void intent_to(Class c) {
        Intent intent = new Intent(this,c);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

}