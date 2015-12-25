package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Rez on 21/12/2015.
 */
public class EditShoppingItemDialog extends AlertDialog implements
        android.view.View.OnClickListener {

    private Button okBtn, cancelBtn;
    private EditText editText;
    private String newData;
    public String oldData;
    private boolean isCanceled = false;
    public Activity activity;

    public EditShoppingItemDialog(Activity activity, String oldData) {
        super(activity);
        this.oldData = oldData;
        this.activity = activity;
    }

    public String getNewData(){
        return newData;
    }
    public Boolean isCanceled(){
        return isCanceled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_shopping_item);

        editText = (EditText)findViewById(R.id.edit_shopping_item_edit_text);
        okBtn = (Button) findViewById(R.id.edit_shopping_item_ok_btn);
        cancelBtn = (Button) findViewById(R.id.edit_shopping_item_cancel_btn);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        setCancelable(true);

        setTitle("Edit: " + oldData);
        editText.setText(oldData);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @Override
    public void onClick(View v) {
        newData = editText.getText().toString();
        switch (v.getId()) {
            case R.id.edit_shopping_item_ok_btn:
                dismiss();
                break;
            case R.id.edit_shopping_item_cancel_btn:
            default:
                isCanceled = true;
                cancel();
                break;
        }
    }
}
