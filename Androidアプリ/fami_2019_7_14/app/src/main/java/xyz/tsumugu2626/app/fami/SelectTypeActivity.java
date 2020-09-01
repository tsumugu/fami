package xyz.tsumugu2626.app.fami;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class SelectTypeActivity extends AppCompatActivity {

    /*
     * 家に設置するか, 普通に使うか選択する画面
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_type);
    }

    public void use_at_home(View v) {
        //type(どのActivityに飛ぶか) を home(家に設置) に設定
        SP.saveString(this,"type","home");
        jump_to();
    }

    public void use_at_myself(View v) {
        //type(どのActivityに飛ぶか) を self(普通に使う) に設定
        SP.saveString(this,"type","self");
        jump_to();
    }

    public void jump_to() {
        String type_s = SP.loadString(this,"type");
        if (type_s.equals("home")) {
            Intent intent = new Intent(this,MainCam2Activity.class);
            startActivity(intent);
        } else if (type_s.equals("self")) {
            Intent intent = new Intent(this,SelectLoginUserActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this,SelectTypeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
