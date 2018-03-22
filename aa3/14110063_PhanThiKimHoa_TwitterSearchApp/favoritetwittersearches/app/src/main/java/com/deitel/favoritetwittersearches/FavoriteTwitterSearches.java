package com.deitel.favoritetwittersearches;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Arrays;

public class FavoriteTwitterSearches extends Activity {
    private SharedPreferences savedSearches; // user's favorite searches
    private TableLayout queryTableLayout; // shows the search buttons
    private EditText queryEditText; // where the user enters queries
    private EditText tagEditText; // where the user enters a query's tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        savedSearches = getSharedPreferences("searches", MODE_PRIVATE);
        // get a reference to the queryTableLayout
        queryTableLayout =
                (TableLayout) findViewById(R.id.queryTableLayout);

        // get references to the two EditTexts and the Save Button
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);

        // register listeners for the Save and Clear Tags Buttons
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);
        Button clearTagsButton = (Button) findViewById(R.id.clearTagsButton);
        clearTagsButton.setOnClickListener(clearTagsButtonListener);

        refreshButtons(null); // add previously saved searches to GUI
    }

    private void refreshButtons(String newTag) {
        if (savedSearches.getAll() != null) {
            String[] tags = savedSearches.getAll().keySet().toArray(new String[0]);
            Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER); // sort by tag
            if (newTag != null) {
                makeTagGUI(newTag, Arrays.binarySearch(tags, newTag));
            }//endif
            else // display GUI for all tags
            {
                // display all saved searches
                for (int index = 0; index < tags.length; ++index)
                    makeTagGUI(tags[index], index);
            } // end else
        }
    }

    private void makeTag(String query, String tag) {
        // originalQuery will be null if we're modifying an existing search
        String originalQuery = savedSearches.getString(tag, null);
        // get a SharedPreferences.Editor to store new tag/query pair
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query); // store current search
        preferencesEditor.apply(); // store the updated preferences
        // if this is a new query, add its GUI
        if (originalQuery == null)
            refreshButtons(tag); // adds a new button for this tag

    }

    private void makeTagGUI(String tag, int index) {
        // get a reference to the LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

// inflate new_tag_view.xml to create new tag and edit Buttons
        View newTagView = inflater.inflate(R.layout.new_tag_view, null);
        Button newTagButton =
                (Button) newTagView.findViewById(R.id.newTagButton);
        newTagButton.setText(tag);
        newTagButton.setOnClickListener(queryButtonListener);

        // get newEditButton and register its listener
        Button newEditButton =
                (Button) newTagView.findViewById(R.id.newEditButton);
        newEditButton.setOnClickListener(editButtonListener);
        // add new tag and edit buttons to queryTableLayout
        queryTableLayout.addView(newTagView, index);
    }

    private void clearButtons() {
        // remove all saved search Buttons
        queryTableLayout.removeAllViews();
    } // end method clearButtons

    public View.OnClickListener saveButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0) {
                makeTag(queryEditText.getText().toString(), tagEditText.getText().toString());
                queryEditText.setText("");
                tagEditText.setText("");
                // hide the soft keyboard
                ((InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        tagEditText.getWindowToken(), 0);
            } // end if
            else {
                // create a new AlertDialog Builder
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(FavoriteTwitterSearches.this);
                builder.setTitle(R.string.missingTitle); // title bar string
                builder.setPositiveButton(R.string.OK, null);
                builder.setMessage(R.string.missingMessage);
                AlertDialog errorDialog = builder.create();
                errorDialog.show(); // display the Dialog
            }
        }
    };

    public View.OnClickListener clearTagsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(FavoriteTwitterSearches.this);
            builder.setTitle(R.string.confirmTitle); // title bar string
            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.erase, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    clearButtons();
                    SharedPreferences.Editor preferencesEditor =
                            savedSearches.edit();
                    preferencesEditor.clear();
                    preferencesEditor.apply();
                }
            });
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setMessage(R.string.confirmMessage);
            AlertDialog confirmDialog = builder.create();
            confirmDialog.show();
        }
    };
    public View.OnClickListener queryButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the query
            String buttonText = ((Button) v).getText().toString();
            String query = savedSearches.getString(buttonText, null);
            // create the URL corresponding to the touched Button's query
            String urlString = getString(R.string.searchURL) + query;
            // create an Intent to launch a web browser
            Intent getURL = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(urlString));
            startActivity(getURL); // execute the Intent
        }
    };
    public View.OnClickListener editButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get all necessary GUI components
            TableRow buttonTableRow = (TableRow) v.getParent();
            Button searchButton =
                    (Button) buttonTableRow.findViewById(R.id.newTagButton);
            String tag = searchButton.getText().toString();
            // set EditTexts to match the chosen tag and query
            tagEditText.setText(tag);
            queryEditText.setText(savedSearches.getString(tag, null));
        } // end method onClick
    }; // end OnClickListener anonymous inner class
}
