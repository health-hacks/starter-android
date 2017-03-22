# starter-android

**Author:** [Tian Hao](https://github.com/haotianibm)

## Introduction
The starter app has the ability to pull data from GoogleFit and store it locally.

## Prerequisites
- [Android Studio](https://developer.android.com/studio/index.html)

## Setup
- Clone/Download the repo
- Follow [instructions](https://developers.google.com/fit/android/get-api-key) to setup your OAuth Client 2.0 ID
- Run the app

## Data

### Realm
The database we are using for local storage is Realm: http://realm.io. You should check it out even if you are not using the starter apps!

### Data Model
The data model only consists of two model classes: `HealthData` and `HealthDataModel`. Although this seems generic and simplistic, it allows us to store data from almost all data types.

**HealthData**
```
public class HealthData extends RealmObject {
    public String id; 
    public String source;
    public Date date;
    public String type;
    public String participantId;
    public String sessionId;
}
```

`HealthData` stores generic health information (e.g. date, type, source, etc). This object does not store the actual data values; this is persisted in `HealthDataValue`. `HealthData` is loosely linked with `HealthDataValue` through [LinkingObjects](https://realm.io/docs/swift/latest/#inverse-relationships).


***

**HealthDataValue**
```
public class HealthDataValue extends RealmObject {

    public HealthData healthObject;
    public String label;
    public float value;

    public HealthDataValue (){}
}

```

`HealthDataValue` stores specific health values and labels for those values. It's linked to a `HealthData` object.
