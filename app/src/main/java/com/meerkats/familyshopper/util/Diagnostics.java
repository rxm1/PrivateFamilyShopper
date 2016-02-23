package com.meerkats.familyshopper.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListMembers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rez on 28/01/2016.
 */
public class Diagnostics {
    private static final String ShoppingListMembersFilename = "ShoppingListMembersFilename";
    private static final String logTag = "Diagnostics";
    private static ShoppingListMembers shoppingListMembers = new ShoppingListMembers();

    public synchronized static void saveLastSyncedBy(Context context, ShoppingList shoppingList) {
        FSLog.verbose(logTag, "Diagnostics saveLastSyncedBy");

        String lastSyncedBy = shoppingList.getLastSyncedBy();
        long lastSeen = shoppingList.getLastSyncedBySeen();
        if (shoppingListMembers.containsMember(lastSyncedBy)) {
            shoppingListMembers.updateMember(lastSyncedBy, lastSeen);
        } else {
            shoppingListMembers.addMember(lastSyncedBy, lastSeen);
        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(ShoppingListMembersFilename, Context.MODE_PRIVATE));
            Gson gson = new Gson();
            Type gsonType = new TypeToken<ShoppingListMembers>() {
            }.getType();
            outputStreamWriter.write(gson.toJson(shoppingListMembers, gsonType));
            outputStreamWriter.close();
        } catch (IOException e) {
            FSLog.error(logTag, "Diagnostics saveLastSyncedBy", e);
            return;
        }

        return;
    }

    public static ArrayList<Map<String, String>> getShoppingListMembers(Context context) {
        FSLog.verbose(logTag, "Diagnostics getShoppingListMembers");

        ArrayList<Map<String, String>> membersArray = new ArrayList<Map<String, String>>();
        StringBuilder text = new StringBuilder();
        File file = new File(context.getFilesDir(), ShoppingListMembersFilename);
        if(!file.exists())
            return membersArray;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            Gson gson = new Gson();
            Type gsonType = new TypeToken<ShoppingListMembers>() {}.getType();
            ShoppingListMembers shoppingListMembers = gson.fromJson(text.toString(), gsonType);

            for (ShoppingListMembers.ShoppingListMember shoppingListMember: shoppingListMembers.getShoppingListMembers()) {
                Map<String, String> memberMap = new HashMap<String, String>();
                memberMap.put("Device ID", shoppingListMember.getName());
                memberMap.put("Last Seen", Long.toString(shoppingListMember.getLastSeen()));
                membersArray.add(memberMap);
            }
        }
        catch (IOException e) {
            FSLog.error(logTag, "Diagnostics getShoppingListMembers", e);
            return membersArray;
        }
        return membersArray;
    }
}
