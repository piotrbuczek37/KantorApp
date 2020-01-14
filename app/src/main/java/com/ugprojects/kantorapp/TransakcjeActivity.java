package com.ugprojects.kantorapp;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransakcjeActivity extends AppCompatActivity {

    Spinner operationSpinner;
    String[] operations = new String[]{"Wybierz operację","Kupno", "Sprzedaż"};
    boolean whichOperation; //0 - kupno, 1 - sprzedaz
    String[] codesTransakcje = new String[35];
    Spinner codesSpinner;
    int codePosition;
    String wybranaPozycja;
    EditText iloscNumber;
    double ilePieniedzy = 0.0;
    double dostanieszPieniedzy = 0;
    double ileTerazJest = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

    HashMap<String,Double> budgetCodes = new HashMap<String, Double>();

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
                    budgetCodes.put(jsonPart.getString("code"),jsonPart.getDouble("mid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transakcje);

        DownloadTask task = new DownloadTask();
        task.execute("http://api.nbp.pl/api/exchangerates/tables/a?format=json");

        final TextView kupnoTextView = findViewById(R.id.kupnoTextView);
        final TextView sprzedazTextView = findViewById(R.id.sprzedazTextView);
        final TextView helpTextView2 = findViewById(R.id.helpTextView2);
        iloscNumber = findViewById(R.id.iloscNumber);
        iloscNumber.setFilters(new InputFilter[] {new MainActivity.DecimalDigitsInputFilter(10,2)});

        final Button confirmButton = findViewById(R.id.confirmButton);

        helpTextView2.setVisibility(View.INVISIBLE);
        kupnoTextView.setVisibility(View.INVISIBLE);
        sprzedazTextView.setVisibility(View.INVISIBLE);
        iloscNumber.setVisibility(View.INVISIBLE);
        confirmButton.setVisibility(View.INVISIBLE);

        codesSpinner = findViewById(R.id.codesSpinner);
        codesSpinner.setVisibility(View.INVISIBLE);


        operationSpinner = findViewById(R.id.spinnerOperation);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, operations);
        operationSpinner.setAdapter(adapter);
        operationSpinner.setSelection(0);
        operationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        kupnoTextView.setVisibility(View.INVISIBLE);
                        sprzedazTextView.setVisibility(View.INVISIBLE);
                        codesSpinner.setVisibility(View.INVISIBLE);
                        helpTextView2.setVisibility(View.INVISIBLE);
                        iloscNumber.setVisibility(View.INVISIBLE);
                        confirmButton.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        kupnoTextView.setVisibility(View.VISIBLE);
                        sprzedazTextView.setVisibility(View.INVISIBLE);
                        confirmButton.setVisibility(View.VISIBLE);
                        helpTextView2.setText("Ile złotych chcesz na to przeznaczyć?");
                        whichOperation = false;

                        codesSpinner.setVisibility(View.VISIBLE);
                        int i = 0;
                        for(String code : budgetCodes.keySet()){
                            codesTransakcje[i] = code;
                            i++;
                        }
                        ArrayAdapter<String> adapterCodes = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, codesTransakcje);
                        codesSpinner.setAdapter(adapterCodes);
                        codesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                codePosition = position;
                                helpTextView2.setVisibility(View.VISIBLE);
                                iloscNumber.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        break;
                    case 2:
                        sprzedazTextView.setVisibility(View.VISIBLE);
                        kupnoTextView.setVisibility(View.INVISIBLE);
                        confirmButton.setVisibility(View.VISIBLE);
                        helpTextView2.setText("Ile tej waluty chciałbyć sprzedać?");
                        whichOperation = true;

                        codesSpinner.setVisibility(View.VISIBLE);
                        int j = 0;
                        for(String code : budgetCodes.keySet()){
                            codesTransakcje[j] = code;
                            j++;
                        }
                        ArrayAdapter<String> adapterCodes2 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, codesTransakcje);
                        codesSpinner.setAdapter(adapterCodes2);
                        codesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                codePosition = position;
                                helpTextView2.setVisibility(View.VISIBLE);
                                iloscNumber.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void confirmTransaction(View view){
        wybranaPozycja = codesTransakcje[codePosition];
        final String currentDateandTime = sdf.format(new Date());
        if(!whichOperation){
            if(!iloscNumber.getText().toString().isEmpty()) {
                ilePieniedzy = Double.parseDouble(iloscNumber.getText().toString());
                if (ilePieniedzy <= MainActivity.totalBudget) {
                    final double ilePrzed = MainActivity.totalBudget;
                    MainActivity.totalBudget -= ilePieniedzy;
                    final double ilePo = MainActivity.totalBudget;
                    MainActivity.budgetRates.remove("PLN");
                    MainActivity.budgetRates.put("PLN", MainActivity.totalBudget);

                    dostanieszPieniedzy = ilePieniedzy / budgetCodes.get(wybranaPozycja);
                    dostanieszPieniedzy = MainActivity.round(dostanieszPieniedzy, 2);
                    ileTerazJest = MainActivity.budgetRates.get(wybranaPozycja);
                    MainActivity.budgetRates.remove(wybranaPozycja);
                    MainActivity.budgetRates.put(wybranaPozycja, dostanieszPieniedzy + ileTerazJest);

                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
                    String hashMapString = MainActivity.gson.toJson(MainActivity.budgetRates);
                    sharedPreferences.edit().putString("budgetRates", hashMapString).apply();

                    new AlertDialog.Builder(TransakcjeActivity.this)
                            .setIcon(android.R.drawable.ic_input_add)
                            .setTitle("Dostaniesz tyle:")
                            .setMessage("Dostaniesz tyle: " + String.valueOf(dostanieszPieniedzy) + wybranaPozycja)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.budgetSpinner.setSelection(12);
                                    MainActivity.budgetTextView.setText(String.format("%.2f", MainActivity.budgetRates.get("PLN")));
                                    MainActivity.historia.add(currentDateandTime + " kupno " + wybranaPozycja + " \nza " + String.format("%.2f", ilePieniedzy) + " PLN\n" + "Otrzymana kwota: " + String.valueOf(dostanieszPieniedzy) + wybranaPozycja +
                                            "\nSuma PLN przed transakcją: " + ilePrzed + "PLN" + "\nSuma PLN po transakcji: " + ilePo + "PLN");

                                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
                                    HashSet<String> set = new HashSet<>(MainActivity.historia);
                                    sharedPreferences.edit().putStringSet("historia", set).apply();
                                    finish();
                                }
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(TransakcjeActivity.this)
                            .setIcon(android.R.drawable.ic_delete)
                            .setTitle("Błąd")
                            .setMessage("Nie masz tyle pieniędzy w budżecie. Twój aktualny budżet to: " + MainActivity.totalBudget + "PLN")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
            else{
                Log.i("tAG", "Wprowadz numer");
            }
        }
        else{
            ilePieniedzy = Double.parseDouble(iloscNumber.getText().toString());
            if(!iloscNumber.getText().toString().isEmpty()) {
                if (ilePieniedzy <= MainActivity.budgetRates.get(wybranaPozycja)) {
                    dostanieszPieniedzy = ilePieniedzy * budgetCodes.get(wybranaPozycja);
                    dostanieszPieniedzy = MainActivity.round(dostanieszPieniedzy, 2);

                    ileTerazJest = MainActivity.budgetRates.get(wybranaPozycja);
                    MainActivity.budgetRates.remove(wybranaPozycja);
                    MainActivity.budgetRates.put(wybranaPozycja, ileTerazJest - ilePieniedzy);
                    final double ilePrzed = MainActivity.totalBudget;
                    MainActivity.totalBudget += dostanieszPieniedzy;
                    final double ilePo = MainActivity.totalBudget;
                    MainActivity.budgetRates.remove("PLN");
                    MainActivity.budgetRates.put("PLN", MainActivity.totalBudget);

                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
                    String hashMapString = MainActivity.gson.toJson(MainActivity.budgetRates);
                    sharedPreferences.edit().putString("budgetRates", hashMapString).apply();

                    new AlertDialog.Builder(TransakcjeActivity.this)
                            .setIcon(android.R.drawable.ic_input_add)
                            .setTitle("Dostaniesz tyle:")
                            .setMessage("Dostaniesz tyle: " + String.valueOf(dostanieszPieniedzy) + "PLN")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.budgetSpinner.setSelection(12);
                                    MainActivity.budgetTextView.setText(String.format("%.2f", MainActivity.budgetRates.get("PLN")));
                                    MainActivity.historia.add(currentDateandTime + " sprzedaż " + wybranaPozycja + " \nza " + String.format("%.2f", ilePieniedzy) + wybranaPozycja + "\nOtrzymana kwota: " + String.valueOf(dostanieszPieniedzy) + "PLN" +
                                            "\nSuma PLN przed transakcją: " + ilePrzed + "PLN" + "\nSuma PLN po transakcji: " + ilePo + "PLN");

                                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
                                    HashSet<String> set = new HashSet<>(MainActivity.historia);
                                    sharedPreferences.edit().putStringSet("historia", set).apply();
                                    finish();
                                }
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(TransakcjeActivity.this)
                            .setIcon(android.R.drawable.ic_delete)
                            .setTitle("Błąd")
                            .setMessage("Nie masz tyle pieniędzy w budżecie. Twój aktualny budżet w tej walucie to: " + String.format("%.2f", MainActivity.budgetRates.get(wybranaPozycja)) + wybranaPozycja)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
            else{
                Log.i("tAG", "Wprowadz numer");
            }
        }
    }
}
