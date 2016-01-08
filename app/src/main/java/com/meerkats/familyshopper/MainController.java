package com.meerkats.familyshopper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

/**
 * Created by Rez on 07/01/2016.
 */
public class MainController {
    Firebase myFirebaseRef;
    Context context;
    DataComparer dataComparer;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;

    public MainController(Context mainContext) {
        this.context = mainContext;
        Firebase.setAndroidContext(context);
        dataComparer = new DataComparer(context);
        myFirebaseRef = new Firebase("https://familyshopper.firebaseio.com/");
        shoppingList = new ShoppingList("mainList", context, myFirebaseRef);

    }

    public void init(){
        shoppingList.loadShoppingListFromFile();

        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dataComparer.dataChanged(snapshot, shoppingList, shoppingListAdapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(context, "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setShoppingListItemDelete(int position){
        shoppingList.remove(position);
        shoppingListAdapter.notifyDataSetChanged();
    }

    public void setShoppingListItemEdit(final AdapterView<?> parent, final View v, final int position, long id, Activity activity){
        final ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);
        EditShoppingItemDialog cdd=new EditShoppingItemDialog(activity, shoppingListItem.getShoppingListItem());

        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (((EditShoppingItemDialog) dialog).isCanceled())
                    return;

                String newData = ((EditShoppingItemDialog) dialog).getNewData();
                shoppingListItem.setShoppingListItem(newData);
                shoppingList.setShoppingListItemEdit(shoppingListItem, position);

                shoppingListAdapter.notifyDataSetChanged();

            }
        });
        cdd.show();
    }

    public void setShoppingItemCrossedOff(int position){
        shoppingList.setItemCrossedOff(position);
        shoppingListAdapter.notifyDataSetChanged();
    }

    public void addItemToShoppingList(String item){
        shoppingList.add(item);
        shoppingListAdapter.notifyDataSetChanged();
    }

    public ShoppingList getShoppingList(){ return shoppingList; }

    public void setShoppingListAdapter(ShoppingListAdapter shoppingListAdapter){
        this.shoppingListAdapter = shoppingListAdapter;
    }
}
