package root.fishfeeder;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

class Utils {

    private Context ctx;

    Utils(Context ctx) {
        this.ctx = ctx;
    }

    void Send(String arg) {
        Request r = new Request(arg, ctx);
        r.execute();
    }
    void Log(RecyclerView rv, CardView ll) {
        GetLog r = new GetLog(ctx, rv, ll);
        r.execute();
    }

    void Dialog(String title, String message) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
        dialog.setInverseBackgroundForced(true);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("OK", null);
        dialog.show();
    }

    void ButtonStatus(final EditText et, final Button b1, final Button b2) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et.getText().toString().equals("")) {
                    b1.setEnabled(false);
                    b2.setEnabled(false);
                } else {
                    b1.setEnabled(true);
                    b2.setEnabled(true);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable arg0) {}
        });
    }

    void CalenderDialog(EditText feedTime) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        final String time = feedTime.getText().toString();

        TimePickerDialog timePickerDialog = new TimePickerDialog(ctx,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Send("daily=" + time + "," + hourOfDay + "," + minute);
                        Toast.makeText(ctx, time + " seconds @" + hourOfDay + ":" + minute, Toast.LENGTH_SHORT).show();;
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    public static String Endpoint(String device_id, String function, String access_token) {
        return "https://api.Particle.io/v1/devices/" + device_id + "/" + function + "?access_token=" + access_token;
    }
}