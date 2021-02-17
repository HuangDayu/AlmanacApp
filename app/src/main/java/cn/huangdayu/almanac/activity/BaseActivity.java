package cn.huangdayu.almanac.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.necer.enumeration.CheckModel;

public abstract class BaseActivity extends AppCompatActivity {

    protected final static String TAG = "NECER";
    protected String title;
    protected CheckModel checkModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkModel = (CheckModel) getIntent().getSerializableExtra("selectedModel");
        title = getIntent().getStringExtra("title");

        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null && title != null) {
            supportActionBar.setTitle(title);
        }
    }

    protected Intent getNewIntent(Class<? extends BaseActivity> clazz, CheckModel checkModel, String title) {
        Intent intent = new Intent(this, clazz);
        intent.putExtra("selectedModel", checkModel);
        intent.putExtra("title", title);
        return intent;
    }


}
