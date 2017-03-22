package com.ibm.us.googlefittoolset;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;

import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;

import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.fitness.result.DataReadResult;

import com.google.android.gms.fitness.request.DataReadRequest;

import com.google.android.gms.fitness.result.SessionReadResult;
import com.ibm.us.googlefittoolset.model.HealthData;
import com.ibm.us.googlefittoolset.model.HealthDataValue;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "GoogleFitToolSet";

    private GoogleApiClient mClient = null;

    TextView infoTextView;
    TextView listInfoTextView;

    // List View for displaying Google Fit data
    ListView listView;
    ArrayAdapter<String> listAdapter;
    int defaultResourceID = android.R.layout.simple_list_item_1;

    String[] strListView = {"Weight", "Steps"};
    ArrayList<String> curListContent = null;

    // menu / weight / step
    String curContent = "menu";


    // if database loading is finished
    boolean isDatabaseLoaded = false;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient appIndexClient;

    //Realm
    private Realm realm;
    private RealmConfiguration realmConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoTextView = (TextView) findViewById(R.id.tv_main_info);
        infoTextView.setText("> Welcome to Google Toolset! \n");
        infoTextView.setMovementMethod(new ScrollingMovementMethod());

        listInfoTextView = (TextView) findViewById(R.id.tv_list_info);
        listInfoTextView.setText("Data types:");

        curListContent = new ArrayList<String>(Arrays.asList(strListView));

        listAdapter = new ArrayAdapter<String>(this, defaultResourceID, curListContent);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(this);

        /**
         * Realm
         */
        // Open the Realm for the UI thread.
        realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();

        Log.e(TAG, realm.toString());

        buildFitnessClient();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        appIndexClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // Create a message handling object as an anonymous class.
    public void onItemClick(AdapterView parent, View v, int position, long id) {

        if(!isDatabaseLoaded)
            return;

        if(curContent.equals("menu")){
            if(position==0){
                // show weight list
                RealmResults<HealthDataValue> healthDataValues = realm.where(HealthDataValue.class)
                                                                    .equalTo("label", "weight").findAll();
                String str = "<BACK";
                listAdapter.clear();
                listAdapter.add(str);

                for(HealthDataValue result : healthDataValues){
                    str = "Date: " + result.healthObject.date.toString() + "\n"
                            + "Steps: " + result.value;

                    listAdapter.add(str);
                }
                curContent = "weight";
                listInfoTextView.setText("Weight data: ");

                listAdapter.notifyDataSetChanged();
            }
            else if(position==1){
                // show step list
                RealmResults<HealthDataValue> healthDataValues = realm.where(HealthDataValue.class)
                        .equalTo("label", "step").findAll();
                String str = "<BACK";
                listAdapter.clear();
                listAdapter.add(str);

                for(HealthDataValue result : healthDataValues){
                    str = "Date: " + result.healthObject.date.toString() + "\n"
                            + "Weight (kg): " + result.value;

                    listAdapter.add(str);
                }
                curContent = "step";
                listInfoTextView.setText("Step data: ");

                listAdapter.notifyDataSetChanged();
            }
        }
        else if(curContent.equals("weight") || curContent.equals("step")){
            if(position==0){
                //back to main menu
                listAdapter.clear();
                for (String str : strListView)
                    listAdapter.add(str);
                curContent = "menu";
                listInfoTextView.setText("Data types: ");

                listAdapter.notifyDataSetChanged();
            }
        }

    }


    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or
     * having multiple accounts on the device and needing to specify which account to use, etc.
     * More details on requesting scopes:
     * https://developers.google.com/fit/android/authorization#scopes
     */
    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                // ADD API and SCOPEs HERE
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.SESSIONS_API)
                // require permissions for for all 4 scopes
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_NUTRITION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                infoTextView.append("> App connected to Google Fit \n");

                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Look at some data!!

                                // Subscribe to some data sources!
                                // More data types:
                                // https://developers.google.com/android/reference/com/google/android/gms/fitness/data/DataType
                                subscribe(DataType.TYPE_WEIGHT);
                                subscribe(DataType.TYPE_CALORIES_EXPENDED);
                                subscribe(DataType.TYPE_ACTIVITY_SAMPLE);
                                subscribe(DataType.TYPE_STEP_COUNT_CADENCE);
                                subscribe(DataType.TYPE_STEP_COUNT_DELTA);

                                //Only available for Android 6.0
                                //requires to add scope: BODY_SENSORS
                                //subscribe(DataType.TYPE_HEART_RATE_BPM);

                                onGoogleFitConnected();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    infoTextView.append("> Connection lost.  Cause: Network Lost. \n");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                    infoTextView.append("> Connection lost.  Reason: Service Disconnected \n");
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Google Play services connection failed. Cause: " +
                                result.toString());
                        infoTextView.append("> Google Play services connection failed. Cause: " +
                                result.toString() + "\n");
                    }
                })
                .build();
    }

    /**
     * Pulling off data from Google Fit
     * and update local Realm DB
     */
    private void onGoogleFitConnected(){
        // Show the list of subscriptions
        dumpSubscriptionsList();

        /**
         * Read the STEP COUNT data from Google Fit
         */
        DataReadRequest readRequest_step = queryFitnessData(DataType.TYPE_STEP_COUNT_DELTA);
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        Fitness.HistoryApi.readData(mClient, readRequest_step)
                .setResultCallback(new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult dataReadResult) {
                        // For the sake of the sample, we'll print the data so we can see what we just added.
                        // In general, logging fitness information should be avoided for privacy reasons.

                        //printData(dataReadResult);

                        // update Realm with the result
                        for (DataSet dataSet : dataReadResult.getDataSets()) {
                            saveDataSetInRealm(dataSet);
                        }
                    }
                });


        /**
         * Read the WEIGHT data from Google Fit
         */
        DataReadRequest readRequest_weight = queryFitnessData(DataType.TYPE_WEIGHT);
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        Fitness.HistoryApi.readData(mClient, readRequest_weight)
                .setResultCallback(new ResultCallback<DataReadResult>() {
                    @Override
                    public void onResult(DataReadResult dataReadResult) {
                        // For the sake of the sample, we'll print the data so we can see what we just added.
                        // In general, logging fitness information should be avoided for privacy reasons.

                        //printData(dataReadResult);

                        // update Realm with the result
                        for (DataSet dataSet : dataReadResult.getDataSets()) {
                            saveDataSetInRealm(dataSet);
                        }
                    }
                });


        /**
         * read fitness data in sessions if needed
         */
        /*SessionReadRequest sessionReadRequest = readFitnessSession();
        // [START read_session]
        // Invoke the Session API to fetch the session and await the result of
        // the read request.
        Fitness.SessionsApi.readSession(mClient, sessionReadRequest)
                .setResultCallback(new ResultCallback<SessionReadResult>() {
                    @Override
                    public void onResult(@NonNull SessionReadResult sessionReadResult) {
                        printSession(sessionReadResult);
                    }
                });

        // [END read_session]*/

        isDatabaseLoaded = true;
    }

    /**
     * Subscribe to an available {@link DataType}. Subscriptions can exist across application
     * instances (so data is recorded even after the application closes down).  When creating
     * a new subscription, it may already exist from a previous invocation of this app.  If
     * the subscription already exists, the method is a no-op.  However, you can check this with
     * a special success code.
     * More details about subscribable data types:
     * https://developers.google.com/android/reference/com/google/android/gms/fitness/data/DataType
     */
    public void subscribe(DataType dataType) {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        // [START subscribe_to_datatype]
        Fitness.RecordingApi.subscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing.");
                            Log.i(TAG, status.toString() + " / " + status.getStatusMessage());
                        }
                    }
                });
        // [END subscribe_to_datatype]
    }

    /**
     * Fetch a list of all active subscriptions and log it. Since the logger for this sample
     * also prints to the screen, we can see what is happening in this way.
     */
    private void dumpSubscriptionsList() {
        // [START list_current_subscriptions]
        Fitness.RecordingApi.listSubscriptions(mClient)
                // Create the callback to retrieve the list of subscriptions asynchronously.
                .setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                        for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                            DataType dt = sc.getDataType();
                            Log.i(TAG, "Active subscription for data type: " + dt.getName());
                            infoTextView.append("> Active subscription for data type: " + dt.getName() + "\n");
                        }
                    }
                });
        // [END list_current_subscriptions]
    }

    /**
     * Cancel the ACTIVITY_SAMPLE subscription by calling unsubscribe on that {@link DataType}.
     */
    private void cancelSubscription() {
        final String dataTypeStr = DataType.TYPE_ACTIVITY_SAMPLE.toString();
        Log.i(TAG, "Unsubscribing from data type: " + dataTypeStr);

        // Invoke the Recording API to unsubscribe from the data type and specify a callback that
        // will check the result.
        // [START unsubscribe_from_datatype]
        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Successfully unsubscribed for data type: " + dataTypeStr);
                        } else {
                            // Subscription not removed
                            Log.i(TAG, "Failed to unsubscribe for data type: " + dataTypeStr);
                        }
                    }
                });
        // [END unsubscribe_from_datatype]
    }

    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    public static DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 3 MONTH before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -3);    //change this if a different range is desired
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    public static DataReadRequest queryFitnessData(DataType dataType) {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -3);    //change this if a different range is desired
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(dataType)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    /**
     * Return a {@link SessionReadRequest} for all speed data in the past week.
     */
    private SessionReadRequest readFitnessSession() {
        Log.i(TAG, "Reading History API results for session");
        // [START build_read_session_request]
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .build();
        // [END build_read_session_request]

        return readRequest;
    }

    // ===================
    // REALM HELPERS
    // ===================

    private void saveDataSetInRealm(DataSet dataSet){
        Log.i(TAG, "Updating Data Set in Realm: " + dataSet.getDataType().getName());

        for (DataPoint dp : dataSet.getDataPoints()) {
            saveDataPointInRealm(dp);
        }
    }

    private void saveDataPointInRealm(DataPoint dataPoint) {

        long timestamp = dataPoint.getEndTime(TimeUnit.MILLISECONDS);
        String origin = dataPoint.getOriginalDataSource().getName();
        String source = dataPoint.getDataSource().getName();
        String dataType = dataPoint.getDataType().getName();
        String label = null;
        float value = 0;

        // get the value associated with the first field
        Value dpValue = dataPoint.getValue(dataPoint.getDataType().getFields().get(0));

        if(dataType.equals("com.google.step_count.delta")) {
            label = "step";
            value = dpValue.asInt();
        }
        else if(dataType.equals("com.google.weight")) {
            label = "weight";
            value = dpValue.asFloat();
        }
        else{
            Log.i(TAG, "Unexpected Data Type:" + dataType);
            return;
        }

        String id = label + Long.toString(timestamp);

        HealthData.storeData(id, timestamp, origin, source, label, value);
    }
}
