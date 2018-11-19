package com.example.ryan.roomr_ticketservice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DemoCommuncation extends AppCompatActivity {

    Button submit;
    Button takePicture;
    ImageView imageView;
    EditText description;
    SeekBar priority;
    TextView priorityDisplay;

    TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_communcation);
        takePicture = findViewById(R.id.btnTakePicture);
        submit = findViewById(R.id.btnSubmit);
        imageView = findViewById(R.id.imgImageView);
        description = findViewById(R.id.edtDescription);
        priority = findViewById(R.id.skbPriority);
        priorityDisplay = findViewById(R.id.txtPriorityDisplay);
        name = findViewById(R.id.txtName);
        submit.setOnClickListener(onSubmit);
        takePicture.setOnClickListener(onTakePicture);
        priority.setOnSeekBarChangeListener(onPriorityChange);

        priorityDisplay.setText("0");

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("NAME"));

    }






    private SeekBar.OnSeekBarChangeListener onPriorityChange= new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            seekBar.setMax(10);
            priorityDisplay.setText(Integer.toString(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };




    private View.OnClickListener onTakePicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            startActivityForResult(intent, 0);
        }
    };

    private View.OnClickListener onSubmit = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


            DemoCommuncation.NetworkHelperTask helper = new DemoCommuncation.NetworkHelperTask();
            helper.execute();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0){
            if (resultCode == RESULT_OK){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
            }
        }

    }

    private class NetworkHelperTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            //initialize url
            URL url;
            String urlString = "http://10.16.26.15:5000/";
            try {
                //create new url
                url = new URL(urlString);
                //open connection to url
                URLConnection connection = url.openConnection();
                //Make it an http connection
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                //get response code
                int responseCode = httpConnection.getResponseCode();
                //if response code == 200
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //make the request
                    onRequest(urlString);
                    //return because doInBackground returns a String
                    return "";
                    //if response is unsuccessful
                } else {

                    return "Can't get info: " + responseCode;
                }
            }

            catch (Exception ex) {

                Log.e("doInBackground", ex.toString());
                return "Can't retrieve info";
            }
        }
    }






    private void onRequest(String url) {

        final RequestQueue queue = Volley.newRequestQueue(this);

        final String json = createJSONObject().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url + "test", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(DemoCommuncation.this, response, Toast.LENGTH_SHORT).show();
                queue.stop();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DemoCommuncation.this, "Error", Toast.LENGTH_SHORT).show();
                queue.stop();
            }
        }){

            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(stringRequest);

    }

    private JSONObject createJSONObject(){
        JSONObject json = new JSONObject();

        try {
            json.put("Name", name.getText().toString());
            json.put("Photo", bitmapToString());
            json.put("Description", description.getText().toString());
            json.put("Priority", priorityDisplay.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private String bitmapToString(){
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream btmp = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, btmp);
        byte [] bytes = btmp.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


}
