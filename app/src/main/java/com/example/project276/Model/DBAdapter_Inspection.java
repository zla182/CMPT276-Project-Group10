// ------------------------------------ DBADapter.java ---------------------------------------------

// TODO: Change the package to match your project.
package com.example.project276.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


// TO USE:
// Change the package (at top) to match your project.
// Search for "TODO", and make the appropriate changes.
public class DBAdapter_Inspection {

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapterInspection";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    public static final String KEY_TRACKING_NUMBER = "trackingnumber";
    public static final String KEY_DATE = "date";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NUMCRITICAL = "numcritical";
    public static final String KEY_NUMNONCRITICAL = "numnoncritical";
    public static final String KEY_VIOLATIONS = "violations";
    public static final String KEY_HAZARD_RATING = "hazardrating";


    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_TRACKING_NUMBER = 1;
    public static final int COL_DATE = 2;
    public static final int COL_TYPE = 3;
    public static final int COL_NUMCRITICAL = 4;
    public static final int COL_NUMNONCRITICAL = 5;
    public static final int COL_VIOLATIONS = 6;
    public static final int COL_HAZARDRATING = 7;



    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TRACKING_NUMBER, KEY_DATE, KEY_TYPE, KEY_NUMCRITICAL, KEY_NUMNONCRITICAL, KEY_VIOLATIONS, KEY_HAZARD_RATING};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "DbInspections";
    public static final String DATABASE_TABLE = "inspectionTable";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "

                    /*
                     * CHANGE 2:
                     */
                    // TODO: Place your fields here!
                    // + KEY_{...} + " {type} not null"
                    //	- Key is the column name you created above.
                    //	- {type} is one of: text, integer, real, blob
                    //		(http://www.sqlite.org/datatype3.html)
                    //  - "not null" means it is a required field (must be given a value).
                    // NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
                    + KEY_TRACKING_NUMBER + " text not null, "
                    + KEY_DATE + " string not null, "
                    + KEY_TYPE + " string not null, "
                    + KEY_NUMCRITICAL + " int not null, "
                    + KEY_NUMNONCRITICAL + " int not null, "
                    + KEY_VIOLATIONS + " string not null, "
                    + KEY_HAZARD_RATING + " string not null "


                    // Rest  of creation:
                    + ");";

    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper;
    private static SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter_Inspection(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBAdapter_Inspection open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRow(String trackingnum, String date, String type, int numcritical, int numnoncritical, String violations, String hazardrating) {
        /*
         * CHANGE 3:
         */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TRACKING_NUMBER, trackingnum);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_NUMCRITICAL, numcritical);
        initialValues.put(KEY_NUMNONCRITICAL, numnoncritical);
        initialValues.put(KEY_VIOLATIONS, violations);
        initialValues.put(KEY_HAZARD_RATING, hazardrating);

        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public static Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String trackingnum, String date, String type, int numcritical, int numnoncritical, String violations, String hazardrating) {
        String where = KEY_ROWID + "=" + rowId;

        /*
         * CHANGE 4:
         *
         */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TRACKING_NUMBER, trackingnum);
        newValues.put(KEY_DATE, date);
        newValues.put(KEY_TYPE, type);
        newValues.put(KEY_NUMCRITICAL, numcritical);
        newValues.put(KEY_NUMNONCRITICAL, numnoncritical);
        newValues.put(KEY_VIOLATIONS, violations);
        newValues.put(KEY_HAZARD_RATING, hazardrating);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }

    //search the database with the given query
    public Cursor getInspectionMatch(String column, String query){
        open();
        Cursor cursor = myDBHelper.getReadableDatabase().rawQuery("SELECT * FROM "+DATABASE_TABLE+ " WHERE "+column+" LIKE '%"+query+"%'",null);
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        close();
        return cursor;
    }

    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }
}
