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
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StockActivity extends Activity {

    GeneralUtils generalUtils = new GeneralUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        final String stockName = getIntent().getStringExtra("selectedStock");


        //Unfollow button - start
        Button unfollowButton = (Button) findViewById(R.id.btnDel);
        View.OnClickListener listenerDel = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(StockActivity.this, MainActivity.class);
                myIntent.putExtra("selectedStock", stockName);
                startActivity(myIntent);
            }
        };

        unfollowButton.setOnClickListener(listenerDel);
        //Unfollow button - end

        //Stats - start
        TextView tv = (TextView) findViewById(R.id.statsText);
        tv.setText(Html.fromHtml(generalUtils.getStat(stockName)));
        //Stats - end

    }
}
