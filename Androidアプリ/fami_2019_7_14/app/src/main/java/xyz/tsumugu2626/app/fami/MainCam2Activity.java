package xyz.tsumugu2626.app.fami;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainCam2Activity extends AppCompatActivity {

    /*
     *  家に設置するときの画面。帰宅、外出検知など
    */

    private TextureView textureView;
    private TextView prog_textview;
    private ImageView debug_image_view;
    private EZCam cam;
    private String id;
    private SnackbarClass sncbar;
    private FrameLayout v;
    private long last_upload_current_time_millis = 0;
    private long process_fin_count = 0;
    private FirebaseVisionFaceDetectorOptions realTimeOpts;
    private FirebaseVisionFaceDetector detector;
    private Handler face_detect_handler;
    private Runnable face_detect_runnable;
    private ArrayList<String> play_wait_listItems;
    private MediaPlayer se_media_p;
    private MediaPlayer media_p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cam2);

        sncbar = new SnackbarClass();
        v = findViewById(R.id.fl_mc2);
        prog_textview = findViewById(R.id.prog_textview);

        play_wait_listItems = new ArrayList<>();
        /*
        realTimeOpts = new FirebaseVisionFaceDetectorOptions.Builder()
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build();
                */
        realTimeOpts = new FirebaseVisionFaceDetectorOptions.Builder()
                .build();


        detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts);
        textureView = findViewById(R.id.textureView2);
        debug_image_view = findViewById(R.id.debug_iv);
        //カメラの準備
        cam = new EZCam(this);
        id = cam.getCamerasList().get(CameraCharacteristics.LENS_FACING_FRONT);
        cam.selectCamera(id);
        cam.open(CameraDevice.TEMPLATE_PREVIEW, textureView);
        cam.setCameraCallback(new EZCamCallback() {
            @Override
            public void onCameraReady() {
                cam.setCaptureSetting(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
                cam.startPreview();
                //cam.takePicture();
            }
            @Override
            public void onPicture(final Image image) {image.close();}
            @Override
            public void onError(String message) {}
            @Override
            public void onCameraDisconnected() {}
        });
        //SEのセット
        try {
            media_p = new MediaPlayer();
            se_media_p = new MediaPlayer();
            AssetFileDescriptor descriptor = getAssets().openFd("se_notice_voice.mp3");
            se_media_p.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            se_media_p.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //se_media_p.start();

    }

    @Override
    protected void onResume() {
        //開始する
        //一定時間ごとに顔検出
        face_detect_handler = new Handler();
        face_detect_runnable = new Runnable() {
            @Override
            public void run() {
                //TextureViewからbitmapを取得
                final Bitmap bitmap = textureView.getBitmap();
                if (bitmap != null) {
                    //FirebaseMLKitで顔検出
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                    detector.detectInImage(image).addOnSuccessListener(
                            new OnSuccessListener<List<FirebaseVisionFace>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionFace> faces) {
                                    //debug_image_view.setImageBitmap(bitmap);
                                    //顔の個数
                                    int face_count = faces.size();
                                    //顔が0個でなかったら
                                    if (face_count > 0) {
                                        prog_textview.setText("顔を検出しました\n処理回数: "+process_fin_count);
                                        //前回のUPから10秒は開ける
                                        long now_current_time_millis = System.currentTimeMillis();
                                        if (last_upload_current_time_millis == 0 || (now_current_time_millis-last_upload_current_time_millis) > 8*1000) {
                                            sncbar.dispInfo("アップロード中です",MainCam2Activity.this,v);
                                            //アップロード
                                            img_upload(bitmap);
                                            last_upload_current_time_millis = now_current_time_millis;
                                        }
                                    } else {
                                        prog_textview.setText("\n処理回数: "+process_fin_count);
                                    }
                                    process_fin_count++;
                                    //bitmap解放
                                    bitmap.recycle();
                                }
                            });
                }
                //毎秒処理
                face_detect_handler.postDelayed(this, 2000);
            }
        };
        //顔写真が設定されていないユーザが一人でもいたら、設定画面に飛ぶか確認ダイアログを表示。
        // この関数によって上の顔検出処理が開始される
        check_image_set();
        //パーミッションの確認
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(MainCam2Activity.this,MainCam2Activity.class);
                    startActivity(i);
                }else{
                    new AlertDialog.Builder(MainCam2Activity.this)
                            .setMessage("すべてのパーミッションを許可してください")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainCam2Activity.this, new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;
            }
        }
    }

    //顔写真の確認を行う関数
    public void check_image_set() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/get_family.php?token="+SP.loadString(this,"token")).build();
        client.newCall(request).enqueue(new Callback() {
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Error
                        sncbar.dispWarning("通信エラーが発生しました",MainCam2Activity.this,v);
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
                                        int icon_notfound_count = 0;
                                        if (len == 0) {
                                            //家族未登録だったら登録画面へ
                                            Intent intent = new Intent(MainCam2Activity.this, FamilyStructureActivity.class);
                                            intent.putExtra("from","mcmk");
                                            startActivity(intent);
                                        } else {
                                            for(int i=0;i<len;i++){
                                                JSONObject info = array.getJSONObject(i);
                                                String img_url = info.getString("img_url");
                                                if (img_url.equals("notfound")) {
                                                    icon_notfound_count++;
                                                }
                                            }
                                        }
                                        if (icon_notfound_count != 0) {
                                            //全員未登録だったら登録画面へ
                                            if (len == icon_notfound_count) {
                                                new AlertDialog.Builder(MainCam2Activity.this)
                                                        .setMessage("1人も顔写真が登録されていません。登録してください")
                                                        .setPositiveButton("登録画面に移動する", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                cam.close();
                                                                Intent intent = new Intent(MainCam2Activity.this, FamilyStructureActivity.class);
                                                                intent.putExtra("from","mcmk");
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setCancelable(false)
                                                        .show();
                                            } else {
                                                //登録画面へのダイアログ
                                                new AlertDialog.Builder(MainCam2Activity.this)
                                                        .setMessage("顔写真が登録されていないユーザがいます。設定画面に移動しますか？")
                                                        .setPositiveButton("移動する", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                cam.close();
                                                                Intent intent = new Intent(MainCam2Activity.this, FamilyStructureActivity.class);
                                                                intent.putExtra("from","mcm");
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //顔検出を開始する
                                                                face_detect_handler.post(face_detect_runnable);
                                                            }
                                                        })
                                                        .setCancelable(false)
                                                        .show();
                                            }
                                        } else {
                                            //顔検出を開始する
                                            face_detect_handler.post(face_detect_runnable);
                                        }
                                    } else{
                                        //取得失敗
                                        sncbar.dispWarning("取得に失敗しました", MainCam2Activity.this, v);
                                    }
                                } else  if (json_res_id == 2) {
                                    //問題があるので再ログイン
                                    Intent intent = new Intent(MainCam2Activity.this, LoginActivity.class);
                                    intent.putExtra("from","fs");
                                    startActivity(intent);
                                } else  {
                                    //何らかの問題
                                    sncbar.dispWarning(json_res_message, MainCam2Activity.this, v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //Error
                            sncbar.dispWarning("エラーが発生しました",MainCam2Activity.this,v);
                        }
                    }
                });
            }
        });
    }

    //画像をアップロードする関数
    public void img_upload(final Bitmap bitmap) {
        // Bitmapを保存する
        final String fileName = System.currentTimeMillis()+".jpg";
        final File file = new File(getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), fileName);
        try {
            FileOutputStream out = new FileOutputStream(file.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        //アップロードする
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.connectTimeout(180, TimeUnit.SECONDS);
        okHttpBuilder.readTimeout(180, TimeUnit.SECONDS);
        okHttpBuilder.writeTimeout(180, TimeUnit.SECONDS);
        OkHttpClient client = okHttpBuilder.build();
        String img_post_url = MyURLManager.get_vps_url()+"diff/";
        MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpg");
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("user_id", SP.loadString(this,"token"))
                .addFormDataPart("family_id",SP.loadString(this,"token"))
                .addFormDataPart("upfile",file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file))
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
                sncbar.dispWarning("通信に失敗しました",MainCam2Activity.this,v);
                //通信失敗
                file.delete();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int res_code = response.code();
                final String res = response.body().string();
                Log.d("con_debug",file.getName()+res);
                file.delete();
                if (res_code == 200) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = new JSONObject(res);
                                int json_res_id = json.getInt("id");
                                String json_res_message = json.getString("message");
                                JSONArray user_json_array = json.getJSONArray("user_list");
                                if (json_res_id == 1) {
                                    //複数人検知した場合一人一人処理
                                    int len = user_json_array.length();
                                    if (len == 0) {
                                        //顔が検出されなかったとき
                                        sncbar.dispAttention("処理しましたが、人物が見つかりませんでした",MainCam2Activity.this,v);
                                    } else {
                                        for (int i = 0; i < len; i++) {
                                            JSONObject user_json = new JSONObject(String.valueOf(user_json_array.get(i)));
                                            final String json_res_user_id = user_json.getString("userid");
                                            final String json_res_name = user_json.getString("name");
                                            final String json_res_is_dont_know_people = user_json.getString("is_dont_know_people");
                                            final String json_res_io_status = user_json.getString("io_status");
                                            Log.d("con_debug",json_res_is_dont_know_people);
                                            if (json_res_is_dont_know_people.equals("true")) {
                                                //未登録者
                                                sncbar.dispWarning("未登録の人物を検出しました。不審者の可能性があります",MainCam2Activity.this,v);
                                            } else {
                                                //Snackbar表示
                                                String disp_text;
                                                if (json_res_io_status.equals("0")) {
                                                    disp_text = "【帰宅】";
                                                } else if (json_res_io_status.equals("1")) {
                                                    disp_text = "【外出】";
                                                } else {
                                                    disp_text = "";
                                                }
                                                disp_text = disp_text+json_res_name;
                                                sncbar.dispInfo(disp_text,MainCam2Activity.this,v);
                                                //
                                                Log.d("sound_debug_st",json_res_io_status);
                                                //eのときは前回の処理から30秒後制限にひっかかってる
                                                if (!json_res_io_status.equals("e")) {
                                                    //音声メッセージあるか問い合わせ、あったら再生
                                                    OkHttpClient client = new OkHttpClient();
                                                    Request request = new Request.Builder().url(MyURLManager.get_main_url()+"nty_login/api/check_audio.php?user_id="+json_res_user_id+"&family_id="+SP.loadString(MainCam2Activity.this, "token")+"&io_status="+json_res_io_status).build();
                                                    client.newCall(request).enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(Call call, IOException e) {
                                                            sncbar.dispWarning("通信に失敗しました",MainCam2Activity.this,v);
                                                        }
                                                        @Override
                                                        public void onResponse(Call call, Response response) throws IOException {
                                                            int res_code = response.code();
                                                            String res = response.body().string();
                                                            if (res_code == 200) {
                                                                try {
                                                                    JSONObject json = new JSONObject(res);
                                                                    int json_res_id = json.getInt("id");
                                                                    String json_res_message = json.getString("message");
                                                                    Log.d("sound_debug",res);
                                                                    if (json_res_id == 1) {
                                                                        if (json_res_message.equals("y")) {
                                                                            //音声メッセージのwavダウンロード&再生する
                                                                            if (json.getJSONArray("download_file_name_list") != null) {
                                                                                JSONArray array = json.getJSONArray("download_file_name_list");
                                                                                int len = array.length();
                                                                                if (len != 0) {
                                                                                    //音声メッセージあるよっていう効果音を再生
                                                                                    se_media_p.start();
                                                                                    for (int i = 0; i < len; i++) {
                                                                                        JSONObject info = array.getJSONObject(i);
                                                                                        String name = info.getString("file_name");
                                                                                        //サーバ上の音声ファイルのURL
                                                                                        String download_file_url = MyURLManager.get_main_url()+"nty_login/up_audio/"+name+".wav";
                                                                                        //再生待ちList的なのにぶち込む
                                                                                        play_wait_listItems.add(download_file_url);
                                                                                    }
                                                                                    //再生関数呼び出し
                                                                                    if (!media_p.isPlaying()) {
                                                                                        play_audio_messages();
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                    });
                                                    //
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    sncbar.dispWarning("サーバでの処理に失敗しました",MainCam2Activity.this,v);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    //通信失敗
                    sncbar.dispWarning("処理に失敗しました ["+res_code+"]",MainCam2Activity.this,v);
                }
            }
        });
    }

    public void play_audio_messages() {
        //play_wait_listItemsから取り出して順番に再生していく
        final String download_file_url = play_wait_listItems.get(0);
        //保存して再生
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(download_file_url).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }
                final String save_file_path = getExternalFilesDir(Environment.DIRECTORY_MUSIC)+"/download_"+System.currentTimeMillis()+".wav";
                File newfile = new File(save_file_path);
                newfile.createNewFile();
                if (newfile.exists()) {
                    //ローカル保存
                    FileOutputStream fos = new FileOutputStream(save_file_path);
                    fos.write(response.body().bytes());
                    fos.close();
                    Log.d("sound_debug","URL: "+download_file_url+"\nPath: "+save_file_path);
                    //再生
                    media_p.reset();
                    media_p.setDataSource(save_file_path);
                    media_p.prepare();
                    media_p.start();
                    //消す
                    play_wait_listItems.remove(0);
                    media_p.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Log.d("sound_debug","OnComplete: count: "+play_wait_listItems.size());
                            if (play_wait_listItems.size() != 0) {
                                //再生待ちまだあったら再帰呼び出し
                                media_p.reset();
                                play_audio_messages();
                            }
                        }
                    });
                } else {
                    Log.e("debug:","file_create_failed");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        face_detect_handler.removeCallbacks(face_detect_runnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cam.close();
        face_detect_handler.removeCallbacks(face_detect_runnable);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        reset_func();
    }

    public void back_func(View v) {
        reset_func();
    }

    public void reset_func() {
        cam.close();
        SP.saveString(this,"type","");
        Intent intent = new Intent(MainCam2Activity.this, SelectTypeActivity.class);
        startActivity(intent);
    }
}
