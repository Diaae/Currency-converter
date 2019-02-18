package com.example.diaaebakri.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ExRatesAdapter extends BaseAdapter {

    /*
    * Variables declaration
    * */
    private List<String> ratesList;
    private boolean displayRates;
    ImageView countryFlagView;
    TextView currencyNameView;
    TextView currencyRateView;
    static int imageID;
    static String flagStr;
    static String currencyRate;

    // ----------------CONSTRUCTORS--------------------------------------- //

    public ExRatesAdapter(List<String> ratesList, boolean displayRates){
        this.ratesList = ratesList;
        this.displayRates = displayRates;

    }

    public ExRatesAdapter(List<String> ratesList){ this(ratesList,false);
    }

    // ----------------CONSTRUCTORS--------------------------------------- //

    public int getCount(){
        return ratesList.size();
    }
    public Object getItem(int position){
        return ratesList.get(position);
    }
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        String currencyName = ratesList.get(position);

        if (convertView == null){
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.currency_list_layout,
                    null,false);
        }

        currencyNameView = convertView.findViewById(R.id.currency);
        countryFlagView = convertView.findViewById(R.id.countryFlag);
        flagStr = "flag_"+currencyName.toLowerCase();
        imageID = context.getResources().getIdentifier(flagStr,"drawable",
                context.getPackageName());

        countryFlagView.setImageResource(imageID);
        currencyNameView.setText(currencyName);

        if(displayRates){
            currencyRateView = convertView.findViewById(R.id.rate);
            currencyRate = CurrencyConverter.df.format(CurrencyConverter.CurrencyDB.getExchangeRate(currencyName));
            currencyRateView.setText(currencyRate);
        }

        return convertView;


    }
}