package com.samplereact.functionality;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.samplereact.MainActivity;
import com.samplereact.model.Model_StepCounter;
import com.samplereact.network.OkHttpNetworkServiceImplementation;

import org.json.JSONException;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.samplereact.functionality.ActivityFunctionality.activity_jsonform;
import static java.text.DateFormat.getTimeInstance;

@SuppressWarnings("deprecation")
public class StepCountsCalculation {

    private static final String TAG = "Steps class: =>";
    private int startTime, endTime;
    private GoogleApiClient mGoogleApiClient;
    private Model_StepCounter modelStepCounter;
    private ReactContext context;
    public static OkHttp3CookieHelper cookieHelper = new OkHttp3CookieHelper();
    private OkHttpNetworkServiceImplementation okHttpNetworkServiceImplementation = OkHttpNetworkServiceImplementation.getInstance();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final String random_data = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();
    public static String summary_id, steps_jsonform, steps_server_data;
    public static String formattedDate;
    private ArrayList<String> stepCount_list = new ArrayList<String>();
    private ArrayList<Model_StepCounter> totalStepsArraylist;
    private HashMap<String,ArrayList<String>> stepCount_hm = new    HashMap<String, ArrayList<String>>();

    private DateFormat dateFormat, timeFormat;
    private String returnValue = "";
    private String ac_activity_type, ac_start_date, ac_end_date, ac_device_type;
    private int ac_step, ac_active_dur;
    private float ac_dist, ac_calorie;
    private HashMap<String, ArrayList<String>> activity_hm = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> activity_list = new ArrayList<String>();
    private TimeZone ac_time_zone;
    private String timeZoneID, activity_server_data,activity_server_data1,activity_server_data2;
    public static String activity_jsonform;

