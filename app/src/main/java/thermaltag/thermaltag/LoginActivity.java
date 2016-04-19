package thermaltag.thermaltag;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LoginActivity extends AppCompatActivity {
    private static final String LOGTAG = "ThermalTagLog";
    private EditText username, password;
    private String enteredUsername, enteredPassword;
    // TODO: Add AWS link for thermalTag here... - currently references old server
    private final String serverUrl = "http://thermaltag.netau.net/androidwebservice/loginVerification.php?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = (EditText) findViewById(R.id.username_field);
        password = (EditText) findViewById(R.id.password_field);
        ImageButton loginButton = (ImageButton) findViewById(R.id.loginButton);
        Log.i(LOGTAG, "And we are off and running");

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                enteredUsername = username.getText().toString();
                enteredPassword = password.getText().toString();

                if(enteredUsername.equals("") || enteredPassword.equals("")){
                    Snackbar.make(v, "Username or password must be filled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    // request authentication with remote server
                    // Un-comment this code to test without server connection
//                      Toast.makeText(LoginActivity.this, "You Logged in!", Toast.LENGTH_LONG).show();
//                      Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
//                      startActivity(intent);

                    RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                    String url = serverUrl + "username=" + enteredUsername + "&password=" + enteredPassword;
                    StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.startsWith("Sucess")) {
                                Intent i = new Intent(LoginActivity.this, CameraActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("username", enteredUsername);
                                i.putExtras(bundle);
                                startActivity(i);
                            }  // end if
                            else {
                                Snackbar.make(v, "Invalid Username or password ", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            } // end if
                        } // end onResponse
                    }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(),"Network "+error,Toast.LENGTH_LONG).show();
                            }
                        });
                        queue.add(strReq);
                    }
                }
            });
        }
    }
