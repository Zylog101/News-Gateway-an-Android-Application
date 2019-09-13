package com.christopherhield.newsreader;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.ArraySet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.M)
public class NewsSourceInfoDownloader extends AsyncTask<Void, Void, Void> {

    private String category="";
    //https://newsapi.org/v1/sources?language=en&country=us&apiKey=
    private String newsSourceDownloaderUrlWithoutCatagory="https://newsapi.org/v1/sources?language=en&country=us";
    //https://newsapi.org/v1/sources?language=en&country=us&category=&apiKey=
    private String newsSourceDownloaderUrlWithCatagory = "https://newsapi.org/v1/sources?language=en&country=us";
    private String myAPIKey = "8f2494bb320c440e978a32c82f4369e8";
    private MainActivity myMainActivity;
    List<SourceInfo> newsSourceList = new ArrayList<>();
    Set<String>categoryList=new ArraySet<>();



    public NewsSourceInfoDownloader(MainActivity mainActivity,String category) {
        this.myMainActivity = mainActivity;
        this.category=category;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Uri.Builder buildURL;
    if(category.isEmpty()==true)
    {
        buildURL=Uri.parse(newsSourceDownloaderUrlWithCatagory).buildUpon();

    }
    else
    {
        buildURL=Uri.parse(newsSourceDownloaderUrlWithoutCatagory).buildUpon();
        buildURL.appendQueryParameter("category",category);
    }
        buildURL.appendQueryParameter("apiKey",myAPIKey);
        String urlString=buildURL.build().toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url= new URL(urlString);

            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream=connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(inputStream)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         parseJSON(sb.toString());
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        myMainActivity.OnNewsSouceInfoDownloadComplete(newsSourceList,categoryList);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void parseJSON(String s) {

        try {
            JSONObject jsonObject=new JSONObject(s);
            JSONArray jsonArray=jsonObject.getJSONArray("sources");

            for(int i=0;i<jsonArray.length();i++) {

                SourceInfo sourceInfo = new SourceInfo();
                sourceInfo.Id = jsonArray.getJSONObject(i).getString("id");
                sourceInfo.Name= jsonArray.getJSONObject(i).getString("name");
                sourceInfo.Url= jsonArray.getJSONObject(i).getString("url");
                sourceInfo.Category= jsonArray.getJSONObject(i).getString("category");
                categoryList.add(sourceInfo.Category);
                newsSourceList.add(sourceInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
