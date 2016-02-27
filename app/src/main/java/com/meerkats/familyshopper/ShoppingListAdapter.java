package com.meerkats.familyshopper;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;
import com.meerkats.familyshopper.util.FSLog;

/**
 * Created by Rez on 18/12/2015.
 */
public class ShoppingListAdapter extends ArrayAdapter<String> {
    private final Context context;
    ShoppingList shoppingList;

    public ShoppingListAdapter(Context context, ShoppingList shoppingList){
        super(context, com.meerkats.familyshopper.R.layout.shopping_list, shoppingList);
        FSLog.verbose(MainActivity.activity_log_tag, "ShoppingListAdapter ShoppingListAdapter");

        this.context = context;
        this.shoppingList = shoppingList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        FSLog.verbose(MainActivity.activity_log_tag, "ShoppingListAdapter getView");

        ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(com.meerkats.familyshopper.R.layout.shopping_list, parent, false);
        TextView textView = (TextView)rowView.findViewById(com.meerkats.familyshopper.R.id.shoppingListItemTextView);
        textView.setText(shoppingListItem.getShoppingListItem());

        if (shoppingListItem.isCrossedOff()) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(textView.getTextColors().withAlpha(50));
        } else {
            //textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        if(shoppingListItem.getIsDeleted()){
            LayoutInflater myInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(com.meerkats.familyshopper.R.layout.deleted_shopping_list, parent, false);
        }

        return rowView;

    }


}
