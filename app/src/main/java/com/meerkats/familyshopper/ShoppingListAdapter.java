package com.meerkats.familyshopper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.meerkats.familyshopper.model.ShoppingList;

import java.util.ArrayList;

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
        TextView textView = (TextView)rowView.findViewById(R.id.shoppingListTextView);
        textView.setText(shoppingList.get(position));

        //Toast.makeText(getContext(), "in Adapter getView" + position, Toast.LENGTH_SHORT).show();
        return rowView;

    }

}
