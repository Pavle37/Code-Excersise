/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package pavle.com.codingexcersise;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter mAdapter;
    private ImageView mRefresh;

    private static final String URL = "https://raw.githubusercontent.com/danieloskarsson/mobile-coding-exercise/master/items.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If we just launched the application, initialize the ArrayList
        if ( getList() == null) {
            setList(new ArrayList<DataObjectModel>());
        }

        initializeAdapter();

        ListView lvItems = (ListView) findViewById(R.id.lvMain);
        lvItems.setAdapter(mAdapter);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                intent.putExtra("Position",i);
                startActivity(intent);
            }
        });

        mRefresh = (ImageView) findViewById(R.id.ivRefresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation rotation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.refresh_anim);
                rotation.setRepeatCount(Animation.INFINITE);
                mRefresh.startAnimation(rotation);

                // Run animation for 300ms so that user can feel the input
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloadJson();
                    }
                },300);
            }
        });

        // Perform click at start so that user doesn't have to
        if(getList().size() == 0) {
            mRefresh.performClick();
        }
    }

    private void downloadJson() {
        TextView tvNoInternet = (TextView) findViewById(R.id.tvNoInternet);
        // If no data was downloaded, download JSON
        if(getList().size() == 0){
            // Check Internet connection, if there is none, display message
            if(hasInternet()) {
                tvNoInternet.setVisibility(View.GONE);
                new JsonTask().execute(URL);
            }
            else{
                mRefresh.clearAnimation();
                tvNoInternet.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initializeAdapter() {
        // Link the adapter with that list so when elements are added, we can refresh
        mAdapter = new ArrayAdapter(this, R.layout.list_item_main, R.id.text1, getList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);

                text1.setText(getList().get(position).getTitle());
                text2.setText(getList().get(position).getDescription());
                return view;
            }
        };
    }

    private void hideInternetLayout() {
        mRefresh.clearAnimation();
        RelativeLayout rlNoInternet = (RelativeLayout) findViewById(R.id.rlInternet);
        rlNoInternet.setVisibility(View.GONE);
    }

    private boolean hasInternet() {
        // get Connectivity Manager object to check connection
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        NetworkInfo info = conMan.getActiveNetworkInfo();
        if(info == null) return false; // If no network is available, return false
        return info.isConnected(); // Return status of the connection
    }

    public List<DataObjectModel> getList() {
        return ((MyApplication)getApplicationContext()).getDataObjects();
    }

    public void setList(List<DataObjectModel> list){
        ((MyApplication)getApplicationContext()).setDataObjects(list);
    }

    public class JsonTask extends AsyncTask<String, String, String>{


        @Override //Opens a connection and reads data
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);

                String data = readDataFromUrl(url);

                if(data != null) { // If some data was read, parse it
                    JSONArray jObjects = new JSONArray(data);
                    for (int i = 0; i < jObjects.length(); i++) {
                        JSONObject tmp = jObjects.getJSONObject(i);
                        addObjectToList(tmp);
                    }
                }
            } catch (MalformedURLException e) {
                Log.d("JsonTask:", "Problem with URL");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        //Reads data from JSONObject and creates new DataObjectModel with same info
        private void addObjectToList(JSONObject tmp) {
            DataObjectModel object = new DataObjectModel();
            try {
                object.setTitle(tmp.getString("title"));
                object.setDescription(tmp.getString("description"));
                object.setImgUrl(tmp.getString("image"));
                getList().add(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private String readDataFromUrl(URL url) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try{
                //open a connection
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                //set up a reader
                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                //read line by line and put it in a buffer
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                return buffer.toString();
            }
            catch (IOException e) {
                    Log.d("JsonTask:","Connection problem");
            }
            finally { //If error occurred, make sure to close the connections
                if(connection != null) connection.disconnect();
                if(reader != null) try {
                    reader.close();
                    }
                catch (IOException e) {
                        Log.d("JsonTask","Cannot close BufferedReader");
                }
            }
            return null; //If there was an error, return null
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mAdapter.notifyDataSetChanged();
            hideInternetLayout();
        }
    }
}
