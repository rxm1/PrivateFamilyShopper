package com.meerkats.familyshopper.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListMembers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

/**
 * Created by Rez on 28/01/2016.
 */
public class Diagnostics {
    private static final String ShoppingListMembersFilename = "ShoppingListMembersFilename";
    private static final String logTag = "Diagnostics";
    private static ShoppingListMembers shoppingListMembers = new ShoppingListMembers();

    public static void saveLastSyncedBy(Context context, ShoppingList shoppingList){
        String lastSyncedBy = shoppingList.getLastSyncedBy();
        long lastSeen = shoppingList.getLastSyncedBySeen();
        if(shoppingListMembers.containsMember(lastSyncedBy)){
            shoppingListMembers.updateMember(lastSyncedBy, lastSeen);
        }
        else {
            shoppingListMembers.addMember(lastSyncedBy, lastSeen);
        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(ShoppingListMembersFilename, Context.MODE_PRIVATE));
            Gson gson = new Gson();
            Type gsonType = new TypeToken<ShoppingListMembers>() {}.getType();
            outputStreamWriter.write(gson.toJson(shoppingListMembers, gsonType));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            FSLog.error(logTag, "Diagnostics saveLastSyncedBy", e);
            return;
        }

        return;
    }

    public static ShoppingListMembers getShoppingListMembers(){return shoppingListMembers;}
}
