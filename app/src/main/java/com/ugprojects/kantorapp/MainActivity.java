package com.ugprojects.kantorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    static double totalBudget = 100000.00;
    TextView welcomeTextView;
    TextView welcomeTextView2;
    static TextView budgetTextView;
    Button showButton;
    static Spinner budgetSpinner;
    String[] codes = new String[36];
    static ArrayAdapter<String> adapter;
    Button firstButton;
    Button thirdButton;
    Button secondButton;
    LayoutInflater inflater;
    double nowaWartosc =0;
    static ArrayList<String> historia;
    SharedPreferences sharedPreferences;
    static Gson gson = new Gson();

    static HashMap<String,Double> budgetRates;

    public class DownloadTask extends AsyncTask<String,Void,String> {

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

                    budgetRates.put(jsonPart.getString("code"), 0.00);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    static public class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
            mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher=mPattern.matcher(dest);
            if(!matcher.matches())
                return "";
            return null;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
        String storedHashMapString = sharedPreferences.getString("budgetRates", null);
        HashSet<String> set = (HashSet<String>) sharedPreferences.getStringSet("historia",null);

        if(set==null){
            historia = new ArrayList<>();
        }
        else{
            historia = new ArrayList(set);
        }

        if(storedHashMapString==null){
            budgetRates  = new HashMap<String, Double>();

            DownloadTask task = new DownloadTask();
            task.execute("http://api.nbp.pl/api/exchangerates/tables/a?format=json");

            budgetRates.put("PLN",totalBudget);
        }
        else {
            java.lang.reflect.Type type = new TypeToken<HashMap<String, Double>>() {}.getType();
            budgetRates = gson.fromJson(storedHashMapString, type);
            totalBudget = budgetRates.get("PLN");
        }
        welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeTextView2 = findViewById(R.id.welcomeTextView2);
        budgetTextView = findViewById(R.id.budgetTextView);
        budgetTextView.setVisibility(View.INVISIBLE);
        showButton = findViewById(R.id.showButton);
        budgetSpinner = findViewById(R.id.budgetSpinner);
        budgetSpinner.setVisibility(View.INVISIBLE);
        firstButton = findViewById(R.id.firstButton);
        thirdButton = findViewById(R.id.thirdButton);
        secondButton = findViewById(R.id.secondButton);

        inflater = this.getLayoutInflater();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void showBudget(View view){
        budgetTextView.setText(String.valueOf(budgetRates.get("PLN")));
        showButton.setVisibility(View.INVISIBLE);
        budgetTextView.setVisibility(View.VISIBLE);
        firstButton.setVisibility(View.VISIBLE);
        thirdButton.setVisibility(View.VISIBLE);
        secondButton.setVisibility(View.VISIBLE);

        String hashMapString = gson.toJson(budgetRates);
        sharedPreferences.edit().putString("budgetRates", hashMapString).apply();

        int i = 0;
        for(String key : budgetRates.keySet()){
            codes[i] = key;
            i++;
        }
        budgetSpinner.setVisibility(View.VISIBLE);
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, codes);
        budgetSpinner.setAdapter(adapter);
        budgetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                budgetTextView.setText(String.format("%.2f", budgetRates.get(codes[position])));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        budgetSpinner.setSelection(12);
    }

    public void pokazTabeleKursow(View view){
        Intent intent = new Intent(getApplicationContext(),TabelaWalutActivity.class);
        startActivity(intent);
    }


    public void dokonajTransakcji(View view){
        Intent intent = new Intent(getApplicationContext(), TransakcjeActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.change_budget,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.change_budget){

            View dialogView = inflater.inflate(R.layout.change_budget_layout, null);
            final EditText editBudget = (EditText) dialogView.findViewById(R.id.editBudget);
            editBudget.setHint(String.format("%.2f",budgetRates.get("PLN")));
            editBudget.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_menu_edit)
                    .setTitle("Edycja budżetu")
                    .setMessage("Wpisz twój budżet w PLN:")
                    .setView(dialogView)
                    .setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            nowaWartosc = Double.parseDouble(editBudget.getText().toString());
                            totalBudget = nowaWartosc;
                            budgetRates.put("PLN",totalBudget);
                            budgetTextView.setText(String.format("%.2f", budgetRates.get("PLN")));
                            budgetSpinner.setSelection(12);

                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
                            String hashMapString = MainActivity.gson.toJson(budgetRates);
                            sharedPreferences.edit().putString("budgetRates", hashMapString).apply();
                        }
                    })
                    .setNegativeButton("Wróć", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            return true;
        }
        return false;
    }

    public void zobaczHistorie(View view){
        Intent intent = new Intent(getApplicationContext(), HistoriaActivity.class);
        startActivity(intent);
    }
}
