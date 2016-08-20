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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that reads and writes DataObjects into a SQLite database
 */
public class DataObjectSqliteHelper extends SQLiteOpenHelper{

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "DataObjectBase";

    // Table name
    private static final String TABLE_NAME = "objects";

    // Column names
    private static final String ID_KEY = "id";
    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String IMAGE_URL_KEY = "imageUrl";
    private static final String IMAGE_RESOURCE_KEY = "imageResource";


    private static final String[] COLUMNS = {ID_KEY,TITLE_KEY,DESCRIPTION_KEY,IMAGE_URL_KEY,IMAGE_RESOURCE_KEY};
    private static final String IMAGE_PREFIX = "codingexcersize";


    public DataObjectSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ( " +
                ID_KEY +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TITLE_KEY +" TEXT, "+
                DESCRIPTION_KEY +" TEXT, " +
                IMAGE_URL_KEY +" TEXT, " +
                IMAGE_RESOURCE_KEY +" TEXT )";

        // create table
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older objects table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS objects");

        // create fresh table
        this.onCreate(sqLiteDatabase);
    }
    public void addObject(DataObjectModel object){

        // 1. Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. Get content values
        ContentValues values = getValues(object);

        // 3. insert key/value -> keys = column names/ values = column values
        db.insert(TABLE_NAME, null,values);

        // 4. close
        db.close();
    }

    // Returns absolute path to the file saved in memory
    private String saveBitmap(Bitmap bitmap,String id) {
        if(bitmap == null) return "";
        String path = Environment.getExternalStorageDirectory().toString();
        FileOutputStream out = null;
        File file = new File(path, IMAGE_PREFIX+id+".jpg"); // the File to save to
        try {

            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DataObjectModel getObject(int id){
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_NAME, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. Create and return the object
        return createObjectFromCursor(cursor);
    }

    // Loads a bitmap from absolute path
    private Bitmap loadBitmapFromStorage(String path) {
        if(path == "") return null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path,bmOptions);
        if(bitmap != null) return Bitmap.createBitmap(bitmap);
        return null;
    }

    public List<DataObjectModel> getObjectList() {
        List<DataObjectModel> objects= new ArrayList<>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_NAME;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build object and add it to list
        DataObjectModel object = null;
        if (cursor.moveToFirst()) {
            do {
                // Add object to the list
                objects.add(createObjectFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        // return list
        return objects;
    }

    // Creates object and returns it
    private DataObjectModel createObjectFromCursor(Cursor cursor) {
        if(cursor == null) return null;
        DataObjectModel object = new DataObjectModel();
        object.setId(Integer.parseInt(cursor.getString(0)));
        object.setTitle(cursor.getString(1));
        object.setDescription(cursor.getString(2));
        object.setImgUrl(cursor.getString(3));
        object.setBitmap(loadBitmapFromStorage(cursor.getString(4)));
        return object;
    }

    public int updateObject(DataObjectModel object) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = getValues(object);

        // 3. updating row
        int i = db.update(TABLE_NAME, //table
                values, // column/value
                ID_KEY+" = ?", // selections
                new String[] { String.valueOf(object.getId()) }); //selection args

        // 4. close
        db.close();

        return i;
    }

    public ContentValues getValues(DataObjectModel object) {
        // Create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(TITLE_KEY, object.getTitle());
        values.put(DESCRIPTION_KEY, object.getDescription());
        values.put(IMAGE_URL_KEY, object.getImgUrl());

        values.put(IMAGE_RESOURCE_KEY, saveBitmap(
                object.getBitmap(),String.valueOf(object.getId())));
        return values;
    }

    public void deleteObject(DataObjectModel object) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_NAME, //table name
                ID_KEY+" = ?",  // selections
                new String[] { String.valueOf(object.getId()) }); //selections args

        // 3. close
        db.close();
    }
}
