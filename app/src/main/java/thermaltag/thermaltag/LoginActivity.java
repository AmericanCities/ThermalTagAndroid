package thermaltag.thermaltag;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {


    private static final String LOGTAG = "ThermalTagLog";
    private EditText username, password;
    private String enteredUsername, enteredPassword;
    private final String serverUrl = "http://10.0.2.2/androidLogin/index.php";

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
            public void onClick(View v) {
                enteredUsername = username.getText().toString();
                enteredPassword = password.getText().toString();

                if(enteredUsername.equals("") || enteredPassword.equals("")){
                    Toast.makeText(LoginActivity.this, "Username or password must be filled", Toast.LENGTH_LONG).show();
                    return;

                }
                // request authentication with remote server4
                AsyncDataClass asyncRequestObject = new AsyncDataClass();
                asyncRequestObject.execute(serverUrl, enteredUsername, enteredPassword);
            }
        });

    }

    private class AsyncDataClass extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String uri = params[0];

            try{
//              http://developer.android.com/reference/java/net/HttpURLConnection.html
//              http://easyway2in.blogspot.com/2015/07/android-mysql-database-connect.html

                URL url = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                StringBuilder builder = new StringBuilder();

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);


                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data = URLEncoder.encode("login_name", "UTF-8")+"="+URLEncoder.encode(enteredUsername,"UTF-8")+"&"+
                        URLEncoder.encode("login_pass","UTF-8")+"="+URLEncoder.encode(enteredPassword,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine())!=null)
                {
                    builder.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                urlConnection.disconnect();
                return builder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }
        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            Log.i(LOGTAG, "Resulted Value: " + result);

            if(result.equals("") || result == null){
                Toast.makeText(LoginActivity.this, "Server connection failed", Toast.LENGTH_LONG).show();
                return;
            }

            int jsonResult = returnParsedJsonObject(result);

            if(jsonResult == 0){
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                return;

            }

            if(jsonResult == 1){
                  Toast.makeText(LoginActivity.this, "You Logged in!", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
//                intentestt.putExtra("USERNAME", enteredUsername);
//                intent.putExtra("MESSAGE", "You have been successfully login");
//                startActivity(intent);

            }
        }

    }


    private int returnParsedJsonObject(String result){
        JSONObject resultObject = null;
        int returnedResult = 0;
        try {
            resultObject = new JSONObject(result);
            returnedResult = resultObject.getInt("success");
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return returnedResult;
    }
}





