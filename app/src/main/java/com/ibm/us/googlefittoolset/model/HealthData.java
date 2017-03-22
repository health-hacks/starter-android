package com.ibm.us.googlefittoolset.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import java.util.Date;

/**
 * Created by thao on 8/25/16.
 */
public class HealthData extends RealmObject {

    public String id;   //label+timestamp

    public String source;
    public String origin;
    public String type;

    public Date date;

    public HealthData(){}

    @Override
    public String toString(){
        String str = "";

        return str;
    }

    public static void storeData(String id, long timestamp, String origin, String source, String dataLabel, float dataValue) {
        Realm realm = Realm.getDefaultInstance();

        // check duplicates
        // if duplicates exist, do not store data
        RealmResults<HealthData> result = realm.where(HealthData.class).equalTo("id", id).findAll();
        if(result.size() > 0){
            return;
        }

        // creating and pushing realm objects
        realm.beginTransaction();

        HealthData healthData = realm.createObject(HealthData.class);
        healthData.date = new Date(timestamp);
        healthData.origin = origin;
        healthData.source = source;
        healthData.id = id;

        HealthDataValue healthDataValue = realm.createObject(HealthDataValue.class);
        healthDataValue.value = dataValue;
        healthDataValue.label = dataLabel;
        healthDataValue.healthObject = healthData;

        realm.commitTransaction();
    }
}
