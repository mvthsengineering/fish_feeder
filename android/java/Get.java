package root.fishfeeder;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class Get extends AsyncTask<Void, Void, String> {
    private ProgressDialog p;
    private Utils u;

    Get(Context ctx) {
        this.p = new ProgressDialog(ctx);
        this.u = new Utils(ctx);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        p.setMessage("Loading...");
        p.setIndeterminate(false);
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.setCancelable(false);
        p.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        final StringBuilder response = new StringBuilder();
        try {
            URL obj = new URL("https://api.Particle.io/v1/devices/2e001f000e47353136383631/checkFeed?access_token=13603d6d6d0dd75418d22bde371fc0afa4305273");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "*/*");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        p.dismiss();
        u.Dialog(result);
    }
}