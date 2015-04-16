package com.hanshenrik.dbankclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private static final int SETTINGS_REQUEST = 1;

    private Button getBalanceButton;
    private Button withdrawButton;
    private Button depositButton;
    private TextView balanceText;
    private EditText amountInput;
    private ListView accountNumbersListView;

    private ArrayList<String> accountNumbers;
    private double amount;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("1", "");
        if (resultCode == RESULT_CANCELED) { // user clicked back button in SettingsActivity
            Log.d("2", "");
            if (requestCode == SETTINGS_REQUEST) {
                Log.d("3", "");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                Map<String, ?> prefsMap = prefs.getAll();
                for (String key : prefsMap.keySet()) {
                    Log.d("MAP", key + " | " + prefsMap.get(key).toString());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountNumbers = new ArrayList<>();
        // DEV
        accountNumbers.add("1234.56.78910");
        accountNumbers.add("9876.54.32100");
        accountNumbers.add("0000.00.00000");

        getBalanceButton = (Button) findViewById(R.id.getBalanceButton);
        withdrawButton = (Button) findViewById(R.id.withdrawButton);
        depositButton = (Button) findViewById(R.id.depositButton);
        balanceText = (TextView) findViewById(R.id.balanceTextView);
        amountInput = (EditText) findViewById(R.id.amountInput);
        accountNumbersListView = (ListView) findViewById(R.id.accountNumbersListView);

        ArrayAdapter accountNumbersListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, accountNumbers);
        accountNumbersListView.setAdapter(accountNumbersListAdapter);

        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balanceText.setText("getting balance...");
                // setup connection
                // query balance
                // on some callback
                balanceText.setText(getString(R.string.currency_prefix) + "100.00");
            }
        });

        depositButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = Double.parseDouble(amountInput.getText().toString());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Deposit?")
                        .setMessage("Are you sure you want to deposit " + amount + " into account "
                                + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // setup connection
                                // do stuff
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = Double.parseDouble(amountInput.getText().toString());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Withdraw?")
                        .setMessage("Are you sure you want to withdraw " + amount + " from account "
                                + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // setup connection
                                // do stuff
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
