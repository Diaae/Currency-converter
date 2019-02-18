package com.example.diaaebakri.currencyconverter;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.diaaebakri.currencyconverter.CurrencyConverter;
import com.example.diaaebakri.currencyconverter.ExchangeRateDatabase;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ExchangeRateUpdateRunnable implements Runnable  {

    static final String queryString =
            "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    private Context context;
    private Activity activity;
    private static final int NOTIFICATION_ID = 123;

    public ExchangeRateUpdateRunnable(Activity activity, Context context){
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void run(){
        synchronized (this){
            updateCurrencies(CurrencyConverter.CurrencyDB);
        }
    }

    public void updateCurrencies(ExchangeRateDatabase database) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        InputStream inputStream = null;
        try{
            if(isConnected) {
                URL u = new URL(queryString);
                URLConnection connection = u.openConnection();

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
                Log.i("updateCurrencies", "Rates updated :)");
                sendToast("Rates updated");
            }else{
                Log.i("updateCurrencies", "No internet connection detected");
            }
        } catch (Exception e) {
            Log.i("updateCurrencies", "Error: Cant update");
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                try{
                    inputStream.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            //currencyList = Arrays.asList(database.getCurrencies());
        }
    }

    private synchronized void sendToast(final String string){
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context, string, Toast.LENGTH_SHORT).show();

            }
            });
    };
}
