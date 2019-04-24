package root.fishfeeder;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    EditText feedTime;
    Button dailyFeedTime, feedNow;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this);
        feedTime = (EditText) findViewById(R.id.feed_time);
        dailyFeedTime = (Button) findViewById(R.id.daily_feed_time);
        feedNow = (Button) findViewById(R.id.feed_now);

        dailyFeedTime.setEnabled(false);
        feedNow.setEnabled(false);
        utils.ButtonStatus(feedTime, dailyFeedTime, feedNow);
    }

    public void dailyFeedTime(View v) {
        utils.CalenderDialog(feedTime);
    }

    public void log(View v) {
        startActivity(new Intent(this, LogActivity.class));
    }

    public void feedNow(View v) {
        String time = feedTime.getText().toString();
        utils.Send("feed=" + time);
    }

    public void info(View v) {
        utils.Dialog("About", "Created by Robotic's students Julian Osborne, Michael Ortiz, and Matt Adiletto with help from our dope teachers Mr. Christy and Mr. Alveraz.");
    }
}