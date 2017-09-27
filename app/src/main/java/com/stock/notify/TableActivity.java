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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONObject;

public class TableActivity extends Activity {

    GeneralUtils generalUtils = new GeneralUtils();
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);

        //heading
        View tableRow = LayoutInflater.from(this).inflate(R.layout.activity_table_item, null, false);
        TextView openTextView = (TextView) tableRow.findViewById(R.id.open);
        TextView yearLowTextView = (TextView) tableRow.findViewById(R.id.yearLow);
        TextView percentChangeTextView = (TextView) tableRow.findViewById(R.id.percentChangeFromYearLow);
        TextView communityRatingTextView = (TextView) tableRow.findViewById(R.id.communityRating);
        openTextView.setText("Current");
        yearLowTextView.setText("Year Low");
        percentChangeTextView.setText("% Change");
        communityRatingTextView.setText("Nasdaq");
        tableLayout.addView(tableRow);

        //blank line
        tableRow = LayoutInflater.from(this).inflate(R.layout.activity_table_item, null, false);
        tableLayout.addView(tableRow);

        //rows
        for (final String stockName : generalUtils.getWatchList(getApplicationContext(), "Data.txt")) {
            try {
                JSONObject json = generalUtils.getJson(stockName);
                JSONObject json2 = generalUtils.getJson2(stockName);
                tableRow = LayoutInflater.from(this).inflate(R.layout.activity_table_item, null, false);
                TextView nameTextView = (TextView) tableRow.findViewById(R.id.name);
                openTextView = (TextView) tableRow.findViewById(R.id.open);
                yearLowTextView = (TextView) tableRow.findViewById(R.id.yearLow);
                percentChangeTextView = (TextView) tableRow.findViewById(R.id.percentChangeFromYearLow);
                communityRatingTextView = (TextView) tableRow.findViewById(R.id.communityRating);

                String openString = "";
                for (int i = 1; i <= 3 && TextUtils.isEmpty(openString); i++) {
                    openString = json2.get("03. Latest Price").toString().substring(0, json2.get("03. Latest Price").toString().length() - 2);
                }

                if (openString.equals("0.00")) {
                    openString = json.get("Open").toString();
                }

                String yearLowString = "";
                for (int i = 1; i <= 3 && TextUtils.isEmpty(yearLowString); i++) {
                    yearLowString = json.get("YearLow").toString();
                }

                String percentChangeString = "";
                for (int i = 1; i <= 3 && TextUtils.isEmpty(percentChangeString); i++) {
                    percentChangeString = json.get("PercentChangeFromYearLow").toString();
                }

                try {
                    nameTextView.setText(stockName + " (" + json.get("Name").toString().substring(0, 11) + ")");
                } catch (Exception e) {
                    nameTextView.setText(stockName + " (" + json.get("Name") + ")");
                }
                openTextView.setText(openString.equals("null") ? "-" : openString);
                yearLowTextView.setText(yearLowString.equals("null") ? "-" : yearLowString);
                percentChangeTextView.setText(percentChangeString.equals("null") ? "-" : percentChangeString);
                communityRatingTextView.setText(generalUtils.getCommunityRating(stockName));

                tableLayout.addView(tableRow);

                tableRow.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent myIntent = new Intent(TableActivity.this, StockActivity.class);
                        myIntent.putExtra("selectedStock", stockName);
                        startActivity(myIntent);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