    public StepCountsCalculation(GoogleApiClient mGoogleApiClient, ReactContext context) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.context = context;
    }

    private void send_steps_data_to_server() {
        // to generate a 10 digit random id
        randomIdGenerator(10);
        //Get today's date
        Date today_date = Calendar.getInstance().getTime();
        Log.e(TAG,"Current time => " + today_date);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = df.format(today_date);
        Log.e(TAG,"Current date => " + formattedDate);

        Log.e(TAG,"steps_Hashmap "+stepCount_hm);
        steps_server_data = stepCount_hm.toString().replaceAll("=", "");
        steps_jsonform = steps_server_data;
        steps_jsonform = steps_jsonform.substring(1, steps_jsonform.length() - 1);

        activity_server_data = activity_hm.toString().replaceAll("=", "");
        activity_jsonform = activity_server_data;
        activity_jsonform = activity_jsonform.substring(1, activity_jsonform.length() - 1);
        //Log.e(TAG,"Activity_JsonFrom "+activity_jsonform);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", "Jvbapple");
            jsonObject.put("password", "health2112");
            String info = okHttpNetworkServiceImplementation.loginRequest("https://app.jvbwellness.com/api/users/login/", jsonObject.toString(), new HashMap<String, String>());
            Log.e(TAG, " login API response: " + info);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("user", "402");
            jsonObject2.put("belong_to", formattedDate);
            jsonObject2.put("summary_id", summary_id);
            jsonObject2.put("data", steps_jsonform);

            String infor1 = okHttpNetworkServiceImplementation.protocolRequest("https://app.jvbwellness.com/apple/users/datasteps", jsonObject2.toString(), new HashMap<String, String>());
            Log.e(TAG, "steps API response: " + infor1);

            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("user", "402");
            jsonObject3.put("belong_to", formattedDate);
            jsonObject3.put("data", activity_jsonform);
            jsonObject3.put("summary_id", summary_id);
            String infor2 = okHttpNetworkServiceImplementation.protocolRequestActivity("https://app.jvbwellness.com/apple/users/dataactivities", jsonObject3.toString(), new HashMap<String, String>());
            Log.e(TAG, "activity data sending response: " + infor2);

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Records step data by requesting a subscription to background step data.
     */

    public void readStepountData() {
        try {
            Fitness.getHistoryClient(context, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(context)))
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    long total = dataSet.isEmpty()
                                            ? 0
                                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    dumpReadStepsData(dataSet);
//                                    ActivityFunctionality activityFunctionality = new ActivityFunctionality(context);
//                                    activityFunctionality.ActivityTypDetails();
                                    ActivityTypDetails();
                                    Log.d(TAG, "Total steps in given time: " + total);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "There was a problem getting the step count.", e);
                                }
                            });
        } catch(NullPointerException n) {
            n.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dumpReadStepsData(final DataSet dataSet) {

        startTime = 0;
        endTime = 24;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, startTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, endTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long endTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByActivitySegment(1, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        new CountSteps_Thread(readRequest, mGoogleApiClient, dataSet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private DataReadRequest queryStepCountData() {
        startTime = 0;
        endTime = 24;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, startTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, 0);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, endTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long endTime = cal.getTimeInMillis();

        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showReadStepDataResults(DataSet dataSet) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = getTimeInstance();

        /**
         * Current Full functional Code for step counts for the day per minutes.
         */
        int stepcount = 0;
        totalStepsArraylist = new ArrayList<>();
        //time format
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            for (DataPoint dp : dataSet.getDataPoints()) {
                for (Field field : dp.getDataType().getFields()) {
                    stepcount = dp.getValue(field).asInt();
                    if (field.getName().equals("steps")) {
                        String startTime = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
                        String endTime = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
                        modelStepCounter = new Model_StepCounter(startTime, endTime, stepcount);
                        totalStepsArraylist.add(modelStepCounter);

                        if (stepcount != 0) {
                           /* Log.d("History --->", "StartTime: " + modelStepCounter.getStartTime() + "\nEndTime: " + modelStepCounter.getEndTime() + "\nSteps covered: " + modelStepCounter.getStep());
                            Log.d("History --->", "\n");*/

                            stepCount_list.add("{\n"+"\""+"Start date"+"\""+" : "+"\""+simpleDateFormat.format(new Date(modelStepCounter.getStartTime()))+"\""+"\n");
                            stepCount_list.add("\""+"End date"+"\""+" : "+"\""+simpleDateFormat.format(new Date(modelStepCounter.getEndTime()))+"\""+"\n");
                            stepCount_list.add("\""+"steps"+"\""+" : "+modelStepCounter.getStep()+"\n"+"}\n");
                            stepCount_hm.put("",stepCount_list);

                        } else {

                            Log.d("History1", "StartTime:1 " + modelStepCounter.getStartTime() + "\nEndTime: " + modelStepCounter.getEndTime() + "\nSteps covered: " + "-");
                            Log.d("History1", "\n");
                        }
                    }
                }
//                Log.e(TAG,"1totalStepsArraylist-> "+totalStepsArraylist);
            }

        } catch (NullPointerException p) {
            Log.e(TAG, "null-pointer while getting package name: sleep");
            p.printStackTrace();
        }
    }

    private String randomIdGenerator(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( random_data.charAt( rnd.nextInt(random_data.length()) ) );
        Log.e(TAG,"random_10_digit_id : "+sb);
        summary_id = String.valueOf(sb);
        return sb.toString();
    }


    @SuppressLint("StaticFieldLeak")
    private class CountSteps_Thread extends AsyncTask<Void, Void, Void> {
        DataReadRequest readRequest;
        GoogleApiClient mClient;
        DataSet dataset;

        CountSteps_Thread(DataReadRequest dataReadRequest_, GoogleApiClient googleApiClient, DataSet dataSet) {
            this.readRequest = dataReadRequest_;
            this.mClient = googleApiClient;
            this.dataset = dataSet;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... voids) {
            // Begin by creating the query.
            DataReadRequest readRequest = queryStepCountData();
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);
            if (dataReadResult.getBuckets().size() > 0) {
                //Log.d("History", "Number of buckets/Active Time Data: " + dataReadResult.getBuckets().size());

                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        showReadStepDataResults(dataSet);
                    }
                }
            }
            send_steps_data_to_server();
            return null;
        }
    }

    /**
     * *************************** ACTIVITY PART ***************************************************
     */

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void ActivityTypDetails() {
        startTime = 0;
        endTime = 24;

        /**
         * Extra data for the time being
         * ===============================================================================================
         */
        //time format
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Date today_date = Calendar.getInstance().getTime();
        Log.e(TAG, "Current time => " + today_date);

        ac_time_zone = TimeZone.getDefault();
        timeZoneID = ac_time_zone.getID();
        // to generate a 10 digit random id
        randomIdGenerator(10);

        /**
         * ===============================================================================================
         */

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, startTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, endTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long endTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)  // step counts
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED) // calories
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY) //active duration
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)  // distnace
                .bucketByActivitySegment(1, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DataReadResponse dataReadResult) {
                        String returnValue = "", activityval = "";

                        if (dataReadResult.getBuckets().size() > 0) {
                            for (int i = 0; i < dataReadResult.getBuckets().size(); i++) {
                                activityval += "\n\n" + i + " ---new bucket-- activity: " + dataReadResult.getBuckets().get(i).getActivity() + "\n"
                                        + dataReadResult.getBuckets().get(i).getStartTime(TimeUnit.MILLISECONDS) + "~"
                                        + dataReadResult.getBuckets().get(i).getEndTime(TimeUnit.MILLISECONDS);

                                ac_activity_type = dataReadResult.getBuckets().get(i).getActivity();


                                for (int j = 0; j < dataReadResult.getBuckets().get(i).getDataSets().size(); j++) {
                                    returnValue += "\n-data set " + j
                                            + "-package: " + dataReadResult.getBuckets().get(i).getDataSets().get(j).getDataSource().getAppPackageName()
                                            + ", stream: " + dataReadResult.getBuckets().get(i).getDataSets().get(j).getDataSource().getStreamIdentifier();
                                    returnValue += handleDailyRecordInDataSet(dataReadResult.getBuckets().get(i).getDataSets().get(j));
                                }


                               /* if (ac_activity_type != null && ac_start_date != null && ac_end_date != null &&
                                        ac_device_type != null && ac_step != 0 && ac_active_dur != 0 && ac_dist != 0 && ac_calorie != 0) {*/
                                Log.e(TAG, "activity_type_data \n "
                                        + "activity_type: " + ac_activity_type + " "
                                        + "start_date: " + ac_start_date + " "
                                        + "end_date: " + ac_end_date + " "
                                        + "device_type: " + ac_device_type + " "
                                        + "steps: " + ac_step + " "
                                        + "active_duration: " + ac_active_dur + " "
                                        + "distnance: " + ac_dist + " "
                                        + "calorie: " + ac_calorie + " "
                                        + "time_zone: " + ac_time_zone);

                                activity_list.add("{\n" + "\"WorkoutType\"" + " : " + "\"" + ac_activity_type + "\"" + "\n");
                                activity_list.add("\"Start date\"" + " : " + "\"" + simpleDateFormat.format(new Date(ac_start_date)) + "\"" + "\n");
                                activity_list.add("\"End date\"" + " : " + "\"" + simpleDateFormat.format(new Date(ac_end_date)) + "\"" + "\n");
                                activity_list.add("\"deviceType\"" + " : " + "\"" + ac_device_type + "\"" + "\n");
                                activity_list.add("\"steps\"" + " : " + String.valueOf(ac_step) + "\n");
                                activity_list.add("\"Duration\"" + " : " + "\"" + String.valueOf(ac_active_dur) + "\"" + "\n");
                                activity_list.add("\"Distance\"" + " : " + "\"" + String.valueOf(ac_dist) + "\"" + "\n");
                                activity_list.add("\"totalEnergyBurned\"" + " : " + "\"" + String.valueOf(ac_calorie) + " kcal" + "\"" + "\n");
                                activity_list.add("\"TimeZone\"" + " : " + "\"" + String.valueOf(timeZoneID) + "\"" + "\n");
                                activity_list.add("\"ActivityID\"" + " : " + "\"" + summary_id + "\"" + "}\n");

                                activity_hm.put("", activity_list);
                                //}
                            }
                            Log.e(TAG, "activity_hash_map " + activity_hm);

                            //Log.e(TAG, "returnvalue1" + activityval);

                            activity_server_data = activity_hm.toString().replaceAll("=", "");
                            activity_jsonform = activity_server_data;
                            activity_jsonform = activity_jsonform.substring(1, activity_jsonform.length() - 1);
                            //Log.e(TAG,"ActivityJsonFormat: "+activity_jsonform);
                            //makeNetworkCall();
                        }

                    }
                });

        //readSessionsApiAllSessions();
        //  new CountSteps_Thread(readRequest, mGoogleApiClient, dataSet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String handleDailyRecordInDataSet(DataSet dataSet) {
        dateFormat = DateFormat.getDateInstance();
        timeFormat = getTimeInstance();

        startTime = 0;
        endTime = 24;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, startTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, endTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long endTime = cal.getTimeInMillis();


        for (DataPoint dataPoint : dataSet.getDataPoints()) {
            /*long startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS);
            long endTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS);*/
            String tempValue = "DataPoint start time: " + new Date(startTime)
                    + ",\n end time=" + new Date(endTime)
                    + ",\n data type=" + dataPoint.getDataType().getName()
                    + " \n package=" + dataPoint.getDataSource().getAppPackageName()
                    + ",\n stream=" + dataPoint.getDataSource().getStreamIdentifier();
            //Log.e(TAG, "tempValue1 :\n" + tempValue);

            //ac_start_date = String.valueOf(new Date(startTime));
            //ac_end_date = String.valueOf(new Date(endTime));

            if (dataPoint.getDataSource().getDevice() != null) {
                tempValue += "\nManufacturer=" + dataPoint.getDataSource().getDevice().getManufacturer()
                        + ",\n model=" + dataPoint.getDataSource().getDevice().getModel()
                        + ",\n uid: " + dataPoint.getDataSource().getDevice().getUid()
                        + ",\n type=" + dataPoint.getDataSource().getDevice().getType();
                //Log.e(TAG, "tempValue2 :\n" + tempValue);
            }

            tempValue += "\norigin source: package="
                    + dataPoint.getOriginalDataSource().getAppPackageName()
                    + ",\n stream=" + dataPoint.getOriginalDataSource().getStreamIdentifier();
            //Log.e(TAG, "tempValue3 :\n" + tempValue);

            if (dataPoint.getOriginalDataSource().getDevice() != null) {
                tempValue += "\nManufacturer=" + dataPoint.getOriginalDataSource().getDevice().getManufacturer()
                        + ",\n model=" + dataPoint.getOriginalDataSource().getDevice().getModel()
                        + ",\n uid: " + dataPoint.getOriginalDataSource().getDevice().getUid()
                        + ",\n type=" + dataPoint.getOriginalDataSource().getDevice().getType();
                //Log.e(TAG, "tempValue4 :\n" + tempValue);
                ac_device_type = dataPoint.getOriginalDataSource().getDevice().getManufacturer();
            }

            returnValue += ("\n\n" + tempValue);
            for (DataPoint dp : dataSet.getDataPoints()) {
                for (Field field : dataPoint.getDataType().getFields()) {

                    ac_start_date = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
                    ac_end_date = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));

                    String fieldValue = "Field name: " + field.getName()
                            + ", value: " + dataPoint.getValue(field);
                    returnValue += ("\n" + fieldValue);

                    if (field.getName().equals("steps")) {
                        ac_step = dataPoint.getValue(field).asInt();
                    }

                    if (field.getName().equals("duration")) {
                        ac_active_dur = dataPoint.getValue(field).asInt();
                    }

                    if (field.getName().equals("distance")) {
                        ac_dist = dataPoint.getValue(field).asFloat();
                    }

                    if (field.getName().equals("calories")) {
                        ac_calorie = dataPoint.getValue(field).asFloat();
                    }
                }
            }
        }

        //Log.e(TAG, "returnValue2 :\n" + returnValue);
        return returnValue;
    }


    //----------------------------

    private void readSessionsApiAllSessions() {
        SessionReadRequest readRequest = readFitnessSession();

        Fitness.getSessionsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        // Get a list of the sessions that match the criteria to check the result.
                        List<Session> sessions = sessionReadResponse.getSessions();
                        Log.i(TAG, "Session read was successful. Number of returned sessions is: "
                                + sessions.size());

                        for (Session session : sessions) {
                            // Process the session
                            dumpSession(session);
                            Log.e(TAG,"session"+session);

                            // Process the data sets for this session
                            List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                            for (DataSet dataSet : dataSets) {
                                dumpDataSet(dataSet);
                                Log.e(TAG,"dataset"+dataSet);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to read session");
                    }
                });

    }

    private SessionReadRequest readFitnessSession() {

        startTime = 0;
        endTime = 24;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, startTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, endTime);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long endTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_WORKOUT_EXERCISE)
                /*.read(DataType.TYPE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_ACTIVITY_SAMPLES)
                .read(DataType.TYPE_DISTANCE_DELTA)*/
                .readSessionsFromAllApps()
                .build();
        // [END build_read_session_request]

        return readRequest;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dumpDataSet(DataSet dataSet) {

        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            //DateFormat dateFormat = getTimeInstance();
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

            ac_activity_type = dp.getDataType().getName();

            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

            }

            handleDailyRecordInDataSet(dataSet);

       /* if (ac_activity_type != null && ac_start_date != null && ac_end_date != null &&
                ac_device_type != null && ac_step != 0  && ac_dist != 0 && ac_calorie != 0) {*/


            activity_list.add("{\n" + "\"WorkoutType\"" + " : " + "\"" + ac_activity_type + "\"" + "\n");
            activity_list.add("\"Start date\"" + " : " + "\"" + simpleDateFormat.format(new Date(ac_start_date)) + "\"" + "\n");
            activity_list.add("\"End date\"" + " : " + "\"" + simpleDateFormat.format(new Date(ac_end_date)) + "\"" + "\n");
            activity_list.add("\"deviceType\"" + " : " + "\"" + ac_device_type + "\"" + "\n");
            activity_list.add("\"steps\"" + " : " + String.valueOf(ac_step) + "\n");
            activity_list.add("\"Duration\"" + " : " + "\"" + String.valueOf(ac_active_dur) + "\"" + "\n");
            activity_list.add("\"Distance\"" + " : " + "\"" + String.valueOf(ac_dist) + "\"" + "\n");
            activity_list.add("\"totalEnergyBurned\"" + " : " + "\"" + String.valueOf(ac_calorie) + " kcal" + "\"" + "\n");
            activity_list.add("\"TimeZone\"" + " : " + "\"" + String.valueOf(timeZoneID) + "\"" + "\n");
            activity_list.add("\"ActivityID\"" + " : " + "\"" + summary_id + "\"" + "}\n");

            //activity_list.removeIf(ac_activity_type -> ac_activity_type.equals("com.google."));


            activity_hm.put("", activity_list);

            Log.e(TAG, "session rem" + activity_hm);

            Log.e(TAG, "session_data \n "
                    + "activity_type: " + ac_activity_type + " "
                    + "start_date: " + ac_start_date + " "
                    + "end_date: " + ac_end_date + " "
                    + "device_type: " + ac_device_type + " "
                    + "steps: " + ac_step + " "
                    + "active_duration: " + ac_active_dur + " "
                    + "distnance: " + ac_dist + " "
                    + "calorie: " + ac_calorie + " "
                    + "time_zone: " + ac_time_zone);
            // }

        }
    }

    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Log.i(TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));


    }

    //----------------------------


    /**
     * *************************** ACTIVITY PART ENDS HERE *****************************************
     */
}
