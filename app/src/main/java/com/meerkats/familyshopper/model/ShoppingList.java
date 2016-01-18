package com.meerkats.familyshopper.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Rez on 17/12/2015.
 */
public class ShoppingList extends ArrayList<String>{
    Gson gson = new Gson();
    Type gsonType = new TypeToken<InnerShoppingList>() {}.getType();

    private InnerShoppingList innerShoppingList;

    public ShoppingList(String name){
        innerShoppingList = new InnerShoppingList(name);
    }
    public ShoppingList(){
        innerShoppingList = new InnerShoppingList("");
    }

    public synchronized String getShoppingListName() {
        return innerShoppingList.getShoppingListName();
    }


    @Override
    public synchronized boolean add(String item){
        super.add(item);
        innerShoppingList.add(new ShoppingListItem(item));
        return true;
    }
    public synchronized void add(ShoppingListItem shoppingListItem){
        super.add(shoppingListItem.getShoppingListItem());
        shoppingListItem.setLastModified(new Date());
        innerShoppingList.add(shoppingListItem);
    }

    @Override
    public synchronized String remove(int position){
        String item = super.remove(position);
        innerShoppingList.remove(position);
        return item;
    }
    public synchronized void markAsDeleted(int position){
        innerShoppingList.markAsDeleted(position);
    }

    public synchronized void deleteAll(){
        for (int i=0; i<innerShoppingList.getShoppingListItems().size(); i++) {
            innerShoppingList.markAsDeleted(i);
        }
    }
    public synchronized void clearCrossedOff(){
        for (int i = 0; i < innerShoppingList.getShoppingListItems().size(); i++) {
            ShoppingListItem listItem = innerShoppingList.getShoppingListItems().get(i);
            if(listItem.isCrossedOff()) {
                innerShoppingList.markAsDeleted(i);
            }
        }
    }
    public synchronized void setItemCrossedOff(int position){
        ShoppingListItem shoppingListItem = innerShoppingList.getShoppingListItems().get(position);
        shoppingListItem.setIsCrossedOff(!shoppingListItem.isCrossedOff());
        shoppingListItem.setLastModified(new Date());
        innerShoppingList.setShoppingListItem(position, shoppingListItem);
    }


    public synchronized void loadShoppingList(String newGson){
            super.clear();
            if (newGson != null && !newGson.isEmpty())
                innerShoppingList = gson.fromJson(newGson, gsonType);
            else
                innerShoppingList = new InnerShoppingList("");

            for (int i = 0; i < innerShoppingList.getShoppingListItems().size(); i++) {
                this.add(i, innerShoppingList.getShoppingListItems().get(i).getShoppingListItem());
            }
    }
    public synchronized String getJson(){
        return gson.toJson(innerShoppingList, gsonType);
    }


    public synchronized ShoppingListItem getShoppingListItem(int index){
        return innerShoppingList.getShoppingListItems().get(index);
    }
    public synchronized ArrayList<ShoppingListItem> getShoppingListItems(){
        return innerShoppingList.getShoppingListItems();
    }

    public synchronized void setShoppingListItemEdit(ShoppingListItem shoppingListItem, int position){
        shoppingListItem.setLastModified(new Date());
        innerShoppingList.setShoppingListItem(position, shoppingListItem);
        this.set(position, shoppingListItem.toString());
    }

    public synchronized long getLastUpdated(){return innerShoppingList.getLastModified();}

    public boolean equals(ShoppingList other){return innerShoppingList.equals(other.innerShoppingList);}
    class InnerShoppingList
    {
        private String shoppingListName;
        private ArrayList<ShoppingListItem> shoppingListItems;
        private long lastModified;

        public InnerShoppingList(String shoppingListName)
        {
            shoppingListItems = new ArrayList<ShoppingListItem>();
            this.shoppingListName = shoppingListName;
            lastModified = 0;
        }

        public void add(ShoppingListItem shoppingListItem){
            shoppingListItems.add(shoppingListItem);
            setLastModified(shoppingListItem.getLastModified().getTime());
        }

        public void remove(int postion){
            shoppingListItems.remove(postion);
            setLastModified(new Date().getTime());
        }

        public void markAsDeleted(int postion){
            ShoppingListItem shoppingListItem = shoppingListItems.get(postion);
            shoppingListItem.setIsDeleted(true);
            Date now = new Date();
            shoppingListItem.setLastModified(now);
            setLastModified(now.getTime());
            shoppingListItems.set(postion, shoppingListItem);
        }

        private void clear(){
            shoppingListItems.clear();
            setLastModified(new Date().getTime());
        }

        public void setShoppingListItem(int position, ShoppingListItem shoppingListItem){
            shoppingListItems.set(position, shoppingListItem);
            setLastModified(shoppingListItem.getLastModified().getTime());
        }
        public String getShoppingListName(){return shoppingListName;}
        public void setShoppingListName(String shoppingListName){
            this.shoppingListName=shoppingListName;
            setLastModified(new Date().getTime());
        }
        public ArrayList<ShoppingListItem> getShoppingListItems(){return shoppingListItems;}
        public void setShoppingListItems(ArrayList<ShoppingListItem> shoppingListItems){
            this.shoppingListItems=shoppingListItems;
            setLastModified(new Date().getTime());
        }
        public long getLastModified(){return lastModified;}
        public void setLastModified(long lastModified){this.lastModified = lastModified;}

        @Override
        public synchronized String toString(){
            String items = "";
            for (int i=0; i < shoppingListItems.size(); i++) {
                items += shoppingListItems.get(i).toString();
            }
            return "ShoppingList [shopping_list_name=" + shoppingListName + ", "
                    + "last_modified=" + lastModified + ", "
                    + items + "]";
        }

        public synchronized boolean equals(InnerShoppingList other){
            //if(this.lastModified!=other.lastModified) return false;
            if(this.shoppingListName!=other.shoppingListName) return false;
            if(this.shoppingListItems.size()!=other.shoppingListItems.size()) return false;

            HashMap<UUID, ShoppingListItem> otherListHash = new HashMap<>(other.shoppingListItems.size());
            for (ShoppingListItem i : other.getShoppingListItems()) otherListHash.put(i.getGuid(),i);

            for (ShoppingListItem thisItem : this.getShoppingListItems()) {
                if (otherListHash.containsKey(thisItem.getGuid())) {
                    ShoppingListItem otherItem = otherListHash.get(thisItem.getGuid());
                    if (!thisItem.equals(otherItem))
                        return false;

                    otherListHash.remove(thisItem.getGuid());
                } else
                    return false;
            }
            if(otherListHash.size()>0)
                return false;

            return true;
        }
    }
}
