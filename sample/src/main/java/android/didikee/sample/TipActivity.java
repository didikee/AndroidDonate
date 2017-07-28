package android.didikee.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

public class TipActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String title = intent.getStringExtra("title");
        String tip = intent.getStringExtra("tip");
        int bgRes = intent.getIntExtra("res", R.drawable.p1);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(title);
        }

        coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator));
        imageView = ((ImageView) findViewById(R.id.image));


        imageView.setImageResource(bgRes);
        showSnackBar(tip);
    }

    private void showSnackBar(String msg) {
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
