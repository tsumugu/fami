package xyz.tsumugu2626.app.fami;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoiceRecord extends AppCompatActivity {

    /*
     * 音声メッセージを録音する画面
    */

    private MediaRecorder rec;
    private SnackbarClass sncbar;
    private RelativeLayout v;
    private ImageButton btn;
    private TextView sec_txt_view;
    private ProgressDialog progressDialog;
    LinearLayout HelpBubble;
    String fileName;
    Timer t;
    int sec;
    Context c;
    ArrayList list_array;
    ArrayList name_array;
    ArrayList position_array;
    ArrayList family_id_array;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);

        btn = findViewById(R.id.recordButton);
        sec_txt_view = findViewById(R.id.secTextView);
        HelpBubble = findViewById(R.id.HelpBubbleLayout);
        c = this;
        v = findViewById(R.id.vrAct);
        sncbar = new SnackbarClass();
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //1. 押し始めたら録音開始
                    try {
                        //ボタンのアイコン変更
                        btn.setImageResource(R.drawable.rec_button_red);
                        //録音処理
                        rec = new MediaRecorder();
                        rec.setAudioSource(MediaRecorder.AudioSource.MIC);
                        rec.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        rec.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        fileName = getExternalFilesDir(Environment.DIRECTORY_MUSIC)+"/"+System.currentTimeMillis()+".wav";
                        rec.setOutputFile(fileName);
                        rec.prepare();
                        rec.start();
                        final Handler mainHandler = new Handler(Looper.getMainLooper());
                        t = new Timer();
                        sec = 0;
                        t.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                //sec/60
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        sec++;
                                        String m = String.valueOf((sec/60)%60);
                                        String s = String.valueOf(sec % 60);
                                        if (m.length() == 1) {
                                            m = "0"+m;
                                        }
                                        if (s.length() == 1) {
                                            s = "0"+s;
                                        }
                                        sec_txt_view.setText(m+":"+s);
                                    }
                                });
                            }
                        },  0, 1000);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    //2. 離したら録音終了
                    try {
                        //ボタンのアイコン変更
                        btn.setImageResource(R.drawable.rec_button_red_circle);
                        //停止処理
                        rec.stop();
                        t.cancel();
                        sec_txt_view.setText("00:00");
                        //確認画面表示
                        check_voice();
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        //パーミッション確認
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 0);
        }

        //ヘルプの非表示
        if (SP.loadString(this,"voice_help").equals("true")) {
            HelpBubble.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(VoiceRecord.this, VoiceRecord.class);
                    startActivity(i);
                }else{
                    new AlertDialog.Builder(VoiceRecord.this)
                            .setMessage("すべてのパーミッションを許可してください")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(VoiceRecord.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 0);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;
            }
        }
    }

    public void check_voice() {
        //音声をチェック
        //吹き出しの非表示
        SP.saveString(this,"voice_help","true");
        HelpBubble.setVisibility(View.GONE);
        //1.wavの再生時間,pathをチェック
        File file = new File(fileName);
        if (file.exists()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            final View dialog_check_view = inflater.inflate(R.layout.voice_check, null);
            final MediaPlayer mp = new MediaPlayer();
            try {
                mp.setDataSource(fileName);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            TextView play_time_textview = dialog_check_view.findViewById(R.id.playTimeTextView);
            final ImageButton play_button = dialog_check_view.findViewById(R.id.playButton);
            ImageButton replay_button = dialog_check_view.findViewById(R.id.replayButton);
            //再生時間表示
            int wav_sec = mp.getDuration()/1000;
            String m = String.valueOf((wav_sec/60)%60);
            String s = String.valueOf(wav_sec % 60);
            if (s.length() == 1) {
                s = "0"+s;
            }
            play_time_textview.setText(m+":"+s);
            //ButtonのOnclick
            play_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mp.isPlaying()) {
                        mp.pause();
                        play_button.setImageResource(R.drawable.baseline_play_arrow_white_24dp);
                    } else {
                        mp.start();
                        play_button.setImageResource(R.drawable.baseline_pause_white_24dp);
                    }
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    play_button.setImageResource(R.drawable.baseline_play_arrow_white_24dp);
                }
            });
            replay_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mp.start();
                }
            });
            new AlertDialog.Builder(this)
                    .setMessage("音声の確認")
                    .setPositiveButton("これでOK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //OKだったら送信ユーザを選択するダイアログを表示
                            select_send_user_and_opt();
                        }
                    })
                    .setNegativeButton("再度録音する", null)
                    .setView(dialog_check_view)
                    .setCancelable(false)
                    .show();
            //再生する
            //mp.start();
        } else {
            sncbar.dispWarning("ファイルが正しく保存出来ませんでした", c, v);
        }
    }

    public void select_send_user_and_opt() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        list_array = new ArrayList<>();
        name_array = new ArrayList<>();
        position_array = new ArrayList<>();
        family_id_array = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/get_family.php?token="+SP.loadString(this,"token")).build();
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
                                snackbar.dismiss();
                                select_send_user_and_opt();
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
                    @Override
                    public void run() {
                        //
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
                                            list_array.add("家族が登録されていません。下から追加してください");
                                            name_array.add("0");
                                            position_array.add("0");
                                            family_id_array.add("0");
                                        } else {
                                            list_array.add("家族のだれか");
                                            name_array.add("1");
                                            position_array.add("1");
                                            family_id_array.add("1");
                                            for(int i=0;i<len;i++){
                                                JSONObject info = array.getJSONObject(i);
                                                String name = info.getString("name");
                                                String position = info.getString("position");
                                                String family_id = info.getString("family_id");
                                                name_array.add(name);
                                                position_array.add(position);
                                                family_id_array.add(family_id);
                                                list_array.add(name+"("+position+")");
                                            }
                                        }
                                    } else{
                                        list_array.add("取得に失敗しました");
                                        name_array.add("0");
                                        position_array.add("0");
                                        family_id_array.add("0");
                                    }
                                    //list_arrayをnameSpinnerに設定
                                    LayoutInflater inflater = LayoutInflater.from(VoiceRecord.this);
                                    View dialog_view_voice_prog_setting = inflater.inflate(R.layout.voice_prog_setting, null);
                                    //spinnerに設定
                                    Spinner dateSpinner = dialog_view_voice_prog_setting.findViewById(R.id.dateSpinner);
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                            c,
                                            R.layout.custom_spinner_voice,
                                            getResources().getStringArray(R.array.voice_prog_date)
                                    );
                                    adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                                    dateSpinner.setAdapter(adapter);
                                    final Spinner optSpinner = dialog_view_voice_prog_setting.findViewById(R.id.OptionSpinner);
                                    ArrayAdapter<String> adapter_2 = new ArrayAdapter<>(
                                            c,
                                            R.layout.custom_spinner_voice,
                                            getResources().getStringArray(R.array.voice_prog_opt)
                                    );
                                    adapter_2.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                                    optSpinner.setAdapter(adapter_2);
                                    Spinner nameSpinner = dialog_view_voice_prog_setting.findViewById(R.id.nameSpinner);
                                    //list_arrayをnameSpinnerに設定
                                    ArrayAdapter<String> adapter_3 = new ArrayAdapter<>(
                                            c,
                                            R.layout.custom_spinner_voice,
                                            list_array
                                    );
                                    adapter_3.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                                    nameSpinner.setAdapter(adapter_3);
                                    Spinner timingSpinner = dialog_view_voice_prog_setting.findViewById(R.id.timingSpinner);
                                    ArrayAdapter<String> adapter_4 = new ArrayAdapter<>(
                                            c,
                                            R.layout.custom_spinner_voice,
                                            getResources().getStringArray(R.array.voice_prog_freq)
                                    );
                                    adapter_4.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                                    timingSpinner.setAdapter(adapter_4);
                                    //Spinnerの値取得
                                    final int[] dateSpinner_val = {0};
                                    final int[] optSpinner_val = {0};
                                    final int[] nameSpinner_val = {0};
                                    final int[] timingSpinner_val = {0};
                                    dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            dateSpinner_val[0] = position;
                                            //Log.d("pram_voice_rec", String.valueOf(dateSpinner_val[0])+String.valueOf(optSpinner_val[0])+String.valueOf(nameSpinner_val[0])+String.valueOf(timingSpinner_val[0]));
                                        }
                                        public void onNothingSelected(AdapterView<?> parent) {
                                        }
                                    });
                                    optSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            optSpinner_val[0] = position;
                                            //Log.d("pram_voice_rec", String.valueOf(dateSpinner_val[0])+String.valueOf(optSpinner_val[0])+String.valueOf(nameSpinner_val[0])+String.valueOf(timingSpinner_val[0]));
                                        }
                                        public void onNothingSelected(AdapterView<?> parent) {
                                        }
                                    });
                                    nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            nameSpinner_val[0] = position;
                                            //Log.d("pram_voice_rec", String.valueOf(dateSpinner_val[0])+String.valueOf(optSpinner_val[0])+String.valueOf(nameSpinner_val[0])+String.valueOf(timingSpinner_val[0]));
                                        }
                                        public void onNothingSelected(AdapterView<?> parent) {
                                        }
                                    });
                                    timingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                           timingSpinner_val[0] = position;
                                            //Log.d("pram_voice_rec", String.valueOf(dateSpinner_val[0])+String.valueOf(optSpinner_val[0])+String.valueOf(nameSpinner_val[0])+String.valueOf(timingSpinner_val[0]));
                                        }
                                        public void onNothingSelected(AdapterView<?> parent) {
                                        }
                                    });
                                    //ダイアログ
                                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                    builder.setTitle("設定");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //パラメータを取得
                                            //Log.d("pram_voice_rec", String.valueOf(dateSpinner_val[0])+String.valueOf(optSpinner_val[0])+String.valueOf(nameSpinner_val[0])+String.valueOf(timingSpinner_val[0]));
                                            //音声ファイルをアップロード
                                            String setting_val = String.valueOf(dateSpinner_val[0])+","+String.valueOf(optSpinner_val[0])+","+String.valueOf(nameSpinner_val[0])+","+String.valueOf(timingSpinner_val[0]);
                                            String family_id = family_id_array.get(nameSpinner_val[0]).toString();
                                            file_upload(fileName,family_id,setting_val);
                                            dialog.dismiss();
                                            //
                                        }
                                    });
                                    builder.setNegativeButton("キャンセル", null);
                                    builder.setView(dialog_view_voice_prog_setting);
                                    final AlertDialog dialog = builder.create();
                                    dialog.show();
                                } else  if (json_res_id == 2) {
                                    //問題があるので再ログイン
                                    Intent intent = new Intent(c, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //何らかの問題
                                    sncbar.dispWarning(json_res_message, c, v);
                                }
                                progressDialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            progressDialog.dismiss();
                            final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                            snackbar.setAction("再試行", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    select_send_user_and_opt();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
            }
        });
    }

    public void file_upload(final String file_path, final String to_fam_id, final String params) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        final File file = new File(file_path);
        OkHttpClient client = new OkHttpClient();
        String img_post_url = MyURLManager.get_main_url()+"nty_login/api/upload_audio.php";
        MediaType MEDIA_TYPE = MediaType.parse("audio/x-wav");
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", SP.loadString(this,"token"))
                .addFormDataPart("from_user_id", SP.loadString(this,"family_id"))
                .addFormDataPart("family_id", to_fam_id)
                .addFormDataPart("param", params)
                .addFormDataPart("upfile",file.getName(), RequestBody.create(MEDIA_TYPE, file))
                .build();
        Request request = new Request.Builder()
                .url(img_post_url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("con_debug","falled:"+e.getMessage());
                //通信失敗
                file.delete();
                progressDialog.dismiss();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int res_code = response.code();
                String res = response.body().string();
                Log.d("con_debug",res);
                if (res_code == 200) {
                    try {
                        JSONObject json = new JSONObject(res);
                        int json_res_id = json.getInt("id");
                        String json_res_message = json.getString("message");
                        if (json_res_id == 1) {
                            //TOPに
                            //Intent intent = new Intent(c,TopActivity.class);
                            //intent.putExtra("from","audio_succeed");
                            //startActivity(intent);
                            sncbar.dispInfo("設定が完了しました",c, v);
                        } else  if (json_res_id == 2 || json_res_id == 3) {
                            //問題があるので再ログイン
                            Intent intent = new Intent(c, LoginActivity.class);
                            intent.putExtra("from","fs");
                            startActivity(intent);
                        } else  {
                            //何らかの問題
                            sncbar.dispWarning("サーバで問題が発生しました", c, v);
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
                            file_upload(file_path, to_fam_id, params);
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

    public void goto_voice_manage(View v) {
        Intent intent = new Intent(this, ManageVoiceMessageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(VoiceRecord.this, TopActivity.class);
        startActivity(intent);
    }

}
