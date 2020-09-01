package xyz.tsumugu2626.app.fami;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

public class CameraActivity extends Activity {

    /*
     * 顔認識, アイコン用の写真を撮影する
     */

    private EZCam cam;
    private TextureView textureView;
    private int imagePicker_select_pos = 0;
    private FrameLayout v;
    private Context c;
    private SnackbarClass sncbar;
    private LayoutInflater inflater;
    private View grid_layout;
    private GridView imagePickerGridView;
    private AlertDialog imagePicker_alert_dialog;
    private String userid;
    private String is_first;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        v = findViewById(R.id.fl_cam);
        c = CameraActivity.this;
        sncbar = new SnackbarClass();
        inflater = LayoutInflater.from(CameraActivity.this);
        grid_layout = inflater.inflate(R.layout.image_picker_grid_view, null);
        imagePickerGridView = grid_layout.findViewById(R.id.m_image_picker_grid_view);
        textureView = findViewById(R.id.textureView);
        cam = new EZCam(CameraActivity.this);
        String id = cam.getCamerasList().get(CameraCharacteristics.LENS_FACING_FRONT);
        //String id = cam.getCamerasList().get(CameraCharacteristics.LENS_FACING_BACK);
        Intent i = getIntent();
        userid = i.getStringExtra("userid");
        is_first = i.getStringExtra("is_first");
        Log.d("family_debug","camera___:"+is_first);
        if (userid == null) {
            startActivity(new Intent(CameraActivity.this,FamilyStructureActivity.class));
        }

