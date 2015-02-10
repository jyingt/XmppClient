package com.john.ying.xmppclient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;

public class LoginActivity extends Activity {
    private static final int TIMEOUT_PACKET_REPLY = 30000;

    private XMPPTCPConnection connection;
    private ChatManager chatManager;
    private Chat chat;

    private EditText usernameText;
    private EditText serverText;
    private EditText passwordText;
    private Button loginButton;
    private TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameText = (EditText) findViewById(R.id.username_text);
        serverText = (EditText) findViewById(R.id.server_text);
        passwordText = (EditText) findViewById(R.id.password_text);
        loginButton = (Button) findViewById(R.id.login_button);
        logText = (TextView) findViewById(R.id.log_text);
    }

    public void onLoginButtonClick(View v) {
        if (v.equals(loginButton)) {
            if (connection == null || !connection.isConnected() || !connection.isAuthenticated()) {
                connection = new XMPPTCPConnection(
                        usernameText.getText().toString(),
                        passwordText.getText().toString(),
                        serverText.getText().toString()
                );
                connection.setPacketReplyTimeout(TIMEOUT_PACKET_REPLY);

                Toast.makeText(this, "Connecting to server...", Toast.LENGTH_SHORT).show();
                new ConnectTask().execute(connection);

                Toast.makeText(this, "Authorizing user...", Toast.LENGTH_SHORT).show();
                new LoginTask().execute(connection);
            } else {
                try {
                    chat.sendMessage("Hey Jonathan Yang!");
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                logText.post(new Runnable() {
                    @Override
                    public void run() {
                        logText.setText(logText.getText().toString() + "\nSent message to Jonathan Yang!");
                    }
                });
            }
        }
    }

    private class ConnectTask extends AsyncTask<AbstractXMPPConnection, Void, Boolean> {
        @Override
        protected Boolean doInBackground(AbstractXMPPConnection... connections) {
            AbstractXMPPConnection connection = connections[0];
            boolean isSuccess = false;
            try {
                connection.connect();
                isSuccess = true;
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            if (isSuccess) {
                logText.setText("Connected!");
            }
        }
    }

    private class LoginTask extends AsyncTask<AbstractXMPPConnection, Void, Boolean> {
        @Override
        protected Boolean doInBackground(AbstractXMPPConnection... connections) {
            AbstractXMPPConnection connection = connections[0];
            boolean isSuccess = false;
            try {
                connection.login();
                isSuccess = true;
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            if (isSuccess) {
                logText.setText(logText.getText().toString() + "\nLogged in!");
            }

            chatManager = ChatManager.getInstanceFor(connection);
            chat = chatManager.createChat("jyang@wtfismyip.com", new ChatMessageListener() {
                @Override
                public void processMessage(Chat chat, final Message message) {
                    logText.post(new Runnable() {
                        @Override
                        public void run() {
                            logText.setText(logText.getText().toString() + "\nReceived message: " + message.getBody());
                        }
                    });
                }
            });

            logText.post(new Runnable() {
                @Override
                public void run() {
                    logText.setText(logText.getText().toString() + "\nOpened chat with jyang@wtfismyip.com!");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        if (connection != null) {
            new DisconnectTask().execute(connection);
        }
        super.onStop();
    }

    private class DisconnectTask extends AsyncTask<AbstractXMPPConnection, Void, Boolean> {
        @Override
        protected Boolean doInBackground(AbstractXMPPConnection... connections) {
            AbstractXMPPConnection connection = connections[0];
            connection.disconnect();

            return !connection.isConnected();
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            if (isSuccess) {
                logText.post(new Runnable() {
                    @Override
                    public void run() {
                        logText.setText("Disconnected.");
                    }
                });
            }
        }
    }
}
