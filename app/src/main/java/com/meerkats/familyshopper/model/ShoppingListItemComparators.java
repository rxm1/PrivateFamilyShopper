package com.meerkats.familyshopper.model;

import com.meerkats.familyshopper.Settings.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Rez on 29/02/2016.
 */
public class ShoppingListItemComparators {

    public static void sort(ArrayList<ShoppingListItem> shoppingListItems){
        switch (Settings.getSortBy()){
            case "AlphabetAsc":
            case "AlphabetDesc":
                Collections.sort(shoppingListItems, ShoppingListItemComparators.Alphabetize);
                break;
            case "DateEnteredAsc":
            case "DateEnteredDesc":
                Collections.sort(shoppingListItems, ShoppingListItemComparators.DateEntered);
                break;
        }

    }
    public static Comparator<ShoppingListItem> Alphabetize = new Comparator<ShoppingListItem>() {
        @Override
        public int compare(ShoppingListItem lhs, ShoppingListItem rhs) {
            if(Settings.crossedOffItemsAtBottom()) {
                if (lhs.isCrossedOff() && !rhs.isCrossedOff())
                    return 1;
                if (!lhs.isCrossedOff() && rhs.isCrossedOff())
                    return -1;
            }

            return descOrAsc(lhs.getShoppingListItem().compareTo(rhs.getShoppingListItem()));
        }
        private int descOrAsc(int initialResult){
            return Settings.getSortBy().equals("AlphabetDesc") ? initialResult * -1 : initialResult;
        }
    };

    public static Comparator<ShoppingListItem> DateEntered = new Comparator<ShoppingListItem>() {
        @Override
        public int compare(ShoppingListItem lhs, ShoppingListItem rhs) {
            if (Settings.crossedOffItemsAtBottom()) {
                if (lhs.isCrossedOff() && !rhs.isCrossedOff())
                    return 1;
                if (!lhs.isCrossedOff() && rhs.isCrossedOff())
                    return -1;
            }

            return descOrAsc((int) (lhs.getDateCreated() - rhs.getDateCreated()));
        }

        private int descOrAsc(int initialResult) {
            return Settings.getSortBy().equals("DateEnteredDesc") ? initialResult * -1 : initialResult;
        }
    };
}