        cam.selectCamera(id);
        cam.open(CameraDevice.TEMPLATE_PREVIEW, textureView);
        cam.setCameraCallback(new EZCamCallback() {
            @Override
            public void onCameraReady() {
                cam.setCaptureSetting(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
                cam.startPreview();
            }

            @Override
            public void onPicture(Image image) {
                try {
                    //撮影した写真を保存
                    String fileName = System.currentTimeMillis()+".jpg";
                    File file = new File(getExternalFilesDir(
                            Environment.DIRECTORY_PICTURES), fileName);
                    EZCam.saveImage(image, file);
                    //保存した写真をサーバにアップロードする
                    img_upload(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String message) {
            }
            @Override
            public void onCameraDisconnected() {
            }
        });
        //パーミッションの確認
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    //パーミッションチェック関数
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(CameraActivity.this, CameraActivity.class);
                    i.putExtra("userid",userid);
                    i.putExtra("is_first",is_first);
                    startActivity(i);
                }else{
                    new AlertDialog.Builder(CameraActivity.this)
                            .setMessage("すべてのパーミッションを許可してください")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(CameraActivity.this, new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        cam.close();
        super.onDestroy();
    }

    //顔と思われるものが複数あった場合確定する関数
    public void determine_face_image(String face_img_file_path) {
        if (face_img_file_path != null) {
            // face_img_file_pathは [/var/www/html/nty_img/tmp/filename.jpeg] という形式, filename部分だけを取り出す
            String[] face_img_file_path_splited = face_img_file_path.split("/", 0);
            String face_img_file_name= face_img_file_path_splited[6];
            face_img_file_name = face_img_file_name.replace(".jpeg", "");
            String url = MyURLManager.get_vps_url()+"determine_face_image.php?file_name="+face_img_file_name;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    RelativeLayout v = findViewById(R.id.rl_login);
                    Context c = CameraActivity.this;
                    final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                    snackbar.show();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    int res_code = response.code();
                    String res = response.body().string();
                    Log.d("e-py",res);
                    if (res_code == 200) {
                        try {
                            JSONObject json = new JSONObject(res);
                            int json_res_id = json.getInt("id");
                            String json_res_message = json.getString("message");
                            if (json_res_id == 1) {
                                //成功したらダイアログを表示
                                final Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(CameraActivity.this)
                                                .setTitle("確認")
                                                .setMessage("アップロードが完了しました")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // 家族設定画面に飛ばす
                                                        Intent intent = new Intent(CameraActivity.this,FamilyStructureActivity.class);
                                                        //MainCam2から来た場合はputExtraをつける
                                                        Intent i = getIntent();
                                                        String from = i.getStringExtra("from");
                                                        if (from != null) {
                                                            Log.d("family_debug","cam: "+from);
                                                            if (from.equals("mcmk")) {
                                                                intent.putExtra("from", "mcm");
                                                            } else if (from.equals("slu")) {
                                                                intent.putExtra("from", "slu-k");
                                                            } else {
                                                                intent.putExtra("from",from);
                                                            }
                                                        }
                                                        if (is_first != null) {
                                                            intent.putExtra("is_first", is_first);
                                                        }
                                                        //
                                                        if (from != null) {
                                                            if (from.equals("userinfo")) {
                                                                //UserInfoActivityからだったらTopへ
                                                                Intent intent_u = new Intent(CameraActivity.this, TopActivity.class);
                                                                startActivity(intent_u);
                                                            } else {
                                                                startActivity(intent);
                                                            }
                                                        } else {
                                                            startActivity(intent);
                                                        }
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            } else  {
                                //何らかの問題
                                sncbar.dispWarning("処理で問題が発生しました",CameraActivity.this, v);
                                Log.d("e",json_res_message);
                            }
                            //Log.d("debug_mes",json_res_id+"/"+json_res_message+"/"+json_res_token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        RelativeLayout v = findViewById(R.id.rl_login);
                        Context c = CameraActivity.this;
                        final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                        snackbar.show();
                    }
                }
            });
        }
    }

    //画像をアップロードする関数
    public void img_upload(final File file) {
        //アップロード中表示
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("処理中");
        progressDialog.setMessage("画像のアップロード中です");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.connectTimeout(180, TimeUnit.SECONDS);
        okHttpBuilder.readTimeout(180, TimeUnit.SECONDS);
        okHttpBuilder.writeTimeout(180, TimeUnit.SECONDS);
        OkHttpClient client = okHttpBuilder.build();

        String img_post_url = MyURLManager.get_vps_url();
        MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpg");
        RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user_id", SP.loadString(CameraActivity.this,"token"))
                .addFormDataPart("family_id", userid)
                .addFormDataPart("upfile",file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file))
                .build();

        Request request = new Request.Builder()
                .url(img_post_url+"upload.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                final Snackbar snackbar = Snackbar.make(v, "通信エラー発生", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(c, R.color.colorSnackbarText));
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(c, R.color.colorSnackbarBG));
                snackbar.setAction("再試行", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        img_upload(file);
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                progressDialog.dismiss();
                int res_code = response.code();
                String res = response.body().string();
                if (res_code == 200) {
                    try {
                        final JSONObject json = new JSONObject(res);
                        int json_res_id = json.getInt("id");
                        String json_res_message = json.getString("message");
                        if (json_res_id == 1) {
                            //アップロード成功
                            final JSONArray face_json_array = json.getJSONArray("face_list");
                            //1枚だけだったらそのまま設定
                            if (face_json_array.length() == 1) {
                                //画像設定処理
                                determine_face_image(face_json_array.getString(0));
                            } else {
                                //2つ以上あったらpicker表示
                                ArrayList<ImagePickerListItem> listItems = new ArrayList<>();
                                for (int i=0; i < face_json_array.length(); i++) {
                                    String[] face_img_file_path_splited = face_json_array.getString(i).split("/", 0);
                                    String face_img_file_name= face_img_file_path_splited[6];
                                    String img_url = MyURLManager.get_vps_url()+"tmp/"+face_img_file_name;
                                    ImagePickerListItem item = new ImagePickerListItem(img_url, (float)0);
                                    listItems.add(item);
                                }
                                ArrayAdapter adapter = new ImagePickerListAdapter(CameraActivity.this, R.layout.image_picker_item_layout, listItems);
                                imagePickerGridView.setAdapter(adapter);
                                imagePickerGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        imagePicker_select_pos = position;
                                        try {
                                            //pickerダイアログ閉じる
                                            if (imagePicker_alert_dialog != null) {
                                                imagePicker_alert_dialog.dismiss();
                                            }
                                            //画像設定処理
                                            determine_face_image(face_json_array.getString(position));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                //pickerのダイアログ表示
                                final Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imagePicker_alert_dialog = new AlertDialog.Builder(CameraActivity.this)
                                                .setMessage("顔が複数検出されました。選択してください")
                                                .setNegativeButton("撮り直す", null)
                                                .setView(grid_layout)
                                                .setCancelable(false)
                                                .show();
                                    }
                                });
                            }
                        } else  {
                            //何らかの問題
                            sncbar.dispWarning(json_res_message,CameraActivity.this, v);
                            Log.d("debug_mes",json_res_id+"/"+json_res_message);
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
                            img_upload(file);
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
    }

    public void takePicture(View v) {
        cam.takePicture();
    }

}
