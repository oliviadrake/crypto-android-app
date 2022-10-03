package com.example.cryptocoin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main Activity
 */
public class CryptoListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    // set constant for API url
    private static final String API_BASE_URL = "https://api.coinlore.net/api/tickers/";

    // set ID for loader
    private static final int API_SEARCH_LOADER_ID = 17;

    final String PREFS = "prefs";
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor myEditor;
    View frame_layout;
    SearchView searchBar;
    Crypto[] dataList = {};

    /**
     * When activity is created, create all aspects of the page and apply any saved preferences
     * @param savedInstanceState bundle provided of data activity needs to create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cryptolist_activity);

        // initialise shared preferences and the editor
        mySharedPreferences = getSharedPreferences(PREFS, 0);
        myEditor = mySharedPreferences.edit();

        // initialise loader with ID
        getSupportLoaderManager().initLoader(API_SEARCH_LOADER_ID, null, this);

        // create and populate recycler view with internet data
        makeAPISearchQuery();

        // get elements to change color if necessary
        frame_layout = (View)findViewById(R.id.frame_layout);
        searchBar = (SearchView) findViewById(R.id.searchbar);

        // check preferences file been already created
        if (mySharedPreferences != null && mySharedPreferences.contains("backColor")) {
            // if file exists and contains preferences, apply these
            applySavedPreferences();
        } else {
            Toast.makeText(getApplicationContext(), "No Theme Preferences Found", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create a bundle containing the URL of the API, create an AsyncTaskLoader with that bundle,
     * then restart the loader if it hasn't already started
     */
    private void makeAPISearchQuery(){
        // create a bundle for the query, including the API URL
        Bundle queryBundle = new Bundle();

        queryBundle.putString("API_URL", API_BASE_URL);

        LoaderManager loaderManager = getSupportLoaderManager();

        // get loader from the ID
        Loader<String> apiSearchLoader = loaderManager.getLoader(API_SEARCH_LOADER_ID);

        // if the loader was null, initialize it, otherwise restart it.
        if (apiSearchLoader == null) {
            loaderManager.initLoader(API_SEARCH_LOADER_ID, queryBundle, this);
        } else {
            loaderManager.restartLoader(API_SEARCH_LOADER_ID, queryBundle, this);
        }
    }

    /**
     * When a loader is created for asynchronously accessing data from the internet, this method
     * starts a load of the url it is provided
     * @param id loader id
     * @param args bundle of url and any extra information
     * @return created loader
     */
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        //  return a new AsyncTaskLoader<String> as an anonymous inner class with 'this' as the constructor's parameter
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                // if no arguments were passed, no query to perform
                if (args == null) {
                    return;
                }
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                // get the string for the URL from the bundle passed to onCreateLoader
                String queryUrlString = args.getString("API_URL");

                // if the URL is null or empty, return null
                if (queryUrlString == null || TextUtils.isEmpty(queryUrlString)) {
                    return null;
                }

                try {
                    // get the data from the internet using the URL
                    URL apiUrl = new URL(queryUrlString);
                    return getResponseFromHttpUrl(apiUrl);
                } catch (IOException e) {
                    // if a network error occurs, send user to the error page
                    showErrorMessage();
                    return null;
                }
            }
        };
    }

    /**
     * When the asynchronous task loader finishes, this is called
     * @param loader the loader in question
     * @param data the results provided from the async call
     */
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // if no data returned, show error page
        if (null == data) {
            showErrorMessage();
        } else {
            // otherwise start and populate the recycler view
            dataList = parseJSON(data);
            createRecyclerView(parseJSON(data));
        }
    }

    /**
     * When the loader is reset, do nothing
     * @param loader loader in question
     */
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {}

    /**
     * Open an HTTP connection to the url provided and attempt to get a string response
     * @param url url to retrieve data from
     * @return the string of data provided from the url
     * @throws IOException url could provide nothing, or not be valid
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        // open http connection
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        // open stream from the https response
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                // return http response text
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * In case of a network error, provide user with the error activity
     */
    private void showErrorMessage() {
        // explicit intent to the error page
        Context context = CryptoListActivity.this;

        Class<ErrorPage> destinationActivity = ErrorPage.class;

        Intent errorIntent = new Intent(context, destinationActivity);

        startActivity(errorIntent);
    }

    /**
     * Transform JSON data provided by the API into a list of crypto items
     * @param data string data containing JSON
     * @return list of crypto objects
     */
    private Crypto[] parseJSON(String data){
        // parse the returned JSON data, so the list given to the recycler view has the correct data

        ArrayList<Crypto> cryptos = new ArrayList<>();
        Crypto[] list = {};

        try {
            JSONObject json = new JSONObject(new JSONTokener(data));
            JSONArray array = json.getJSONArray("data");

            for(int i = 0; i<array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                String coinSymbol = obj.getString("symbol");
                String coinName = obj.getString("name");
                String coinPrice = obj.getString("price_usd");
                String dailyPercentageChange = obj.getString("percent_change_24h");
                String hourlyPercentageChange = obj.getString("percent_change_1h");

                Crypto crypto = new Crypto(coinSymbol, coinName, coinPrice, dailyPercentageChange, hourlyPercentageChange);
                cryptos.add(crypto);
            }

            list = cryptos.toArray(new Crypto[0]);

        }catch(Exception e){
            showErrorMessage();
        }

        return list;
    }

    /**
     * Create the adapter for the recycler view and set the instructions for when a user
     * types into the search bar
     * @param cryptoList the list of crypto objects that were parsed from the HTTP resonse
     */
    private void createRecyclerView(Crypto[] cryptoList){
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        CryptoListAdapter mAdapter = new CryptoListAdapter(this, cryptoList);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // set what to do when user makes a search
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    /**
     * Create the options menu
     * @param menu menu layout
     * @return boolean if successful or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Instructions for when each item of the options menu is clicked
     * @param item item of menu that was clicked
     * @return boolean of success or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if user chooses light mode, change their preferences to light and same for dark
        switch (item.getItemId()) {
            case R.id.light_mode:
                myEditor.putInt("backColor", Color.WHITE); // white background
                myEditor.commit();
                applySavedPreferences();
                return true;
            case R.id.dark_mode:
                myEditor.putInt("backColor", Color.DKGRAY); // black background
                myEditor.commit();
                applySavedPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Apply the users choice of colour theme
     */
    public void applySavedPreferences() {
        int backColor = mySharedPreferences.getInt("backColor",Color.WHITE);
        frame_layout.setBackgroundColor(backColor);
    }

}