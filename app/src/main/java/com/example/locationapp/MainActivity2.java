package com.example.locationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity2 extends AppCompatActivity {
    private GpsTracker gpsTracker;
    Calendar calendar;
    Date currentLocalTime,dateObj;
    DateFormat date;
    EditText e1, e2;
    HttpURLConnection urlConnect;
    String inString,localTime,s,s1,s2,sunrise,sunset;
    TextView t1, t2, t3;
    URL url;


    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat",0);
        double longi = intent.getDoubleExtra("longi",0);

        progressBar = findViewById(R.id.progressBar);
        e1 = findViewById(R.id.enter_text);
        e2 = findViewById(R.id.enter_text2);
        t1 = findViewById(R.id.sunrise);
        t2 = findViewById(R.id.sunattop);
        t3 = findViewById(R.id.sunset);
        if (lat == 0 && longi == 0)
        {
            e1.setText("Please go back and refresh");
            e2.setText("Please go back and refresh");
        }
        else{
            e1.setText(String.valueOf(lat));
            e2.setText(String.valueOf(longi));
        }


        progressBar.setVisibility(View.VISIBLE);
        s = "https://api.sunrise-sunset.org/json?lat=" + e1.getText().toString() + "&lng=" + e2.getText().toString() + "&date=today";
        new MyAsyncTask().execute(s);
        try { if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            } } catch (Exception e){ e.printStackTrace(); }
        }

    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() { }
        @Override
        protected String doInBackground(@Nullable String... p0) {
            try {  url = new URL(p0[0]);
                urlConnect = (HttpURLConnection) url.openConnection();
                urlConnect.setConnectTimeout(7000);
                 inString = convertStreamToString(urlConnect.getInputStream());
                publishProgress(inString);
            } catch (Exception e) { }return " "; }

        @Override
        protected void onProgressUpdate(String... values) {
            try { JSONObject json= new JSONObject(values[0]);
                JSONObject query=json.getJSONObject("results");
                s1=query.getString("sunrise");
                s2=query.getString("sunset");
                 sunrise=gmt(s1.substring(0,8));
                 sunset=gmt(s2.substring(0,9));
               t1.setText("Sunrises at "+sunrise+"AM");
                t3.setText("Sunsets at "+sunset+"PM");
                t2.setText("Peak at "+ avgtime(sunrise,sunset));
            } catch (Exception e){} }

        @Override
        protected void onPostExecute(String string) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public String gmt(String s) throws ParseException {
         calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),Locale.getDefault());
         currentLocalTime = calendar.getTime();
         date = new SimpleDateFormat("ZZZZZ",Locale.getDefault());
         localTime = date.format(currentLocalTime);
        DateFormat format = new SimpleDateFormat("hh:mm:ss ZZZZZ");
        Date d1 = format.parse(s+" GMT+"+localTime);
        return  d1.toString().substring(11,20);
    }
    public String avgtime(String a,String b)throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
        Date date1 = format.parse(a);
        int x= Integer.parseInt(b.split(":")[0]);
        Date date2;
        if(x>12)
            date2 = format.parse(a);
        else
            date2 = format.parse(12+Integer.parseInt(b.substring(0,2))+b.substring(2));
        long difference = (date2.getTime() - date1.getTime())/2;
        date1.setTime(date1.getTime() + difference);
        try { final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
            dateObj = sdf.parse(date1.toString().substring(11,19));
        } catch (final ParseException e) { e.printStackTrace(); }
        String avgtime= new SimpleDateFormat("h:mm:ss").format(dateObj);


        int hours = Integer.parseInt(avgtime.split(":")[0]);

        String suffix = (hours >= 12) ? "PM": "AM";

        return String.format("%s %s", avgtime, suffix);
    }
    public String convertStreamToString(InputStream inputstream) {
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputstream));
        String line;
        String allString = "";
        try { do { line = bufferReader.readLine();
                if (line!=null) { allString += line; }
            } while(line != null);
            inputstream.close(); }
        catch(Exception e) { }
        return allString;
    }
}




