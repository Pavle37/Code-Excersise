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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<DataObject> list = new ArrayList<>();

        //adds 30 elements to list for test purposes
        for(int i = 0; i <= 30; i++){
            DataObject tmp = new DataObject();
            tmp.setTitle(getResources().getString(R.string.app_name));
            tmp.setDescription(getResources().getString(R.string.lorem_ipsum));
            list.add(tmp);
        }

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_item_main, R.id.text1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);

                text1.setText(list.get(position).getTitle());
                text2.setText(list.get(position).getDescription());
                return view;
            }
        };

        ListView lvItems = (ListView) findViewById(R.id.lvMain);
        lvItems.setAdapter(adapter);

    }
}
