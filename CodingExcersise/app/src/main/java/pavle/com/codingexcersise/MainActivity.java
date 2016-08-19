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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

    private List<DataObjectModel> mDataObjects = new ArrayList<>();
    private ArrayAdapter mAdapter;

    private static final String URL = "https://raw.githubusercontent.com/danieloskarsson/mobile-coding-exercise/master/items.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<DataObjectModel> list = new ArrayList<>();

        //adds 30 elements to list for test purposes
        for(int i = 0; i <= 30; i++){
            DataObjectModel tmp = new DataObjectModel();
            tmp.setTitle(getResources().getString(R.string.app_name));
            tmp.setDescription(getResources().getString(R.string.lorem_ipsum));
            list.add(tmp);
        }

        mAdapter = new ArrayAdapter(this, R.layout.list_item_main, R.id.text1, mDataObjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);

                text1.setText(mDataObjects.get(position).getTitle());
                text2.setText(mDataObjects.get(position).getDescription());
                return view;
            }
        };

        ListView lvItems = (ListView) findViewById(R.id.lvMain);
        lvItems.setAdapter(mAdapter);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,ViewActivity.class);
                startActivity(intent);
            }
        });

        new JsonTask().execute(URL);
    }

    public class JsonTask extends AsyncTask<String, String, String>{


        @Override //Opens a connection and reads data
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);

                String data = readDataFromUrl(url);

                JSONArray jObjects = new JSONArray(data);
                for(int i = 0; i < jObjects.length(); i++ ){
                    JSONObject tmp = jObjects.getJSONObject(i);
                    addObjectToList(tmp);
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
                mDataObjects.add(object);
                refreshListVew();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //On UI Thread, notify the adapter about data changes
        private void refreshListVew() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
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
    }
}
