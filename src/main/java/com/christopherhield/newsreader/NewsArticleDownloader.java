package com.christopherhield.newsreader;

import android.net.Uri;
import android.os.AsyncTask;

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

public class NewsArticleDownloader extends AsyncTask<Void, Void, Void> {

    private String source="";

    //https://newsapi.org/v1/articles?source=_______&apiKey=_______
    private String newsArticleDownloaderUrl="https://newsapi.org/v1/articles";

    private String myAPIKey = "8f2494bb320c440e978a32c82f4369e8";
    private Service callerService;
    ArrayList<ArticleInfo> newsArticleList = new ArrayList<>();

    public NewsArticleDownloader(String source, Service callerService) {
        this.source = source;
        this.callerService = callerService;
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        Uri.Builder buildURL;
        buildURL= Uri.parse(newsArticleDownloaderUrl).buildUpon();
        buildURL.appendQueryParameter("source",source);

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
        while ((line = reader.readLine()) != null)
        {
            sb.append(line).append('\n');
        }

    } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
      catch (IOException e)
        {
            e.printStackTrace();
        }
        parseJSON(sb.toString());

        return null;

    }

    private void parseJSON(String s)
    {
        try
        {
            JSONObject jsonObject=new JSONObject(s);
            JSONArray jsonArray=jsonObject.getJSONArray("articles");
            for (int i=0;i<jsonArray.length();i++)
            {
                ArticleInfo articleInfo=new ArticleInfo();

                articleInfo.Author=jsonArray.getJSONObject(i).getString("author");
                articleInfo.Title=jsonArray.getJSONObject(i).getString("title");
                articleInfo.Description=jsonArray.getJSONObject(i).getString("description");
                articleInfo.url=jsonArray.getJSONObject(i).getString("url");
                articleInfo.UrlToImage=jsonArray.getJSONObject(i).getString("urlToImage");
                articleInfo.PublishedAt=jsonArray.getJSONObject(i).getString("publishedAt");

                newsArticleList.add(articleInfo);

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callerService.onArticlesDownloadComplete(newsArticleList);

    }
}
