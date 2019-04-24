package root.fishfeeder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class GetLog extends AsyncTask<String, Void, String> {

    private ProgressDialog p;
    private Context ctx;
    private CardView ll;
    private RecyclerView recyclerView;

    GetLog(Context ctx, RecyclerView recyclerView, CardView ll) {
        this.p = new ProgressDialog(ctx);
        this.ctx = ctx;
        this.ll = ll;
        this.recyclerView = recyclerView;
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... params) {
        final StringBuilder response = new StringBuilder();
        try {
            URL obj = new URL("http://heyroot.com/photon/log.txt");
            try {
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                try {
                    con.setRequestMethod("GET");
                } catch (ProtocolException e) {
                    e.printStackTrace();
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
        p.dismiss();
        String[] objects = result.split("---");
        List<FeedLog> productList = new ArrayList<>();
        for (int i = objects.length; i >= 0; i--) {            try {
                JSONTokener token = new JSONTokener(objects[i]);
                JSONObject obj = new JSONObject(token);
                String publishedAt = obj.getString("published_at");
                String data = obj.getString("data");
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                Date date = parser.parse(publishedAt);
                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                String time = formatter.format(date);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
                productList.add(
                        new FeedLog(
                                1, data, time
                                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogAdapter adapter = new LogAdapter(ctx, productList, ll);
        recyclerView.setAdapter(adapter);
    }
}