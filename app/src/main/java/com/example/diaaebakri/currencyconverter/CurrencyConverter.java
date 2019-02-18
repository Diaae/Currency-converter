    package com.example.diaaebakri.currencyconverter;

    import android.app.Activity;
    import android.app.job.JobInfo;
    import android.app.job.JobScheduler;
    import android.content.ComponentName;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.net.ConnectivityManager;
    import android.net.Network;
    import android.net.NetworkInfo;
    import android.os.Build;
    import android.os.StrictMode;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.inputmethod.InputMethodManager;
    import android.widget.ArrayAdapter;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.ShareActionProvider;
    import android.widget.Spinner;
    import android.widget.TextView;
    import android.widget.Toast;


    import org.xmlpull.v1.XmlPullParser;
    import org.xmlpull.v1.XmlPullParserFactory;

    import java.io.IOException;
    import java.io.InputStream;
    import java.net.URL;
    import java.net.URLConnection;
    import java.text.DecimalFormat;
    import java.text.DecimalFormatSymbols;
    import java.text.NumberFormat;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Locale;

    public class CurrencyConverter extends AppCompatActivity {

        //Constants
        static final ExchangeRateDatabase CurrencyDB = new ExchangeRateDatabase();
        static final DecimalFormat df = (DecimalFormat)NumberFormat.getCurrencyInstance(Locale.GERMAN);
        static List<String> currencyList = Arrays.asList(CurrencyDB.getCurrencies());
        static final String Link = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
        static int initialCurrencyPosition;
        static int targetCurrencyPosition;

        //Variables
        double inputUser;
        double conversionResult;
        EditText enterAmount;
        Spinner fromCurrency;
        Spinner toCurrency;
        TextView textResult;
        String conversionResultStr;
        String fromValue;
        String toValue;
        String inputCurrency;
        Spinner spinnerFrom;
        Spinner spinnerTo;
        ExchangeRateUpdateRunnable RunnableO;
        Thread T1;
        //Shared Prefences
        SharedPreferences sp;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_currency_converter);
            sp = getSharedPreferences("CurrencyConverter", MODE_PRIVATE);

            //Access Internet
