package com.christopherhield.newsreader;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFragment extends Fragment {
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    private ArticleInfo articleInfo;
    public static final MyFragment newInstance(ArticleInfo articleInfo,int postion,int totalCount)
    {
        MyFragment f = new MyFragment();
        Bundle bdl = new Bundle(1);
        bdl.putParcelable(EXTRA_MESSAGE, articleInfo);
        bdl.putInt("PageNumber",postion);
        bdl.putInt("PageTotal",totalCount);
        f.setArguments(bdl);
        return f;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        articleInfo = getArguments().getParcelable(EXTRA_MESSAGE);
        View fragmentView = inflater.inflate(R.layout.myfragment_layout_dup, container, false);
        TextView titleTextView = (TextView)fragmentView.findViewById(R.id.titleView);
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebViewForTheArticle();
            }
        });
        titleTextView.setText(articleInfo.Title);
        TextView authorTextView = (TextView)fragmentView.findViewById(R.id.authorTextView);

        authorTextView.setText(articleInfo.Author.equals("null")?"" : articleInfo.Author);
        authorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebViewForTheArticle();
            }
        });
        TextView dateTextView = (TextView)fragmentView.findViewById(R.id.dateTextView);
        String date;

        SimpleDateFormat sdf;
        Date d = null;
        try {
            if(articleInfo.PublishedAt.contains("."))
            {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            }
            else if(articleInfo.PublishedAt.contains("+"))
            {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'");

            }
            else
            {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            }
                d = sdf.parse(articleInfo.PublishedAt);

            sdf=new SimpleDateFormat("MMM dd, yyyy\nHH:mm");
            date=sdf.format(d);
        } catch (ParseException e) {
            date=articleInfo.PublishedAt;
        }

       // sdf.applyPattern("MMM dd, yyyy\nHH:mm");


        dateTextView.setText( date.equals("null")?"":date);
        //dateTextView.setText(articleInfo.PublishedAt);



        TextView descriptonTextView = (TextView)fragmentView.findViewById(R.id.descriptonTextView);
        descriptonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebViewForTheArticle();
            }
        });
        descriptonTextView.setText(articleInfo.Description);
        TextView pageNumberTextView = (TextView)fragmentView.findViewById(R.id.pageNumberTextView);
        int position=getArguments().getInt("PageNumber");
        int totalCount=getArguments().getInt("PageTotal");
        pageNumberTextView.setText(position+" of "+totalCount);
        ImageView imageView = (ImageView)fragmentView.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebViewForTheArticle();
            }
        });
        uploadPhoto(imageView,articleInfo.UrlToImage);
        return fragmentView;
    }

    private void openWebViewForTheArticle()
    {
        String url=articleInfo.url;


        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

    }

    public void onClick(View v) {

    }
    private void uploadPhoto(final ImageView viewById, final String photoUrl) {

        Picasso picasso = new Picasso.Builder(this.getContext())
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        // Here we try https if the http image attempt failed

                        final String changedUrl = photoUrl.replace("http:", "https:");
                        picasso.load(changedUrl)


                                .into(viewById);
                    }
                })
                .build();

        picasso.load(photoUrl)


                .into(viewById);
    }
}