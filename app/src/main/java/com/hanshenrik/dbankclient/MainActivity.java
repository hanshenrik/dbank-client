package com.hanshenrik.dbankclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    private static final int SETTINGS_REQUEST = 1;
    private static final int GET_BALANCE_OPERATION = 0;
    private static final int DEPOSIT_OPERATION = 1;
    private static final int WITHDRAW_OPERATION = 2;
    private static final int TRANSFER_OPERATION = 3;
    public static final String ACCOUNT_NUMBERS_SET = "com.hanshenrik.dbankclient.account_numbers_list";

    private Button getBalanceButton, withdrawButton, depositButton, addAccountButton;
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

        // Retrieve account numbers
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Set<String> accountNumbersSet = prefs.getStringSet(ACCOUNT_NUMBERS_SET, null);
        accountNumbers.addAll(accountNumbersSet);

        getBalanceButton = (Button) findViewById(R.id.getBalanceButton);
        withdrawButton = (Button) findViewById(R.id.withdrawButton);
        depositButton = (Button) findViewById(R.id.depositButton);
        addAccountButton = (Button) findViewById(R.id.addAccountButton);
        balanceText = (TextView) findViewById(R.id.balanceTextView);
        amountInput = (EditText) findViewById(R.id.amountInput);
        accountNumbersListView = (ListView) findViewById(R.id.accountNumbersListView);

        final ArrayAdapter accountNumbersListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, accountNumbers);
        accountNumbersListView.setAdapter(accountNumbersListAdapter);

        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                balanceText.setText("getting balance...");
                String query = "paul;paulx;4;" + GET_BALANCE_OPERATION + ";-1"; // get from Settings
                new QueryTask(balanceText).execute(GET_BALANCE_OPERATION, query);
            }
        });

        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Add account number");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newAccount = input.getText().toString();

                        // Update account numbers in SharedPreferences
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        Set<String> accountNumbersSet = new HashSet<>();
                        accountNumbers.add(newAccount);
                        accountNumbersSet.addAll(accountNumbers);
                        SharedPreferences.Editor prefsEditor = prefs.edit();
                        prefsEditor.putStringSet(ACCOUNT_NUMBERS_SET, accountNumbersSet);
                        prefsEditor.apply();

                        // Update the ListView as well
                        accountNumbersListAdapter.notifyDataSetChanged();

                        // OBS! this is only added locally, doesn't update server DB
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        depositButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Amount cannot be empty!", Toast.LENGTH_LONG).show();
                    return;
                }
                amount = Double.parseDouble(amountInput.getText().toString());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Deposit?")
                        .setMessage("Are you sure you want to deposit " + amount + " into account "
                                + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                balanceText.setText("depositing money...");
                                String query = "paul;paulx;4;" + DEPOSIT_OPERATION + ";" + amount; // get from Settings
                                new QueryTask(balanceText).execute(DEPOSIT_OPERATION, query);
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
                if (amountInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Amount cannot be empty!", Toast.LENGTH_LONG).show();
                    return;
                }
                amount = Double.parseDouble(amountInput.getText().toString());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Withdraw?")
                        .setMessage("Are you sure you want to withdraw " + amount + " from account "
                                + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                balanceText.setText("withdrawing money...");
                                String query = "paul;paulx;4;" + WITHDRAW_OPERATION + ";" + amount; // get from Settings
                                new QueryTask(balanceText).execute(WITHDRAW_OPERATION, query);
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

    private class QueryTask extends AsyncTask<Object, Void, String> {
        TextView responseView;

        public QueryTask(TextView responseView) {
            this.responseView = responseView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... params) {
            int operation = (int) params[0];
            String query = (String) params[1];

            String message = "just initializing for DEV"; // TODO: initialize empty or something
            try {
                Socket socket = new Socket("161.73.147.225", 5108);
                Log.d("SOC", "socket created");

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                switch (operation) {
                    case GET_BALANCE_OPERATION:
                        Log.d("SOC", "in GET_BALANCE_OPERATION");
                        message = reader.readLine();
                        Log.d("SOC", message);
                        message = query;
                        Log.d("SOC", "sending: " + message);
                        writer.write(message, 0, message.length());
                        writer.newLine();
                        writer.flush();
                        message = reader.readLine();
                        Log.d("SOC", message);

                        writer.close();
                        reader.close();
                        socket.close();
                        break;
                    case DEPOSIT_OPERATION:
                        Log.d("SOC", "in DEPOSIT_OPERATION");
                        break;
                    case WITHDRAW_OPERATION:
                        Log.d("SOC", "in WITHDRAW_OPERATION");
                        break;
                    case TRANSFER_OPERATION:
                        Log.d("SOC", "in TRANSFER_OPERATION");
                        break;
                    default:
                        Log.d("SOC", "in switch default");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                message = e.toString();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String response) {
            responseView.setText(response);
        }
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
