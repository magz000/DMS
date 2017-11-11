package ph.edu.tip.app.dms.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import ph.edu.tip.app.dms.R;

/**
 * Created by Mark Jansen Calderon on 12/19/2016.
 */

public class BaseActivity extends AppCompatActivity{

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
