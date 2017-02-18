package com.dev.fondson.NoteLocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog fragment that allows user to select an item from a list
 */
public class ItemPickerDialogFragment extends DialogFragment {
    public static final String LOGTAG = "ItemPickerDialogFragment";

    /**
     * An item that can be displayed and selected by the ItemPickerDialogFragment
     */
    public static class Item {
        private String title;
        private int intValue;
        private String stringValue;

        private static final String KEY_TITLE        = "title";
        private static final String KEY_INT_VALUE    = "intValue";
        private static final String KEY_STRING_VALUE = "stringValue";

        /**
         * Construct with title and integer value
         *
         * @param title Name displayed in list
         * @param value Integer value associated with item
         */
        public Item(String title, int value) {
            assert(!TextUtils.isEmpty(title));

            this.title = title;
            this.intValue = value;
        }

        /**
         * Construct with title and string value
         *
         * @param title Name displayed in list
         * @param value String value associated with item
         */
        public Item(String title, String value) {
            assert(!TextUtils.isEmpty(title));

            this.title = title;
            this.stringValue = value;
        }

        /**
         * Construct from a bundle of values
         * @param bundle
         */
        public Item(Bundle bundle) {
            title = bundle.getString(KEY_TITLE, null);
            intValue = bundle.getInt(KEY_INT_VALUE, 0);
            stringValue = bundle.getString(KEY_STRING_VALUE, null);
        }

        /**
         * Get a Bundle of values that can be passed to the Item(Bundle) constructor
         * to re-create the object
         *
         * @return Bundle
         */
        public Bundle getValuesBundle() {
            Bundle bundle = new Bundle();

            bundle.putString(KEY_TITLE, title);
            bundle.putInt(KEY_INT_VALUE, intValue);
            if (stringValue != null) {
                bundle.putString(KEY_STRING_VALUE, stringValue);
            }

            return bundle;
        }

        public String getTitle() {
            return title;
        }

        public int getIntValue() {
            return intValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        /**
         * Given a list of items, create a Bundle that can be passed to
         * Item.itemsFromBundle() to recreate them.
         *
         * @param items list of items
         * @return Bundle
         */
        public static Bundle bundleOfItems(List<Item> items) {
            int itemCount = items.size();
            ArrayList<Bundle> itemBundles = new ArrayList<>();
            for (int i = 0; i < itemCount; ++i) {
                itemBundles.add(items.get(i).getValuesBundle());
            }

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(ARG_ITEMS, itemBundles);
            return bundle;
        }

        /**
         * Given a Bundle created by Item.bundleOfItems(), recreate the
         * original list of items.
         *
         * @param bundle Bundle created by Item.bundleOfItems()
         * @return ArrayList&lt;Item&gt;
         */
        public static ArrayList<Item> itemsFromBundle(Bundle bundle) {
            ArrayList<Bundle> itemBundles = bundle.getParcelableArrayList(ARG_ITEMS);
            ArrayList<Item> items = new ArrayList<>();
            for (Bundle itemBundle: itemBundles) {
                items.add(new Item(itemBundle));
            }
            return items;
        }
    }

    /**
     * Interface for notification of item selection
     *
     * If the owning Activity implements this interface, then the fragment will
     * invoke its onItemSelected() method when the user clicks the OK button.
     */
    public interface OnItemSelectedListener {
        void onItemSelected(ItemPickerDialogFragment fragment, Item item, int index);
    }

    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_ITEMS = "ARG_ITEMS";
    private static final String ARG_SELECTED_INDEX = "ARG_SELECTED_INDEX";

    /**
     * Create a new instance of ItemPickerDialogFragment with specified arguments
     *
     * @param title Dialog title text
     * @param items Selectable items
     * @param selectedIndex initial selection index, or -1 if no item should be pre-selected
     * @return ItemPickerDialogFragment
     */
    public static ItemPickerDialogFragment newInstance(String title, ArrayList<Item> items, int selectedIndex) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putBundle(ARG_ITEMS, Item.bundleOfItems(items));
        args.putInt(ARG_SELECTED_INDEX, selectedIndex);

        ItemPickerDialogFragment fragment = new ItemPickerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String title;
    private ArrayList<Item> items;
    private int selectedIndex;

    /**
     * Constructor
     */
    public ItemPickerDialogFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_SELECTED_INDEX, selectedIndex);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(ARG_TITLE, "Dialog");
            items = Item.itemsFromBundle(args.getBundle(ARG_ITEMS));
            selectedIndex = args.getInt(ARG_SELECTED_INDEX, -1);
        }

        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(ARG_SELECTED_INDEX, selectedIndex);
        }

        String[] itemTitles = getItemTitlesArray();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOGTAG, "OK button clicked");

                        Activity activity = getActivity();
                        if (activity instanceof OnItemSelectedListener) {
                            if (0 <= selectedIndex && selectedIndex < items.size()) {
                                Item item = items.get(selectedIndex);
                                OnItemSelectedListener listener = (OnItemSelectedListener)activity;
                                listener.onItemSelected(ItemPickerDialogFragment.this, item, selectedIndex);
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOGTAG, "Cancel button clicked");

                        // OK, just let the dialog be closed
                    }
                })
                .setSingleChoiceItems(itemTitles, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOGTAG, "User clicked item with index " + which);
                        selectedIndex = which;
                    }
                });

        return builder.create();
    }

    private String[] getItemTitlesArray() {
        final int itemCount = items.size();
        String[] itemTitles = new String[itemCount];
        for (int i = 0; i < itemCount; ++i) {
            itemTitles[i] = items.get(i).getTitle();
        }
        return itemTitles;
    }
}