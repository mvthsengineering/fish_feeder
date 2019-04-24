package root.fishfeeder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class Request extends AsyncTask<String, Void, String> {

    private String arg;
    private Context ctx;
    private ProgressDialog p;

    Request(String arg, Context ctx) {
        this.arg = arg;
        this.ctx = ctx;
        this.p = new ProgressDialog(ctx);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        p.setMessage("Sending...");
        p.setIndeterminate(false);
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.setCancelable(false);
        p.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... params) {
        final StringBuilder response = new StringBuilder();
        try {
            URL obj = new URL(Setup.PHOTON_URL);
            try {
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                try {
                    con.setRequestMethod("POST");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
                String urlParameters = "arg=" + arg;
                byte[] postData = new byte[0];
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                }
                int postDataLength = postData.length;
                con.setDoOutput(true);
                con.setInstanceFollowRedirects(false);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("charset", "utf-8");
                con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                con.setUseCaches(false);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.write(postData);
                }
                BufferedReader in;
                try {
                    in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    try {
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        JSONTokener token = new JSONTokener(result);
        JSONObject obj = null;
        try {
            obj = new JSONObject(token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (obj != null) {
            p.dismiss();
        } else {
            p.setMessage("WiFi is a luxury, which the photon doesn\'t have right now. Try again later :( It will feed at the last time you set, and if no time was set it will feed at 2:00 A.M.");
        }
        p.setCancelable(true);
    }
}