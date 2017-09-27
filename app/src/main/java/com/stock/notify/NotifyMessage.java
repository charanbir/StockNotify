package com.stock.notify;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class NotifyMessage extends Activity {

    private GeneralUtils generalUtils = new GeneralUtils();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setMovementMethod(new ScrollingMovementMethod());
        try {
            textView.setText(Html.fromHtml(generalUtils.getNotificationMessage(getApplicationContext())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(textView);
    }

}
