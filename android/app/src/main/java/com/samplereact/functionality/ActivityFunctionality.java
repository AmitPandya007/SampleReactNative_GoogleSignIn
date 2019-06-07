package com.samplereact.functionality;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

@SuppressWarnings("deprecation")
public class ActivityFunctionality {

    private static final String TAG = "ActivityClass: =>";
    private static final String random_data = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static String summary_id;
    private static SecureRandom rnd = new SecureRandom();
    private int start, end;
    private Context context;
    private DateFormat dateFormat, timeFormat;
    private String returnValue = "";
    private String ac_activity_type, ac_start_date, ac_end_date, ac_device_type;
    private int ac_step, ac_active_dur;
    private float ac_dist, ac_calorie;
    public static OkHttp3CookieHelper cookieHelper = new OkHttp3CookieHelper();
    private OkHttpNetworkServiceImplementation okHttpNetworkServiceImplementation = OkHttpNetworkServiceImplementation.getInstance();
    private HashMap<String, ArrayList<String>> activity_hm = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> activity_list = new ArrayList<String>();
    private TimeZone ac_time_zone;
    private String timeZoneID, activity_server_data;
    public static String activity_jsonform;
    public static String formattedDate;
    private SimpleDateFormat simpleDateFormat;

    public ActivityFunctionality(Context context, DateFormat dateFormat, DateFormat timeFormat, SimpleDateFormat simpleDateFormat) {
        this.context = context;
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
        this.simpleDateFormat = simpleDateFormat;
    }

    public ActivityFunctionality(Context context) {
        this.context = context;
    }

    public String randomIdGenerator(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(random_data.charAt(rnd.nextInt(random_data.length())));
        Log.e(TAG, "random_10_digit_id : " + sb);
        summary_id = String.valueOf(sb);
        return sb.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void ActivityTypDetails() {
        start = 0;
        end = 24;

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
        cal.set(Calendar.HOUR_OF_DAY, start);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, end);
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
                                        ac_device_type != null && ac_step != 0 && ac_active_dur != 0 && ac_dist != 0 && ac_calorie != 0) {
*/
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
                            Log.e(TAG,"ActivityJsonFormat: "+activity_jsonform);
                            makeNetworkCall();
                        }

                    }
                });

        //readSessionsApiAllSessions();
        //  new CountSteps_Thread(readRequest, mGoogleApiClient, dataSet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String handleDailyRecordInDataSet(DataSet dataSet) {
        dateFormat = DateFormat.getDateInstance();
        timeFormat = getTimeInstance();

        start = 0;
        end = 24;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, start);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, end);
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



/*
    public void readSessionsApiAllSessions() {
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
                            Log.e(TAG, "session" + session);

                            // Process the data sets for this session
                            List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                            for (DataSet dataSet : dataSets) {
                                dumpDataSet(dataSet);
                                Log.e(TAG, "dataset" + dataSet);
                            }
                        }
                        //makeNetworkCall();
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

        start = 0;
        end = 24;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, start);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, end);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        long endTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_WORKOUT_EXERCISE)
                */
/*.read(DataType.TYPE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_ACTIVITY_SAMPLES)
                .read(DataType.TYPE_DISTANCE_DELTA)*//*

                .readSessionsFromAllApps()
                .build();
        // [END build_read_session_request]

        return readRequest;
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

        simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
        dateFormat = DateFormat.getDateInstance();
        timeFormat = getTimeInstance();
        for (DataPoint dp : dataSet.getDataPoints()) {
            //DateFormat dateFormat = getTimeInstance();
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

            ac_activity_type = dp.getDataType().getName();

            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

            }

            handleDailyRecordInDataSet(dataSet);

       */
/* if (ac_activity_type != null && ac_start_date != null && ac_end_date != null &&
                ac_device_type != null && ac_step != 0  && ac_dist != 0 && ac_calorie != 0) {*//*


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

//        if(dialog.isShowing()){
//            dialog.dismiss();
//        }
    }

    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Log.i(TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }
*/

    private void makeNetworkCall() {
        //Get today's date
        Date today_date = Calendar.getInstance().getTime();
        Log.e(TAG, "Current time => " + today_date);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-DD");
        formattedDate = df.format(today_date);
        Log.e(TAG, "Current date => " + formattedDate);

        try {
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

}
