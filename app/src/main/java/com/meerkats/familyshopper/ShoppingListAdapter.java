package com.meerkats.familyshopper;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

/**
 * Created by Rez on 18/12/2015.
 */
public class ShoppingListAdapter extends ArrayAdapter<String> {
    private final Context context;
    ShoppingList shoppingList;


    public ShoppingListAdapter(Context context, ShoppingList shoppingList){
        super(context, R.layout.shopping_list, shoppingList);
        this.context = context;
        this.shoppingList = shoppingList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.shopping_list, parent, false);
        TextView textView = (TextView)rowView.findViewById(R.id.shoppingListItemTextView);
        ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);

        if(shoppingListItem.isSelectedForEdit()) {
            setItemEdited(position, convertView, parent, textView, rowView, shoppingListItem);
        }

        if(shoppingListItem.isCrossedOff()) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        textView.setText(shoppingList.get(position));
        //Toast.makeText(getContext(), "in Adapter getView" + position, Toast.LENGTH_SHORT).show();
        return rowView;

    }

    public void setItemEdited(int position, View convertView, ViewGroup parent, final TextView textView, View rowView, final ShoppingListItem shoppingListItem) {
        final EditText editText = (EditText) rowView.findViewById(R.id.shoppingListItemEditText);
        final ImageButton cancelButton = (ImageButton) rowView.findViewById(R.id.shoppingListItemCancelButton);
        final ImageButton okButton = (ImageButton) rowView.findViewById(R.id.shoppingListItemOKButton);

        editText.setText(shoppingListItem.getShoppingListItem());
        textView.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        okButton.setVisibility(View.VISIBLE);
        editText.requestFocus();

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    shoppingListItem.setSelectedForEdit(false);
                }
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(context, "ok adapter", Toast.LENGTH_SHORT).show();
            }

        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shoppingListItem.setSelectedForEdit(false);
                textView.setVisibility(View.VISIBLE);
                editText.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
            }

        });
    }
}
