package com.example.diaaebakri.currencyconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Arrays;

public class
CurrencyList extends AppCompatActivity {
    /*
    * Declarations
    * */
    String Capital;
    ExchangeRateUpdateRunnable RunnableO;
    Thread T1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        RunnableO = new ExchangeRateUpdateRunnable(this, getApplicationContext());
        T1 = new Thread(RunnableO);



    /*
    * Initialize an ArrayAdapter and populate the Currencies ListView
    * */
      //  ArrayAdapter <String> currenciesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ExchangeRateDatabase.getCurrencies());
        ListView CurrenciesList = (ListView) findViewById(R.id.currenciesListView);
     /*
     * Assigning the rates and flags and names from the new customized adapter
     * */
     final ExRatesAdapter adapter = new ExRatesAdapter(CurrencyConverter.currencyList,true);
        CurrenciesList.setAdapter(adapter);
        CurrenciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView <?> parent, View view, int position, long id){
                    Capital = CurrencyConverter.CurrencyDB.getCapital(
                            adapter.getItem(position).toString());
                Uri gmmCurrencyUri = Uri.parse("geo:0,0?q=" + Capital);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,gmmCurrencyUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

    }

    public void updateCurrencies(){

        try {
            if (!T1.isAlive()) {
                T1 = new Thread(RunnableO);
                T1.start();
            }else{
                Toast.makeText(this,"Currencies are currently updating", Toast.LENGTH_SHORT).show();
            }

        }finally {

            CurrencyConverter.currencyList = Arrays.asList(CurrencyConverter.CurrencyDB.getCurrencies());
        }
    }


    /*
     * Menu function
     * */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(Build.VERSION.SDK_INT > 11) {
            invalidateOptionsMenu();
            menu.findItem(R.id.textView).setVisible(true);
            menu.findItem(R.id.currenciesListView).setVisible(false);
            menu.findItem(R.id.share_menu).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.overflow_menu: //case 1
                return true;
            case R.id.textView: //case 2
                Intent MainIntent = new Intent(CurrencyList.this, CurrencyConverter.class);
                startActivity(MainIntent);
                return false;
            case R.id.RefreshRates: //case refresh rates
                updateCurrencies();
                return true;

            default:return super.onOptionsItemSelected(item);
        }
    }
}
