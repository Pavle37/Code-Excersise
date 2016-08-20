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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ViewActivity extends AppCompatActivity {

    private ImageView mPicture;
    private DataObjectModel mClickedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        // Get the position passed via Intent and load that Item
        int id = getIntent().getIntExtra("Id",0);
        DataObjectSqliteHelper db = new DataObjectSqliteHelper(this);
        mClickedItem = db.getObject(id);

        // Set the title and description from data
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        TextView tvDescription = (TextView) findViewById(R.id.tvDescription);

        tvTitle.setText(mClickedItem.getTitle());
        tvDescription.setText(mClickedItem.getDescription());

        mPicture = (ImageView) findViewById(R.id.ivPicture);

        // If the picture has been previously loaded, no need to load it again
        if(mClickedItem.getBitmap() != null){
            mPicture.setImageBitmap(mClickedItem.getBitmap());
        }
        else{// Load image from the Internet
            new ImageLoader().execute(mClickedItem.getImgUrl());
        }
    }

    /** Loads Image from URL and saves it into mClickedItem **/
    public class ImageLoader extends AsyncTask<String, String, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            Bitmap bmp = null;
            try {
                url = new URL(strings[0]);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                Log.d("LoadImage","Bad image URL");
                return null;
            } catch (IOException e) {
                Log.d("LoadImage","Connection error");
                return null;
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                mPicture.setImageBitmap(bitmap);
                mClickedItem.setBitmap(bitmap);
                DataObjectSqliteHelper db = new DataObjectSqliteHelper(ViewActivity.this);
                db.updateObject(mClickedItem);
            }
            // There has been a problem in loading, show toast
            else Toast.makeText(getApplicationContext(),"Internet connection needed to load image",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
