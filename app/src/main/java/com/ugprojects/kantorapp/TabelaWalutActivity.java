package com.ugprojects.kantorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class TabelaWalutActivity extends AppCompatActivity {

    ArrayList<String> kursy = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    boolean whichTable = true; //0 - A, 1 - C

    public class DownloadTaskTableC extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url= new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while(data!=-1){
                    char current = (char) data;
                    result+= current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error1";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error2";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                JSONObject jsonObject = new JSONObject(String.valueOf(jsonArray.get(0)));
                String ratesInfo = jsonObject.getString("rates");
                JSONArray array = new JSONArray(ratesInfo);
                for(int i=0;i<array.length();i++){
                    JSONObject jsonPart = array.getJSONObject(i);
                    kursy.add(jsonPart.getString("currency") + " - " + jsonPart.getString("code") + ": \n" + "Kupno: " +
                            jsonPart.getString("bid") + " Sprzedaż: " + jsonPart.getString("ask"));
                }
                for(int i=0;i<kursy.size();i++){
                    Log.i("TEST",kursy.get(i));
                }
                arrayAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class DownloadTaskTableA extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url= new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while(data!=-1){
                    char current = (char) data;
                    result+= current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error1";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error2";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                JSONObject jsonObject = new JSONObject(String.valueOf(jsonArray.get(0)));
                String ratesInfo = jsonObject.getString("rates");
                JSONArray array = new JSONArray(ratesInfo);
                for(int i=0;i<array.length();i++){
                    JSONObject jsonPart = array.getJSONObject(i);
                    kursy.add(jsonPart.getString("currency") + " - " + jsonPart.getString("mid") + " - " + jsonPart.getString("code"));
                }
                for(int i=0;i<kursy.size();i++){
                    Log.i("TEST",kursy.get(i));
                }
                arrayAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabela_walut);

        DownloadTaskTableC taskTableC = new DownloadTaskTableC();
        taskTableC.execute("http://api.nbp.pl/api/exchangerates/tables/c?format=json");

        ListView tabelaListView = findViewById(R.id.tabelaListView);


        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,kursy);
        tabelaListView.setAdapter(arrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.change_table,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.changeTable && whichTable){
            kursy.clear();
            DownloadTaskTableA taskTableA = new DownloadTaskTableA();
            taskTableA.execute("http://api.nbp.pl/api/exchangerates/tables/a?format=json");
            arrayAdapter.notifyDataSetChanged();
            item.setTitle("Pokaż tabelę kursów kupna i sprzedaży");
            whichTable = false;
            return true;
        }
        else if(item.getItemId()==R.id.changeTable && !whichTable){
            kursy.clear();
            DownloadTaskTableC taskTableC = new DownloadTaskTableC();
            taskTableC.execute("http://api.nbp.pl/api/exchangerates/tables/c?format=json");
            arrayAdapter.notifyDataSetChanged();
            item.setTitle("Pokaż tabelę kursów średnich");
            whichTable = true;
            return true;
        }
        return false;
    }
}