/*
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
*/

            RunnableO = new ExchangeRateUpdateRunnable(this, getApplicationContext());
            T1 = new Thread(RunnableO);

            /*
             * Create and Populate spinner from currencies.xml file
             * */
            spinnerFrom = findViewById(R.id.spinner); //From Value in Spinner
            spinnerTo = findViewById(R.id.spinner2); //To value in Spinner


            /*
             * Assigning the rates and flags and names from the new customized adapter
             * */
            ExRatesAdapter adapter = new ExRatesAdapter(CurrencyConverter.currencyList, true);
            spinnerFrom.setAdapter(adapter);
            spinnerTo.setAdapter(adapter);

            //Set defaults currencies to spinners
            spinnerFrom.setSelection(currencyList.indexOf("EUR"));
            spinnerTo.setSelection(currencyList.indexOf("USD"));



            /*
             * Show hint when EditText "Please enter an amount is unfocused"
             */

            EditText EnterAmountFocus = findViewById(R.id.enterAmount);
            EnterAmountFocus.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(v);
                    }
                }
            });

            //Formatting
            DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
            symbols.setCurrencySymbol("");
            df.setDecimalFormatSymbols(symbols);

            //Call job scheduler
            jobScheduler();

        }
        // ------------- END OF ONCREATE --------------- //



        void jobScheduler(){
            ComponentName serviceUpdate  = new ComponentName(this, TimeService.class);
            JobInfo jobInfo = new JobInfo.Builder(420, serviceUpdate)
                    .setRequiresDeviceIdle(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setRequiresCharging(false)
                    .setPersisted(true)
                    .setPeriodic(86400)
                    .build();
            try{
                JobScheduler jScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                int result = jScheduler.schedule(jobInfo);

                if(result == JobScheduler.RESULT_SUCCESS) Log.i("Job Scheduler", "running");
            }catch(Error e){
                Log.i("Job Scheduler", "Error: Something happened...");
            }
        }

        /* Create a new thread if it doesn't exist and get updated currencies otherwise show a toast */
       public void updateCurrencies(){

            try {
                if (!T1.isAlive()) {
                    T1 = new Thread(RunnableO);
                    T1.start();
                }else{
                    Toast.makeText(this,"Currencies are currently updating", Toast.LENGTH_SHORT).show();
                }

            }finally {

                currencyList = Arrays.asList(CurrencyDB.getCurrencies());
            }
        }




        /*
         *
         * Side methods
         *
         * */
        public void hideKeyboard(View view) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


        /*
         * Method to handle the onClick event for Calculate button
         * */


        public void convert(View v) {
            enterAmount = findViewById(R.id.enterAmount);
            fromCurrency = findViewById(R.id.spinner);
            toCurrency = findViewById(R.id.spinner2);
            textResult = findViewById(R.id.textView5);

            fromValue = fromCurrency.getSelectedItem().toString();
            toValue = toCurrency.getSelectedItem().toString();
            inputCurrency = enterAmount.getText().toString();
            if (inputCurrency.isEmpty()) {
                enterAmount.setHint("Please I need an amount !");
            } else {
                inputUser = Double.parseDouble(inputCurrency);
                conversionResult = CurrencyDB.convert(inputUser, fromValue, toValue);
                conversionResultStr = df.format(conversionResult) + " " + toValue;
                textResult.setText(conversionResultStr);
                enterAmount.setHint("Please enter an amount");
            }
        }


        /*
         * Refresh Rates
         * */


/*        private void updateCurrencies(ExchangeRateDatabase database) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

            InputStream inputStream = null;
            try {
                if (isConnected) {
                    URL queryUrl = new URL(Link);
                    URLConnection connection = queryUrl.openConnection();

                    inputStream = connection.getInputStream();
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    String encoding = connection.getContentEncoding();
                    parser.setInput(inputStream, encoding);

                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG
                                && parser.getName().equals("Cube")) {
                            String currencyName = parser.getAttributeValue(null, "currency");
                            if (parser.getAttributeValue(null, "rate") != null) {
                                Double newCurrencyRate =
                                        Double.parseDouble(parser.getAttributeValue(null, "rate"));
                                database.setExchangeRate(currencyName, newCurrencyRate);
                            }
                        }
                        eventType = parser.next();
                    }
                    Log.i("updateCurrencies", "Rates updated");
                } else {
                    Log.i("updateCurrencies", "No internet connection detected");
                }
            } catch (Exception e) {
                Log.i("updateCurrencies", "Error: Can t update");
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                }
                currencyList = Arrays.asList(database.getCurrencies());
                Toast.makeText(this, "Cheers ! Currencies updated", Toast.LENGTH_SHORT).show();
            }
        }*/


        /*
         * Menu function
         * */
        @Override // Control what to display before loading the OptionsMenu
        public boolean onPrepareOptionsMenu(Menu menu) {
            if (Build.VERSION.SDK_INT > 11) {
                invalidateOptionsMenu();
                menu.findItem(R.id.currenciesListView).setVisible(true);
                menu.findItem(R.id.textView).setVisible(false);
            }
            return super.onPrepareOptionsMenu(menu);
        }

        @Override //Loading the OptionsMenu
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        @Override //Define interactions when clicking on a menu item
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.overflow_menu: //case Overflowmenu
                    return true;
                case R.id.currenciesListView: //case Currencies ListView
                    Intent currenciesIntent = new Intent(CurrencyConverter.this, CurrencyList.class);
                    startActivity(currenciesIntent);
                    return false;
                case R.id.share_menu: //case share menu
                    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareBodyText = "The amount " + inputUser + " " + fromValue + " To "
                            + toValue + " is: " + conversionResultStr;
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                    startActivity(Intent.createChooser(shareIntent, "Choose a sharing option"));
                    return true;
                case R.id.RefreshRates: //case refresh rates
                   updateCurrencies();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        public void swapCurrencies(View v){
            initialCurrencyPosition = spinnerFrom.getSelectedItemPosition();
            targetCurrencyPosition = spinnerTo.getSelectedItemPosition();

            initialCurrencyPosition += targetCurrencyPosition;
            targetCurrencyPosition = initialCurrencyPosition - targetCurrencyPosition;
            initialCurrencyPosition = initialCurrencyPosition - targetCurrencyPosition;

            spinnerFrom.setSelection(initialCurrencyPosition);
            spinnerTo.setSelection(targetCurrencyPosition);
        }

        /* Store and Restore */

        @Override
        protected void onPause() {
            super.onPause();
            sp.edit().putInt("inputSpinner", spinnerFrom.getSelectedItemPosition()).apply();
            sp.edit().putInt("targetSpinner", spinnerTo.getSelectedItemPosition()).apply();

            for(String x : CurrencyDB.getCurrencies()){
                sp.edit().putString(x, String.valueOf(CurrencyDB.getExchangeRate(x))).apply();
            }

            sp.edit().putBoolean("saved", true).apply();
        }

        @Override
        protected void onResume() {
            super.onResume();
            spinnerFrom.setSelection(sp.getInt("inputSpin", currencyList.indexOf("EUR")));
            spinnerTo.setSelection(sp.getInt("targetSpin", currencyList.indexOf("USD")));

            if(sp.getBoolean("saved", false)){
                for(String x : CurrencyDB.getCurrencies()){
                    CurrencyDB.setExchangeRate(x, Double.valueOf(sp.getString(x, "")));
                }
            }
        }

    }