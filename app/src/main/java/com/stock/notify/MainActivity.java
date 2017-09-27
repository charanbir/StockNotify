/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stock.notify;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    AlarmReceiver alarm = new AlarmReceiver();
    List<String> watchList = new ArrayList<String>();
    GeneralUtils generalUtils = new GeneralUtils();
    /**
     * Declaring an ArrayAdapter to set items to ListView
     */
    ArrayAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Unfollow - start
        String selectedStock = getIntent().getStringExtra("selectedStock");

        final EditText edit = (EditText) findViewById(R.id.txtItem);
        edit.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        watchList.addAll(generalUtils.getWatchList(getApplicationContext(), generalUtils.stockFile));

        if (getIntent().getStringExtra("selectedStock") != null) {
            watchList.remove(selectedStock);
            updateWatchList(watchList, generalUtils.stockFile);
        }
        //Unfollow - end

        //Follow - start
        Button followButton = (Button) findViewById(R.id.btnAdd);

        listView = (ListView) findViewById(android.R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent myIntent = new Intent(MainActivity.this, StockActivity.class);
                myIntent.putExtra("selectedStock", listView.getItemAtPosition(i).toString());
                startActivity(myIntent);
            }
        });
        //Follow - end

        /** Defining the ArrayAdapter to set items to ListView */
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, watchList);

        /** Defining a click event listener for the button "Add" */
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                String stockName = edit.getText().toString().trim();

                //validate
                if (generalUtils.isValidStock(getApplicationContext(), stockName)) {
                    watchList.add(stockName);
                    updateWatchList(watchList, generalUtils.stockFile);
                }
                edit.setText("");
                adapter.notifyDataSetChanged();
            }
        };

        /** Setting the event listener for the add button */
        followButton.setOnClickListener(listener);

        /** Setting the adapter to the ListView */
        setListAdapter(adapter);
    }

    protected void updateWatchList(List<String> list, String fileName) {
        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            for (String text : list) {
                outputStream.write(text.getBytes());
                outputStream.write("\r\n".getBytes());
            }
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Menu options to set and cancel the alarm.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_action:
                alarm.setAlarm(this);
                Toast.makeText(getApplicationContext(), "You will now receive notifications", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.cancel_action:
                alarm.cancelAlarm(this);
                Toast.makeText(getApplicationContext(), "Notifications have been deactivated", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.table_action:
                Intent myIntent = new Intent(MainActivity.this, TableActivity.class);
                startActivity(myIntent);
                return true;
        }
        return false;
    }
}
