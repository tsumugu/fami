package xyz.tsumugu2626.app.fami;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SuspiciousPersonInfoActivity extends AppCompatActivity {

    /*
    * 不審者情報を表示する
    */
    private TextView time_tv;
    private CircleImageView imgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspicious_person_info);

        time_tv = findViewById(R.id.timeTextView);
        imgv = findViewById(R.id.spiImageView);

        //APIから取得したJSON (TopActivityからのintentに含まれてくる)
        Intent intent = getIntent();
        String json_data = intent.getStringExtra("json");
        if (json_data != null) {
            Log.d("debugaaaaaaa",json_data);
            //最初の一件について処理
            try {
                JSONObject json = new JSONObject(json_data);
                int json_res_count = json.getInt("count");
                if (json_res_count>0) {
                    //一件ずつ処理
                    if (json.getJSONArray("info") != null) {
                        JSONArray array = json.getJSONArray("info");
                        int len = array.length();
                        if (len == 0) {
                            //なぜかOだったら(まあないけど)
                            new AlertDialog.Builder(this)
                                    .setMessage("処理に失敗しました")
                                    .setCancelable(false)
                                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(SuspiciousPersonInfoActivity.this, TopActivity.class);
                                            startActivity(i);
                                        }
                                    });
                        } else {
                            JSONObject info = array.getJSONObject(0);
                            String date = info.getString("date").replace(",","\n");
                            String img_url = MyURLManager.get_vps_url()+info.getString("img_url");
                            String file_name = info.getString("text_file_name");
                            //UIに設定
                            time_tv.setText(date);
                            Glide.with(SuspiciousPersonInfoActivity.this).load(img_url).into(imgv);
                            //処理済みにする
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(MyURLManager.get_vps_url()+"diff/remove-unknown.php?fn="+file_name).build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    //通信エラー
                                    new AlertDialog.Builder(SuspiciousPersonInfoActivity.this)
                                            .setMessage("通信に失敗しました")
                                            .setCancelable(false)
                                            .setNegativeButton("OK", null);
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    int res_code = response.code();
                                    String res = response.body().string();
                                    if (res_code == 200) {
                                        //成功
                                        if (!res.equals("ok")) {
                                            new AlertDialog.Builder(SuspiciousPersonInfoActivity.this)
                                                    .setMessage("通信に失敗しました")
                                                    .setCancelable(false)
                                                    .setNegativeButton("OK", null);
                                        }
                                    } else {
                                        //通信エラー
                                        new AlertDialog.Builder(SuspiciousPersonInfoActivity.this)
                                                .setMessage("通信に失敗しました")
                                                .setCancelable(false)
                                                .setNegativeButton("OK", null);
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //取得失敗
            new AlertDialog.Builder(this)
                    .setMessage("データの取得に失敗しました")
                    .setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(SuspiciousPersonInfoActivity.this, TopActivity.class);
                            startActivity(i);
                        }
                    });
        }
    }

    public void onClick(View v) {
        //ダイアログ表示
        AlertDialog.Builder ad = new AlertDialog.Builder(this)
                .setNegativeButton("キャンセル", null);
        //ボタンによって読み込むlayout変える
        LayoutInflater inflater = LayoutInflater.from(this);
        switch (v.getId()) {
            case R.id.yesButton:
                //yesボタンが押された時の処理 (不審者だった時)
                View dialog_view_yes = inflater.inflate(R.layout.spi_call_layout, null);
                ImageButton call_police_button = dialog_view_yes.findViewById(R.id.call_police_button);
                call_police_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //でんわをかける
                        Uri uri = Uri.parse("tel:110");
                        Intent call_intent = new Intent(Intent.ACTION_DIAL,uri);
                        startActivity(call_intent);
                    }
                });
                ad.setView(dialog_view_yes);
                ad.show();
                break;
            case R.id.noButton:
                //noボタン(知っている人だった時)
                Intent intent = new Intent(SuspiciousPersonInfoActivity.this,TopActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,TopActivity.class);
        startActivity(intent);
    }
}
