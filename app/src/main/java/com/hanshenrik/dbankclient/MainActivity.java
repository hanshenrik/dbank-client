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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    private static final int SETTINGS_REQUEST = 1;
    private static final int GET_BALANCE_OPERATION = 1;
    private static final int DEPOSIT_OPERATION = 2;
    private static final int WITHDRAW_OPERATION = 3;
    private static final int TRANSFER_OPERATION = 4;
    public static final String ACCOUNT_NUMBERS_SET = "com.hanshenrik.dbankclient.account_numbers_list";
    private static final String SEP = ";";

    private Button getBalanceButton, withdrawButton, depositButton, addAccountButton, transferButton;
    private TextView balanceText;
    private EditText amountInput;
    private ListView accountNumbersListView;

    private ArrayList<String> accountNumbers;
    private String selectedAccount, toAccount;
    private double amount;
    private String ip, username, password;
    private int port;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) { // user clicked back button in SettingsActivity
            if (requestCode == SETTINGS_REQUEST) {
                updatePreferences();
            }
        }
    }

    private void updatePreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        ip = prefs.getString(getString(R.string.pref_key_ip), null);
        port = Integer.parseInt(prefs.getString(getString(R.string.pref_key_port), null));
        username = prefs.getString(getString(R.string.pref_key_username), null);
        password = prefs.getString(getString(R.string.pref_key_password), null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updatePreferences();

        accountNumbers = new ArrayList<>();
        toAccount = "-1";
        amount = -1;

        // Retrieve account numbers
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Set<String> accountNumbersSet = prefs.getStringSet(ACCOUNT_NUMBERS_SET, null);
        accountNumbers.addAll(accountNumbersSet);

        addAccountButton = (Button) findViewById(R.id.addAccountButton);
        getBalanceButton = (Button) findViewById(R.id.getBalanceButton);
        withdrawButton = (Button) findViewById(R.id.withdrawButton);
        depositButton = (Button) findViewById(R.id.depositButton);
        transferButton = (Button) findViewById(R.id.transferButton);
        balanceText = (TextView) findViewById(R.id.balanceTextView);
        amountInput = (EditText) findViewById(R.id.amountInput);
        accountNumbersListView = (ListView) findViewById(R.id.accountNumbersListView);

        // Set a title for the list of accounts
        TextView textView = new TextView(this);
        textView.setText("Your accounts");
        accountNumbersListView.addHeaderView(textView);

        final ArrayAdapter accountNumbersListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, accountNumbers);
        accountNumbersListView.setAdapter(accountNumbersListAdapter);

        accountNumbersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedAccount = parent.getItemAtPosition(position).toString();
            }
        });

        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Add account number")
                        .setView(input)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_input_add)
                        .show();
            }
        });

        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountNumbersListView.getItemAtPosition(accountNumbersListView.getCheckedItemPosition()) == null) {
                    Toast.makeText(getApplicationContext(), "Please select an account!", Toast.LENGTH_SHORT).show();
                    return;
                }
                balanceText.setText("getting balance...");
                String query =  username + SEP +
                                password + SEP +
                                selectedAccount + SEP +
                                GET_BALANCE_OPERATION + SEP +
                                amount + SEP +
                                toAccount;
                new QueryTask(balanceText).execute(query);
            }
        });

        depositButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Amount cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (accountNumbersListView.getItemAtPosition(accountNumbersListView.getCheckedItemPosition()) == null) {
                    Toast.makeText(getApplicationContext(), "Please select an account!", Toast.LENGTH_SHORT).show();
                    return;
                }
                amount = Double.parseDouble(amountInput.getText().toString());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Deposit?")
                        .setMessage("Are you sure you want to deposit " + amount + " into account "
                                + selectedAccount + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                balanceText.setText("depositing money...");
                                String query =  username + SEP +
                                        password + SEP +
                                        selectedAccount + SEP +
                                        DEPOSIT_OPERATION + SEP +
                                        amount + SEP +
                                        toAccount;
                                new QueryTask(balanceText).execute(query);
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
                if (selectedAccount == null) {
                    Toast.makeText(getApplicationContext(), "Please select an account!", Toast.LENGTH_SHORT).show();
                    return;
                }
                amount = Double.parseDouble(amountInput.getText().toString());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Withdraw?")
                        .setMessage("Are you sure you want to withdraw " + amount + " from account "
                                + selectedAccount + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                balanceText.setText("withdrawing money...");
                                String query =  username + SEP +
                                        password + SEP +
                                        selectedAccount + SEP +
                                        WITHDRAW_OPERATION + SEP +
                                        amount + SEP +
                                        toAccount;
                                new QueryTask(balanceText).execute(query);
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

        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amountInput.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Amount cannot be empty!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (selectedAccount == null) {
                    Toast.makeText(getApplicationContext(), "Please specify sending account!", Toast.LENGTH_SHORT).show();
                    return;
                }
                amount = Double.parseDouble(amountInput.getText().toString());

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Specify receiving account number:")
                        .setView(input)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                balanceText.setText("transferring money...");
                                toAccount = input.getText().toString().trim();
                                String query =  username + SEP +
                                        password + SEP +
                                        selectedAccount + SEP +
                                        TRANSFER_OPERATION + SEP +
                                        amount + SEP +
                                        toAccount;
                                new QueryTask(balanceText).execute(query);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_media_ff)
                        .show();
            }
        });
    }

    private class QueryTask extends AsyncTask<String, Void, String> {
        TextView responseView;

        public QueryTask(TextView responseView) {
            this.responseView = responseView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String query = params[0];

            String message;
            try {
                Socket socket = new Socket(ip, port);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Receive greeting from server
                message = reader.readLine();

                // Send the query
                message = query;
                writer.write(message, 0, message.length());
                writer.newLine();
                writer.flush();
                message = reader.readLine();

                writer.close();
                reader.close();
                socket.close();
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
