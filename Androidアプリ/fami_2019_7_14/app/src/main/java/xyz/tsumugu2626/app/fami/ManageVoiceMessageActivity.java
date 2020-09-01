package xyz.tsumugu2626.app.fami;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ManageVoiceMessageActivity extends AppCompatActivity {

    /*
     * 音声メッセージ管理画面
     */

    private Context c;
    private RelativeLayout v;
    private SnackbarClass sncbar;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView lv;
    private String[] date_arr;
    private String[] opt_arr;
    private String[] timing_arr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_voice_message);

        c = this;
        v = findViewById(R.id.rl_mvm);
        sncbar = new SnackbarClass();
        lv = findViewById(R.id.voiceListView);

        mSwipeRefreshLayout = findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                get_messages_from_server();
            }
        });
        mSwipeRefreshLayout.setColorScheme(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);

        //Array
        date_arr = getResources().getStringArray(R.array.voice_prog_date);
        opt_arr = getResources().getStringArray(R.array.voice_prog_opt);
        timing_arr = getResources().getStringArray(R.array.voice_prog_freq);

        //メッセージを取得
        get_messages_from_server();
    }

    public void get_messages_from_server() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/get_all_latest_audio.php?family_id="+SP.loadString(this,"token")).build();
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
                                get_messages_from_server();
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
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void run() {
                        if (res_code == 200) {
                            try {
                                Log.d("voice_debug",res);
                                JSONObject json = new JSONObject(res);
                                int json_res_id = json.getInt("id");
                                String json_res_message = json.getString("message");
                                if (json_res_id == 1) {
                                    if (json.getJSONArray("message_list") != null) {
                                        JSONArray array = json.getJSONArray("message_list");
                                        int len = array.length();
                                        if (len == 0) {
                                            sncbar.dispInfo("メッセージはありません", c, v);
                                        } else {
                                            final ArrayList<MVMListItem> listItems = new ArrayList<>();
                                            int count = 0;
                                            for (int i = 0; i < len; i++) {
                                                JSONObject info = array.getJSONObject(i);
                                                String file_name = info.getString("file_name_short");
                                                String timestamp_text = info.getString("timestamp_text");
                                                String is_played_status = info.getString("is_played_status");
                                                String is_today_played = info.getString("today_played");
                                                String params = info.getString("params");
                                                if (is_played_status.equals("n")) {
                                                    //mes
                                                    String[] params_arr = params.split(",", 0);
                                                    String opt_str = "";
                                                    if (Integer.parseInt(params_arr[1]) != 0) {
                                                        opt_str = opt_arr[Integer.parseInt(params_arr[1])]+"、 ";
                                                    }
                                                    String to_user_name_str = info.getString("to_user_name");
                                                    if (!to_user_name_str.equals("家族のだれか")) {
                                                        to_user_name_str = to_user_name_str+"さん";
                                                    }
                                                    String params_str = "【"+date_arr[Integer.parseInt(params_arr[0])]+"】"+opt_str+to_user_name_str +"が"+timing_arr[Integer.parseInt(params_arr[3])]+"とき再生";
                                                    if (is_today_played.equals("true")) {
                                                        params_str = "【再生済み】"+params_str;
                                                    }
                                                    //Log.d("voice_debug",params_str);
                                                    String download_file_url = MyURLManager.get_main_url()+"nty_login/up_audio/"+file_name+".wav";
                                                    MVMListItem item = new MVMListItem(info.getString("from_user_name"), info.getString("to_user_name"), download_file_url, file_name, params_str);
                                                    listItems.add(item);
                                                    count++;
                                                }
                                            }
                                            if (count == 0) {
                                                String[] items = { "音声メッセージはありません" };
                                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageVoiceMessageActivity.this, android.R.layout.simple_list_item_1, items);
                                                lv.setAdapter(adapter);
                                            } else {
                                                ArrayAdapter adapter = new MVMListAdapter(ManageVoiceMessageActivity.this, R.layout.manage_voice_message_item_cell, listItems);
                                                lv.setAdapter(adapter);
                                                final MediaPlayer[] mp = {null};
                                                final int[] before_pos = {0};
                                                //ListView内の2つのボタンのうちどちらが押されたか
                                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                                        switch (view.getId()) {
                                                            case R.id.playButton:
                                                                //再生ボタンが押された時の処理
                                                                final ImageButton play_button = view.findViewById(R.id.playButton);
                                                                if (mp[0] != null && before_pos[0] == position) {
                                                                    if (mp[0].isPlaying()) {
                                                                        mp[0].pause();
                                                                        play_button.setImageResource(R.drawable.baseline_play_arrow_white_24dp);
                                                                    } else {
                                                                        mp[0].start();
                                                                        play_button.setImageResource(R.drawable.baseline_pause_white_24dp);
                                                                    }
                                                                } else {
                                                                    //DL
                                                                    final String save_file_path = getExternalFilesDir(Environment.DIRECTORY_MUSIC)+"/download_mv_"+System.currentTimeMillis()+".wav";
                                                                    OkHttpClient client = new OkHttpClient();
                                                                    Request request = new Request.Builder().url(listItems.get(position).getFileUrl()).build();
                                                                    client.newCall(request).enqueue(new Callback() {
                                                                        public void onFailure(Call call, IOException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        public void onResponse(Call call, Response response) throws IOException {
                                                                            if (!response.isSuccessful()) {
                                                                                sncbar.dispWarning("問題が発生しました",c,v);
                                                                            }
                                                                            File newfile = new File(save_file_path);
                                                                            newfile.createNewFile();
                                                                            if (newfile.exists()) {
                                                                                FileOutputStream fos = new FileOutputStream(save_file_path);
                                                                                fos.write(response.body().bytes());
                                                                                fos.close();
                                                                                //再生
                                                                                try {
                                                                                    mp[0] = new MediaPlayer();
                                                                                    mp[0].setDataSource(newfile.getPath());
                                                                                    mp[0].prepare();
                                                                                    mp[0].start();
                                                                                    play_button.setImageResource(R.drawable.baseline_pause_white_24dp);
                                                                                } catch (IOException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                                mp[0].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                                                    @Override
                                                                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                                                                        play_button.setImageResource(R.drawable.baseline_play_arrow_white_24dp);
                                                                                    }
                                                                                });
                                                                            } else {
                                                                                sncbar.dispWarning("問題が発生しました",c,v);
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                                before_pos[0] = position;
                                                                break;
                                                            case R.id.deleteButton:
                                                                //消去ボタンが押された時
                                                                new AlertDialog.Builder(ManageVoiceMessageActivity.this)
                                                                        .setMessage("本当に削除しますか？")
                                                                        .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                delete_audio(listItems.get(position).getFileName());
                                                                            }
                                                                        })
                                                                        .setNegativeButton("キャンセル", null)
                                                                        .show();
                                                                break;
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                } else  if (json_res_id == 2) {
                                    //メッセージなし
                                    //sncbar.dispInfo("メッセージはありません", c, v);
                                    String[] items = { "音声メッセージはありません" };
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageVoiceMessageActivity.this, android.R.layout.simple_list_item_1, items);
                                    lv.setAdapter(adapter);
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
                                    get_messages_from_server();
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    //メッセージ削除
    public void delete_audio(final String file_name) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/delete_audio.php?filename="+file_name).build();
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
                                delete_audio(file_name);
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
                                    //
                                    get_messages_from_server();
                                    sncbar.dispInfo("削除中です",c,v);
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
                                    delete_audio(file_name);
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

    public void goto_rec(View v) {
        Intent intent = new Intent(ManageVoiceMessageActivity.this, VoiceRecord.class);
        startActivity(intent);
    }
}
