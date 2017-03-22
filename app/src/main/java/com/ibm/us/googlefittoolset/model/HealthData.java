package com.ibm.us.googlefittoolset.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import java.util.Date;

/**
 * Created by thao on 8/25/16.
 */
public class HealthData extends RealmObject {

    public String id;

    public String source;
    public Date date;
    public String type;
    public String participantId;
    public String sessionId;

    public HealthData(){}

    @Override
    public String toString(){
        String str = "";

        return str;
    }

    private static String generateCompoundId(Date date, String participantId, String source, String type) {
        String dateInSeconds = "" + date.getTime()/1000;
        return dateInSeconds + participantId + source + type;
    }

    public static void storeData(long timestamp, String source, String participantId, String sessionId, String type,
                                 String dataLabel, float dataValue) {
        Realm realm = Realm.getDefaultInstance();
        Date date = new Date(timestamp);
        String id = generateCompoundId(date, participantId, source, type);

        // check duplicates
        // if duplicates exist, do not store data
        RealmResults<HealthData> result = realm.where(HealthData.class).equalTo("id", id).findAll();
        if(result.size() > 0){
            return;
        }

        // creating and pushing realm objects
        realm.beginTransaction();

        HealthData healthData = realm.createObject(HealthData.class);
        healthData.date = date;
        healthData.sessionId = sessionId;
        healthData.participantId = participantId;
        healthData.type = type;
        healthData.source = source;
        healthData.id = id;

        HealthDataValue healthDataValue = realm.createObject(HealthDataValue.class);
        healthDataValue.value = dataValue;
        healthDataValue.label = dataLabel;
        healthDataValue.healthObject = healthData;

        realm.commitTransaction();
    }
}
