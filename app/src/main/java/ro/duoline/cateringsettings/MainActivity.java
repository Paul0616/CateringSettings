package ro.duoline.cateringsettings;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{
    private static final int RESTAURANT_LOADER_ID = 35;
    private final static String RESTAURANTE_URL_BASE = "https://www.duoline.ro/catering";
    private final static String RESTAURANTE_FILE_PHP_QUERY = "getRestaurante.php";
    private final static String HEADER_RECYCLER_VIEW = "Catering\nConfigurare";
    private ImageView pozaFundal;
    private RecyclerView headerRecyclerView;
    private ProgressBar mLoadingIndicator;
    private HeaderRecyclerViewAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        Drawable drw = getResources().getDrawable(R.drawable.action_bar);
        getSupportActionBar().setBackgroundDrawable(drw);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        headerRecyclerView = (RecyclerView) findViewById(R.id.add_header);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        headerRecyclerView.setLayoutManager(linearLayoutManager);
        headerRecyclerView.setHasFixedSize(true);
        pozaFundal = (ImageView) findViewById(R.id.imageView2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadListaRestaurante();

    }



    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        }  finally {
            urlConnection.disconnect();
        }
    }

    public void loadListaRestaurante(){
        makeURLConnection(makeURL(RESTAURANTE_URL_BASE, RESTAURANTE_FILE_PHP_QUERY), RESTAURANT_LOADER_ID);
    }

    private void makeURLConnection(URL queryURL, int loaderID){
        Bundle queryBundle = new Bundle();
        queryBundle.putString("link",queryURL.toString());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> queryLoader = loaderManager.getLoader(loaderID);
        if(queryLoader == null){
            loaderManager.initLoader(loaderID, queryBundle, this);
        } else {
            loaderManager.restartLoader(loaderID, queryBundle, this);
        }
    }

    private URL makeURL(String base, String file){
        Uri bultUri = Uri.parse(base);
        bultUri = Uri.withAppendedPath(bultUri, file);
        URL queryURL = null;
        try {
            queryURL = new URL(bultUri.toString());
            return queryURL;
        } catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }
    }


    private List<HeaderRecyclerViewItems> getDataSource(JSONArray jArray){
        List<HeaderRecyclerViewItems> data = new ArrayList<HeaderRecyclerViewItems>();
        if (jArray == null || jArray.length() == 0){
            return data;
        } else {
            data.add(new HeaderRecyclerViewItems(null, HEADER_RECYCLER_VIEW));
            try{
                for(int i = 0; i < jArray.length(); i++){

                    data.add(new HeaderRecyclerViewItems(jArray.getJSONObject(i), HEADER_RECYCLER_VIEW));
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
            return data;
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args == null){
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String queryURLString = args.getString("link");
                if(queryURLString == null || queryURLString == "") return null;
                try{
                    URL queryURL = new URL(queryURLString);
                    String result = getResponseFromHttpUrl(queryURL);

                    return result;
                } catch (IOException e){

                    e.printStackTrace();
                    return null;
                }
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        try {
            JSONArray jArray = null;
            if(data != null) {
                jArray = new JSONArray(data);
            }

            if(jArray != null) {
                customAdapter = new HeaderRecyclerViewAdapter(getApplicationContext(), getDataSource(jArray));

                headerRecyclerView.setAdapter(customAdapter);

            } else Toast.makeText(getApplicationContext(), "Eroare de conectare la server!!!", Toast.LENGTH_LONG).show();



        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
