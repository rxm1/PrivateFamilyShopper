package com.meerkats.familyshopper.model;

import java.util.Comparator;

/**
 * Created by Rez on 29/02/2016.
 */
public class ShoppingListComparator{

    public static Comparator<String> getComparator()
    {
        return new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        };
    }
}
