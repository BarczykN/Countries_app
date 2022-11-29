package com.example.countries_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    TextView content;
    EditText text;
    ImageView imageView;
    public static boolean connected;
    private final static String TAG =  "MainActivity";
    private final static String file =  "names.properties";
    private boolean countryExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = findViewById(R.id.contentTV);
        text = findViewById(R.id.etCountry);
        imageView = findViewById(R.id.flagView);
    }

    public void searchOnClick(View view) {
        imageView.setImageResource(R.drawable.ic_launcher_background);
        content.setText("");

        String countryName = text.getText().toString();

        if(countryName.toLowerCase().equals("usa") || countryName.toLowerCase().equals("stany zjednoczone")){
            countryName = "Stany Zjednoczone";
        }


        checkConnection();
        countryName = countryName.trim();
        checkCountry(countryName);


            if (countryExist) {
                getInfo(countryName);
                getInterestingThings(countryName);
                getImage(countryName);
            } else {
                content.setText("Nie ma takiego państwa :(");
            }

    }

    public void onNetworkChange(boolean isConnected) {
        showSnackBar(isConnected);
    }

    private void checkConnection() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionReceiver(), intentFilter);
        ConnectionReceiver.Listener = this;
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showSnackBar(isConnected);
    }

    private void showSnackBar(boolean isConnected) {
        String message;
        int color;

        if (!isConnected) {
            message = "Not Connected to Internet";
            color = Color.RED;
            Snackbar snackbar = Snackbar.make(findViewById(R.id.button), message, Snackbar.LENGTH_LONG);
            View view = snackbar.getView();
            TextView textView = view.findViewById(R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }


    }

    private void getInfo(String country_name){
        final String address =  "https://pl.wikipedia.org/wiki/"+country_name;



        Thread thread = new Thread(){
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(address);
                    Log.i(TAG,"Trying connect to: " + url);
                    Connection.Response r = Jsoup.connect(url.toString()).header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                            .header("Content-Type", "text/html; charset=ISO-8859-1").referrer("http://www.google.com")
                            .ignoreContentType(true).maxBodySize(0).timeout(600000).execute();
                    Log.i(TAG,"Successfully connected to " + url);
                    Log.i(TAG,"Parsing the content");
                    Document doc = r.parse();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Elements tmp = doc.select("table[class=infobox]");
                            tmp=tmp.first().select("td, th");
                            ArrayList<String> al = new ArrayList<String>();
                            for (int i=tmp.size()-1; i>0; i--) {
                                al.add(tmp.get(i).text());
                                al.add(tmp.get(--i).text());
                            }

                            Collections.reverse(al);

                            Properties nameProps = new Properties();
                            nameProps.setProperty("date", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));
                            nameProps.setProperty("names", ""+al.size());
                            int i = 1;

                            System.out.println("Found: "+al.size()+" names.");
                            boolean get = false;
                            for (String name : al) {
                                if(name.equals("Stolica")){
                                    System.out.println(name);
                                    String actualText = content.getText().toString();
                                    content.setText(actualText+name+" ");
                                    get = true;
                                }
                                else if(get){
                                    System.out.println(name);
                                    String actualText = content.getText().toString();
                                    content.setText(actualText+name+"\n");
                                    get = false;
                                }

                                nameProps.setProperty("name."+i++, name);
                            }
                        }
                    });

                }
                catch (FileNotFoundException e1) {
                    Log.e(TAG,"Problems with local file: "+file);
                }
                catch (MalformedURLException e) {
                    Log.e(TAG,"Problem with URL: " + url, e);
                }
                catch (UnknownHostException e) {
                    Log.e(TAG,"Cannot connect to: "+url);
                }
                catch (IOException e) {
                    Log.e(TAG,"Exception: "+e.getLocalizedMessage(),e);
                }
            }
        };
        thread.start();
        try{

            thread.join();
        }catch (InterruptedException e){
            Log.e(TAG,e.toString());
        }
        thread.interrupt();
    }


    private void getInterestingThings(String country_name){
        final String address =  "https://pl.wikipedia.org/wiki/"+country_name;
      /*  Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.TRACE);*/

        Thread thread = new Thread(){
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(address);
                    Log.i(TAG,"Trying connect to: " + url);
                    Connection.Response r = Jsoup.connect(url.toString()).header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                            .header("Content-Type", "text/html; charset=ISO-8859-1").referrer("http://www.google.com")
                            .ignoreContentType(true).maxBodySize(0).timeout(600000).execute();
                    Log.i(TAG,"Successfully connected to " + url);
                    Log.i(TAG,"Parsing the content");
                    Document doc = r.parse();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Elements paragraphs = doc.select("div[class=mw-parser-output] > p");
                            Element p = paragraphs.get(0);
                            content.setText(content.getText().toString()+p.text());
                        }
                    });
                }
                catch (FileNotFoundException e1) {
                    Log.e(TAG,"Problems with local file: "+file);

                }
                catch (MalformedURLException e) {
                    Log.e(TAG,"Problem with URL: " + url, e);

                }
                catch (UnknownHostException e) {
                    Log.e(TAG,"Cannot connect to: "+url);

                }
                catch (IOException e) {
                    Log.e(TAG,"Exception: "+e.getLocalizedMessage(),e);

                }
            }
        };
        thread.start();
        try{

            thread.join();
        }catch (InterruptedException e){
            Log.e(TAG,e.toString());
        }
        thread.interrupt();

    }


    private void checkCountry(String country_name){
        final String address =  "https://pl.wikipedia.org/wiki/Lista_państw_świata";



        Thread thread = new Thread(){
            @Override
            public void run() {
                URL url = null;

                try {
                    url = new URL(address);
                    Log.i(TAG,"Trying connect to: " + url);

                    Connection.Response r = Jsoup.connect(url.toString()).header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                            .header("Content-Type", "text/html; charset=ISO-8859-1").referrer("http://www.google.com")
                            .ignoreContentType(true).maxBodySize(0).timeout(60).execute();
                    Log.i(TAG,"Successfully connected to " + url);
                    Log.i(TAG,"Parsing the content");
                    Document doc = r.parse();

                    Elements table = doc.select("table[class=wikitable sortable]");
                    Elements rows = table.select("tr");
                    List<String> hrefs = new ArrayList<>(Collections.emptyList());
                    for(Element row : rows){
                        Elements title = row.select("a[href][title]");
                        String name;
                        if(title.size() > 0){
                            name = title.get(0).text();
                        }
                        else{
                            continue;
                        }

                        hrefs.add(name.toLowerCase());
                    }

                    countryExist = hrefs.contains(country_name.toLowerCase());

                }
                catch (FileNotFoundException e1) {
                    Log.e(TAG,"Problems with local file: "+file);

                }
                catch (MalformedURLException e) {
                    Log.e(TAG,"Problem with URL: " + url, e);

                }
                catch (UnknownHostException e) {
                    Log.e(TAG,"Cannot connect to: "+url);

                }
                catch (IOException e) {
                    Log.e(TAG,"Exception: "+e.getLocalizedMessage(),e);

                }
            }
        };
        thread.start();
        try{

            thread.join();
        }catch (InterruptedException e){
            Log.e(TAG,e.toString());
        }
    }

    private void getImage(String country_name){
        final String address =  "https://pl.wikipedia.org/wiki/"+country_name;
       /* Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.TRACE);*/

        Thread thread = new Thread(){
            @Override
            public void run() {
                URL url = null;

                try{
                    url = new URL(address);
                    Log.i(TAG,"Trying connect to: " + url);
                    Connection.Response r = Jsoup.connect(url.toString()).header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                            .header("Content-Type", "text/html; charset=ISO-8859-1").referrer("http://www.google.com")
                            .ignoreContentType(true).maxBodySize(0).timeout(600000).execute();
                    Log.i(TAG,"Successfully connected to " + url);
                    Log.i(TAG,"Parsing the content");
                    Document doc = r.parse();
                    Elements infobox = doc.select("table[class=infobox]");
                    //   System.out.println("table"+infobox.text());
                    Elements images = infobox.select("img");
                    System.out.println("img 0 "+images.get(0));
                    URL url1;
                    Bitmap bitmap;
                    try{
                        url1 = new URL("https:"+images.get(0).attr("src"));
                        InputStream is  = (InputStream) url1.openStream();
                        bitmap = BitmapFactory.decodeStream(is);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }catch (MalformedURLException e){
                        Log.e(TAG,e.toString());
                        return;
                    }catch (Exception e){
                        System.out.println(e);
                    }





                }
                catch (FileNotFoundException e1) {
                    Log.e(TAG,"Problems with local file: "+file);

                }
                catch (MalformedURLException e) {
                    Log.e(TAG,"Problem with URL: " + url, e);

                }
                catch (UnknownHostException e) {
                    Log.e(TAG,"Cannot connect to: "+url);

                }
                catch (IOException e) {
                    Log.e(TAG,"Exception: "+e.getLocalizedMessage(),e);

                }
            }
        };
        thread.start();
        try{
            thread.join();
        }catch (InterruptedException e){
            Log.e(TAG,e.toString());
        }
        thread.interrupt();
    }





}