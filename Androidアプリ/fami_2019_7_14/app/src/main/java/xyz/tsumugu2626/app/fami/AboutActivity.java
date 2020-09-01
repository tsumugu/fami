package xyz.tsumugu2626.app.fami;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    private TextView ver_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ver_tv = findViewById(R.id.verTextView);
        PackageManager pm = getPackageManager();
        String versionName = "ver: ----";
        try{
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ver_tv.setText("ver: "+versionName);
    }
}
