package com.example.cryptocoin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity user is taken to when they want more information about a certain cryptocurrency
 */
public class CryptoMoreInfo extends AppCompatActivity {

    final String PREFS = "prefs";
    SharedPreferences mySharedPreferences;
    SharedPreferences.Editor myEditor;
    View layout;
    EditText convertEntry;
    TextView converted;
    String coinName = "";
    String coinPrice = "";
    String coinSymbol = "";
    String dailyPChange = "";
    String hourlyPChange = "";

    /**
     * When activity is created, gather all data from the intent that sent it here and apply
     * any shared preferences
     * @param savedInstanceState bundle provided of data activity needs to create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crypto_more_info);

        TextView title = (TextView) findViewById(R.id.moreInfoTitle);
        TextView symbol = (TextView) findViewById(R.id.moreInfoSymbol);
        TextView price = (TextView) findViewById(R.id.moreInfoPrice);
        TextView DPercentage = (TextView) findViewById(R.id.moreInfoDPercentage);
        TextView HPercentage = (TextView) findViewById(R.id.moreInfoHPercentage);
        convertEntry = (EditText) findViewById(R.id.text_entry);
        converted = (TextView) findViewById(R.id.convert_result);
        layout = (View) findViewById(R.id.linear_layout);

        // get all information from the intent that started this activity
        Intent previousActivity = getIntent();

        if (previousActivity.hasExtra("name")) {
            coinName = previousActivity.getStringExtra("name");
            // set title text
            title.setText(coinName);
        }
        if (previousActivity.hasExtra("price")) {
            coinPrice = previousActivity.getStringExtra("price");
            String displayPrice = "$" + coinPrice;
            // set price text
            price.setText(displayPrice);
        }
        if (previousActivity.hasExtra("symbol")) {
            coinSymbol = previousActivity.getStringExtra("symbol");
            // set symbol text
            symbol.setText(coinSymbol);
        }
        if (previousActivity.hasExtra("dailypchange")) {
            dailyPChange = previousActivity.getStringExtra("dailypchange");
            String displayPChange;
            int textColour;

            if(dailyPChange.contains("-")){
                displayPChange = dailyPChange + "% (24h)";
                // make negative changes red
                textColour = Color.parseColor("#FF5656");
            }
            else{
                displayPChange = "+" + dailyPChange + "% (24h)";
                // make positive changes green
                textColour = Color.parseColor("#4CAF50");
            }

            // set daily percentage change text and colour
            DPercentage.setText(displayPChange);
            DPercentage.setTextColor(textColour);
        }
        if (previousActivity.hasExtra("hourlypchange")) {
            String displayHPChange;
            int textColour;
            hourlyPChange = previousActivity.getStringExtra("hourlypchange");

            if(hourlyPChange.contains("-")){
                displayHPChange = hourlyPChange + "% (1h)";
                // make negative changes red
                textColour = Color.parseColor("#FF5656");
            }
            else{
                displayHPChange = "+" + hourlyPChange + "% (1h)";
                // make positive changes green
                textColour = Color.parseColor("#4CAF50");
            }

            // set hourly percentage change text and colour
            HPercentage.setText(displayHPChange);
            HPercentage.setTextColor(textColour);
        }

        // get shared preferences and editor
        mySharedPreferences = getSharedPreferences(PREFS, 0);
        myEditor = mySharedPreferences.edit();

        // check preferences file been already created
        if (mySharedPreferences != null && mySharedPreferences.contains("backColor")) {
            // if file exists and contains preferences, apply these
            applySavedPreferences();
        }
    }

    /**
     * Apply the user's choice of colour themes
     */
    public void applySavedPreferences() {
        int backColor = mySharedPreferences.getInt("backColor", Color.WHITE);
        layout.setBackgroundColor(backColor);
    }

    /**
     * When the user clicks the share button, create the string to be shared
     * @param v current view
     */
    public void onClickShareTextButton(View v) {
        // when share button clicked, create string to share
        String sharingText = "";
        if(dailyPChange.contains("-")){
            sharingText = coinName + " is down " + dailyPChange.substring(1) + "% today. Is it time to invest?!";
        }else{
            sharingText = coinName + " is up " + dailyPChange + "% today. Is it time to sell?!";
        }
        shareText(sharingText);
    }

    /**
     * Create implicit intent to share the created string
     * @param textToShare string
     */
    private void shareText(String textToShare) {
        // create an implicit intent to share the text
        String mimeType = "text/plain";

        // create a title for the chooser window that will pop up
        String title = "Share " + coinName + "Daily Roundup";

        // build the intent and start the chooser
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setText(textToShare)
                .startChooser();
    }

    /**
     * On entry of text into the convert box, and clicking the convert button, display the value
     * of their currency in dollars
     * @param v current view
     */
    public void currencyConvert(View v){
        // get user's typed number and calculate coin worth
        String textEntered = convertEntry.getText().toString();
        double coinPriceInt = 0.0;
        double userCoins = 0.0;

        try {
            coinPriceInt = Double.parseDouble(coinPrice);
            userCoins = Double.parseDouble(textEntered);
        }catch(Exception e){}

        double result = coinPriceInt * userCoins;
        String strResult = "$" + result;

        converted.setText(strResult);
    }
}
