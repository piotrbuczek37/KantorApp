package com.ugprojects.kantorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class HistoriaActivity extends AppCompatActivity {

    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historia);

        ListView historiaListView = findViewById(R.id.historiaListView);
        Collections.sort(MainActivity.historia);
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,MainActivity.historia);
        historiaListView.setAdapter(arrayAdapter);

        historiaListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(HistoriaActivity.this)
                        .setIcon(android.R.drawable.ic_delete)
                        .setTitle("Czy jesteś pewien?")
                        .setMessage("Czy na pewno chcesz usunąć ten wpis?")
                        .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.historia.remove(position);
                                arrayAdapter.notifyDataSetChanged();

                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
                                HashSet<String> set = new HashSet<>(MainActivity.historia);
                                sharedPreferences.edit().putStringSet("historia",set).apply();
                            }
                        })
                        .setNegativeButton("Nie",null)
                        .show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.history_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.clearHistory){
            MainActivity.historia.clear();
            arrayAdapter.notifyDataSetChanged();

            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.ugprojects.kantorapp", Context.MODE_PRIVATE);
            HashSet<String> set = new HashSet<>(MainActivity.historia);
            sharedPreferences.edit().putStringSet("historia",set).apply();
            return true;
        }
        return false;
    }
}
