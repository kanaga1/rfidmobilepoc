package com.example.rfidapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawerListContent {
    //An array of sample (Settings) items.
    public static List<DrawerItem> ITEMS = new ArrayList<>();

    //A map of sample (Settings) items, by ID.
    public static Map<String, DrawerItem> ITEM_MAP = new HashMap<>();

    static {
        // Add items.
        //addItem(new DrawerItem("1", "Home", R.drawable.app_icon));
        addItem(new DrawerItem("1", "Rapid Read", R.drawable.btn_rr));
        addItem(new DrawerItem("2", "Inventory Item", R.drawable.btn_inv));
        addItem(new DrawerItem("3", "Locate Tag", R.drawable.btn_locate));
        addItem(new DrawerItem("4", "Tag Register", R.drawable.register));
        addItem(new DrawerItem("5", "Expiry Date", R.drawable.deadline));
        addItem(new DrawerItem("6", "Readers List", R.drawable.dl_rdl));
//        addItem(new DrawerItem("7", "Beeper", R.drawable.volume));
//        addItem(new DrawerItem("8", "About", R.drawable.dl_about));
    }

    /**
     * Method to add a new item
     *
     * @param item - Item to be added
     */
    private static void addItem(DrawerItem item) {

        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A Drawer item represents an entry in the navigation drawer.
     */
    public static class DrawerItem {
        public String id;
        public String content;
        public int icon;

        public DrawerItem(String id, String content, int icon_id) {
            this.id = id;
            this.content = content;
            this.icon = icon_id;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}

