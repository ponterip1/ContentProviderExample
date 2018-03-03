package com.peterponterio.contentproviderexample;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    /*
        A content provider supplies data from one application to others on request. if you need to share
         data between applications, you need to use a content provider.

         The content Resolver provides a layer of abstraction between our app(the client) and the Content
         Provider. The Content Provider adds another layer of abstraction on top of the underlying data
         source. An application has a single ContentResolver that provides access to all the content
         providers that exist on the device

         the client will go to the contentresolver with a request for data and the content resolver will
         go to the appropriate content provider for the data and the content provider will get the data
         from the data source. A chef(client) going to the grocery store(contentresolver) for eggs(data)
         and the grocery store gets its eggs from a wholesaler(contentprovider) the wholesaler will get
         its eggs from the chicken farm(data source)
     */

    private ListView contactNames;
    private static final int REQUEST_CODE_READ_CONTACTS = 1;
//    private static boolean READ_CONTACTS_GRANTED = false;
    FloatingActionButton fab = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        contactNames = (ListView) findViewById(R.id.contact_names);


        /*
            Check to see if the app has already been granted the permission that it needs

            There is a self check permission method in the framework but that will only work for marshmallow
            and above. Both methods return the same results but the contextCompat method first checks to
            see if its running on Android prior to API 23. If it is, it just returns success because the
            new security model doesnt apply before API 23
         */
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        Log.d(TAG, "onCreate: checkSelfPermission " + hasReadContactPermission);


        /*
            if the result we got back from calling checkSelfPermissions was PERMISSION_GRANTED, then we
            have the permission we need and we can carry on. But we also need to know that we have the
            READ_CONTACTS permission before we try to access the contacts when the floating action button
            is clicked, so were storing 'true' in the READ_CONTACTS_GRANTED field

            If the permission wasnt granted then we need to request it by calling the ActivityCompat version
            of requestPermissions, which is for API 22 and below. We provide an array containing the names
            of the permissions that we're requesting, which in this case is only READ_CONTACTS. Final
            parameter is the request code which is sent the value defined by the variable REQUEST_CODE_READ_CONTACTS
         */

//        if (hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "onCreate: permission granted");
////            READ_CONTACTS_GRANTED = true;
//        } else {
//            Log.d(TAG, "onCreate: requesting permission");
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
//        }
        //simplified version of code above ^^
        if(hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab onClick: starts");

//                if (READ_CONTACTS_GRANTED) {
                  if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

                    String[] projection = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};


                    /*
                        we get a contentResolver from the activity using the getContentResolver method
                        we use the contentResolver to query for the data we want
                        ContentResolver is queried for data and returns a cursor

                        ContentResolver will execute a query against its data source and give us back a
                        cursor and then we can use the cursor by using a loop to loop through all the rows
                        in the cursor and then display the contact names

                        contentResolver.query extracts the authority from the URI and uses that to decide
                        which content provider it should send the query request to. It then gets a cursor
                        back from the content provider and returns the cursor to the calling code

                        Parameters passed to the query method:
                            -Content_Uri which identifies the data source that we want to get data from
                            -projection is a string array holding the names of the columns that we want to retrieve
                            -selection is the next parameter which is a string containing a filter to determine
                              which rows are returned(WHERE clause in sql statement. Its null so its asking to get
                              all rows returned
                            -Selection arguments parameter which is an array of values that will be used to
                              replace placeholders in the selection string. Since were not doing any filtering
                              in this particular case, were just passing null for the argument
                            -Sort order parameter which is a string containing the names of the fields you
                              want the data sorted by(ORDER BY clause in sql statement). were ordering by the
                              actual name of the contact DISPLAY_NAME_PRIMARY
                     */
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, projection,
                            null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);



                    //checks if cursor returns any data back
                    if (cursor != null) {
                        List<String> contacts = new ArrayList<String>();
                        while (cursor.moveToNext()) {
                            /*
                                No need to call the cursors moveToFirst method and then start using moveToNext
                                If the cursor hasnt already moved to the first record, then moveToNext behaves
                                the same as moveToFirst. It will position us to the first record
                             */
                            contacts.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
                        }

                        /*
                            close the cursor and create an adapter for the list view so our screen will be
                            updated
                         */
                        cursor.close();
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.contact_detail, R.id.name, contacts);
                        contactNames.setAdapter(adapter);
                    }

                } else {
                    //displays snack bar message when permission is denied
                    Snackbar.make(view, "This app cant display your Contacts records unless you...", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Grant Access", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Snackbar onClick: starts");
                                    /*
                                        After a permission is initially denied, when the fab is pressed,
                                        the snackbar will provide a link to ask for permissions once more.
                                        if permissions are denied a second time(checked the 'dont show me again')
                                        and the fab is pressed, the snackbar will provide a link to the settings page


                                        call shouldShowRequestPermissionRationale method and test to see
                                        if it returns true or false. If it returns true then we request
                                        permission. If it returns false we send the user to the settings
                                        page so they can manually turn permissions on
                                     */
                                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                                        Log.d(TAG, "Snackbar onClick: calling requestPermissions");
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
                                    } else {
                                        //the user has permanently denied permission, take them to settings
                                        Log.d(TAG, "Snackbar onClick: launching settings");

                                        /*
                                            create new intent and use startActivity to start the activity
                                            that has been described by the intent

                                            setAction method needs a string specifying the action to be
                                            performed. Here we want to launch the settings app and have it
                                            open at our application

                                            we use mainActivity.this because 'this' would refer to the onclick
                                            listener. so mainActivity.this would refer to the main activity

                                            Uri consists of a scheme such as HTTPS or file or in this case 'package'
                                            the scheme is followed by the ssp, or scheme specific part, which is just
                                            the package name, so we use the uri.fromParts method to build up the uri's
                                            so that we need to pass as data to the intent


                                         */
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                        Log.d(TAG, "Snackbar onClick: Intent uri is " + uri.toString());
                                        intent.setData(uri);
                                        MainActivity.this.startActivity(intent);

                                    }
                                    Log.d(TAG, "Snackbar onClick: ends");
                                }
                            }).show();
                }
                Log.d(TAG, "fab onClick: ends");
            }
        });
        Log.d(TAG, "onCreate: ends");
    }


    /*
        to find out whether the permission was granted or denied, we have to implement a callback method
        that will be called once the user has made the decision

        first parameter is request code. Second parameter is the permissions we requested and the third
        is the results of each permission. This allows us to check which permissions were accepted and which
        were denied, as the user can accept and deny permissions(ex: for camera app, can accept permission
        to use camera but deny permission to use location where camera was used)
     */



    //*** unused becaused its redundant to know whether permission was granted or not since the code is written to cope with both conditions ***
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Log.d(TAG, "onRequestPermissionsResult: starts");
//        //switch statement because some apps need several requests for different permissions. this app
//        //is simple and only needs one permission request code(permisison to access contacts)
//        switch (requestCode) {
//            case REQUEST_CODE_READ_CONTACTS: {
//                //if request is cancelled the result arrays are empty
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //permission was granted
//                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
////                    READ_CONTACTS_GRANTED = true;
//                } else {
//                    //permission denied. disable the functionality that depends on this permission
//                    Log.d(TAG, "onRequestPermissionsResult: permission refused");
//                }
//            }
//        }
//        Log.d(TAG, "onRequestPermissionsResult: ends");
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
