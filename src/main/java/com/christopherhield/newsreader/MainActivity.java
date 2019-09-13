package com.christopherhield.newsreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    public static final String SERVICE_DATA = "serviceData";
    private SampleReceiver sampleReceiver;
    static final String ARTICLE_BROADCAST_TYPE = "ARTICLE_BROADCAST_TYPE";
    static final String ACTION_MSG_TO_SERVICE="ACTION_MSG_TO_SERVICE";
    private List<SourceInfo> sourceInfos=new ArrayList<>();
    private List<String> categories=new ArrayList<>();
    private MyPageAdapter pageAdapter;
    private List<Fragment> fragments;
    View pagerView;
    private ViewPager pager;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<String> items = new ArrayList<>();
    ArrayAdapter myAdapter;
    private String selectedCatagory="";
    private String selectedSource="";
    private int pageNumber=-1;


    boolean IsConnectionAvailable()
    {
        ConnectivityManager connectivityManager =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetwork=connectivityManager.getActiveNetworkInfo();
        return currentNetwork!=null&&currentNetwork.isConnectedOrConnecting();
    }
    void OnNewsSouceInfoDownloadComplete(List<SourceInfo> newsSourceList,Set<String> categories)
    {

        items.clear();
        sourceInfos.clear();

        for(SourceInfo newsSource:newsSourceList)
        {
            sourceInfos.add(newsSource);
            items.add(newsSource.Name);
        }
        myAdapter.notifyDataSetChanged();

        if(this.categories.isEmpty())
        {
            this.categories.add("All");

            this.categories.addAll(categories);
            invalidateOptionsMenu();
        }
        if(!selectedCatagory.isEmpty())
        {
            items.clear();
            sourceInfos.clear();
            for(SourceInfo newsSource:newsSourceList)
            {
                if(newsSource.Category.equals(selectedCatagory)) {
                    sourceInfos.add(newsSource);
                    items.add(newsSource.Name);
                }
            }
            myAdapter.notifyDataSetChanged();


        }
        if(!selectedSource.isEmpty())
        {
            for(int i=0;i<items.size();i++)
            {
                if(items.get(i).equals(selectedSource))
                {
                    selectItem(i);
                    break;
                }
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("selectedSource", selectedSource);
        outState.putString("selectedCategory", selectedCatagory);
        pageNumber=pager.getCurrentItem();
        outState.putInt("pageNumber",pageNumber);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedSource=savedInstanceState.getString("selectedSource");
        selectedCatagory=savedInstanceState.getString("selectedCategory");
        pageNumber=savedInstanceState.getInt("pageNumber");


    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view);
        if(!IsConnectionAvailable())
        {
            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT);
            return;
        }
        pagerView=findViewById(R.id.viewpager);

        pagerView.setBackground(getDrawable(R.drawable.news));
        //start service
        Intent intent = new Intent(MainActivity.this, Service.class);
        startService(intent);

        //recieve broadcast info
        sampleReceiver = new SampleReceiver();

        IntentFilter filter1 = new IntentFilter(ARTICLE_BROADCAST_TYPE);
        registerReceiver(sampleReceiver, filter1);

       NewsSourceInfoDownloader informationDownloader=new NewsSourceInfoDownloader(MainActivity.this,"");
       informationDownloader.execute();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        myAdapter=new ArrayAdapter<>(this,R.layout.drawer_list_item, items);
        mDrawerList.setAdapter(myAdapter);
        mDrawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectItem(position);
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //

        fragments = getFragments();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);


    }
    private void sendMessage(String source) {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_MSG_TO_SERVICE);
        intent.putExtra(MainActivity.SERVICE_DATA, source);
        sendBroadcast(intent);
    }
    private void selectItem(int position) {
        if(!IsConnectionAvailable())
        {
            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT);
            return ;
        }
        String selectedSourceName=items.get(position);
        for (SourceInfo temp:sourceInfos)
        {
            if(temp.Name.equals(selectedSourceName))
            {
                selectedSource=temp.Name;

                sendMessage(temp.Id);
                setTitle(temp.Name);
                /*reDoFragments(position);
                mDrawerLayout.closeDrawer(mDrawerList);*/
                break;
            }
        }

    }

    private void reDoFragments(ArrayList<ArticleInfo> articles) {

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        fragments.clear();

        int count = articles.size();

        for (int i = 0; i < count; i++) {
            fragments.add(MyFragment.newInstance(articles.get(i),i+1,count));
            pageAdapter.notifyChangeInPosition(i);
        }

        pageAdapter.notifyDataSetChanged();
        if(pageNumber!=-1)
        {
            pager.setCurrentItem(pageNumber);
            pageNumber=-1;

        }
        else
        pager.setCurrentItem(0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        for (String catogary:categories)
        {
            menu.add(catogary);
        }

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatsically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(!IsConnectionAvailable())
        {
            Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_SHORT);
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        selectedCatagory="";
        if(!item.toString().equals("All"))
        {
            selectedCatagory=item.toString();
        }
        NewsSourceInfoDownloader informationDownloader=new NewsSourceInfoDownloader(MainActivity.this,selectedCatagory);
        informationDownloader.execute();

        return true;

    }

    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<>();
        return fList;
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         *
         * @param n number of items which have been changed
         */
        public void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }
    private class SampleReceiver extends BroadcastReceiver {
        static final String ARTICLE_BROADCAST_TYPE = "ARTICLE_BROADCAST_TYPE";
        static final String ACTION_MSG_TO_SERVICE="ACTION_MSG_TO_SERVICE";
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<ArticleInfo>articleInfo;
            switch (intent.getAction()) {
                case ARTICLE_BROADCAST_TYPE:
                    articleInfo=intent.getParcelableArrayListExtra(SERVICE_DATA);

                    reDoFragments(articleInfo);
                    mDrawerLayout.closeDrawer(mDrawerList);
                    pagerView.setBackground(null);
                    break;

            }
        }

    }

}
