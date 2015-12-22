package com.meerkats.familyshopper;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
        textView.setText(shoppingListItem.getShoppingListItem());

        if(shoppingListItem.isCrossedOff()) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        //Toast.makeText(getContext(), "in Adapter getView" + position, Toast.LENGTH_SHORT).show();
        return rowView;

    }


}
