package com.ibm.us.googlefittoolset.model;

import io.realm.RealmObject;

/**
 * Created by thao on 8/25/16.
 */
public class HealthDataValue extends RealmObject{

    public HealthData healthObject;
    public String label;
    public float value;


    public HealthDataValue (){}
}
