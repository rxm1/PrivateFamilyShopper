package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;

/**
 * Created by Rez on 21/12/2015.
 */
public class ConnectUsingFirebaseDialog extends Dialog implements
        View.OnClickListener {

    private Button okBtn, cancelBtn;
    private EditText editText;
    private String newData = "";
    public String oldData = "";
    private boolean isCanceled = true;
    public Activity activity;



    public ConnectUsingFirebaseDialog(Activity activity) {
        super(activity, R.style.ListContextMenu);

        this.activity = activity;
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.connect_using_firebase);

        editText = (EditText)findViewById(R.id.edit_shopping_item_edit_text);
        okBtn = (Button) findViewById(R.id.edit_shopping_item_ok_btn);
        cancelBtn = (Button) findViewById(R.id.edit_shopping_item_cancel_btn);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        setCancelable(true);

        setTitle("Connect to Firebase");
        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
        if(settings.contains(MainController.Firebase_URL_Name)){
            oldData = settings.getString(MainController.Firebase_URL_Name, null);
        }
        editText.setText(oldData);
        editText.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    @Override
    public void onClick(View v) {
        newData = editText.getText().toString().trim();
        switch (v.getId()) {
            case R.id.edit_shopping_item_ok_btn:
                SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(MainController.Firebase_URL_Name, newData);
                editor.commit();

                isCanceled = false;
                dismiss();
                break;
            case R.id.edit_shopping_item_cancel_btn:
            default:
                cancel();
                break;
        }
    }

    public boolean isCanceled(){
        return isCanceled;
    }
}
