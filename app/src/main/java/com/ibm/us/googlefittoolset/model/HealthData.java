package com.ibm.us.googlefittoolset.model;

import io.realm.RealmObject;

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
}
