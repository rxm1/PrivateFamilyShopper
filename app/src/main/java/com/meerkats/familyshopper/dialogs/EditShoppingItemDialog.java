package com.meerkats.familyshopper.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.meerkats.familyshopper.MainActivity;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.Settings.Settings;

/**
 * Created by Rez on 21/12/2015.
 */
public class EditShoppingItemDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Button okBtn, cancelBtn;
    private EditText editText;
    private String newData;
    public String oldData;
    private boolean isCanceled = true;
    public Activity activity;

    public EditShoppingItemDialog(Activity activity, String oldData) {
        super(activity, Settings.getDialogColorTheme());
        FSLog.verbose(MainActivity.activity_log_tag, "EditShoppingItemDialog EditShoppingItemDialog");

        this.oldData = oldData;
        this.activity = activity;
        setCancelable(true);
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
        FSLog.verbose(MainActivity.activity_log_tag, "EditShoppingItemDialog onCreate");

        setContentView(com.meerkats.familyshopper.R.layout.edit_shopping_item);

        editText = (EditText)findViewById(com.meerkats.familyshopper.R.id.edit_shopping_item_edit_text);
        okBtn = (Button) findViewById(com.meerkats.familyshopper.R.id.edit_shopping_item_ok_btn);
        cancelBtn = (Button) findViewById(com.meerkats.familyshopper.R.id.edit_shopping_item_cancel_btn);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        setCancelable(true);

        setTitle("Edit: " + oldData);
        editText.setText(oldData);
        editText.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    @Override
    public void onClick(View v) {
        FSLog.verbose(MainActivity.activity_log_tag, "EditShoppingItemDialog onClick");

        newData = editText.getText().toString();
        switch (v.getId()) {
            case com.meerkats.familyshopper.R.id.edit_shopping_item_ok_btn:
                isCanceled = false;
                dismiss();
                break;
            case com.meerkats.familyshopper.R.id.edit_shopping_item_cancel_btn:
            default:
                cancel();
                break;
        }
    }
}
