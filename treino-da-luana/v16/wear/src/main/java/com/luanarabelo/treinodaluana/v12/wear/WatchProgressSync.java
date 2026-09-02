package com.luanarabelo.treinodaluana.v12.wear;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;
import java.util.Map;

public final class WatchProgressSync {
    public static final String BLOCK_PATH_PREFIX = "/treino-lua/v13/block/";
    public static final String LOAD_PATH_PREFIX = "/treino-lua/v14/load/";
    public static final String SET_PATH_PREFIX = "/treino-lua/v15/sets/";
    public static final String SUMMARY_PATH_PREFIX = "/treino-lua/v15/summary/";
    private static final String PREFS = "treino_v12_watch8";
    private static final String SOURCE = "watch";
    private static final String WEEK = "sync_week";

    public interface PullCallback {
        void onComplete(boolean changed);
    }

    private WatchProgressSync() {}

    public static void prepareWeek(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String current = weekKey();
        if (current.equals(prefs.getString(WEEK, ""))) return;
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            String key = entry.getKey();
            if (key.matches("w\\d+_b\\d+")
                    || key.matches("w\\d+_e\\d+_(done|mask)")
                    || key.startsWith("sync_ts_")
                    || key.startsWith("set_sync_ts_")) editor.remove(key);
        }
        editor.putString(WEEK, current).apply();
    }

    public static void publishBlock(Context context, int workout, int block, boolean done) {
        Context app = context.getApplicationContext();
        prepareWeek(app);
        SharedPreferences prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long timestamp = System.currentTimeMillis();
        prefs.edit().putBoolean(blockKey(workout, block), done)
                .putLong(blockTimestampKey(workout, block), timestamp).apply();

        PutDataMapRequest request = PutDataMapRequest.create(BLOCK_PATH_PREFIX + workout + "/" + block);
        DataMap map = request.getDataMap();
        map.putString("source", SOURCE);
        map.putString("week", weekKey());
        map.putInt("workout", workout);
        map.putInt("block", block);
        map.putBoolean("done", done);
        map.putLong("updated_at", timestamp);
        map.putLong("nonce", System.nanoTime());
        PutDataRequest dataRequest = request.asPutDataRequest();
        dataRequest.setUrgent();
        Wearable.getDataClient(app).putDataItem(dataRequest);
    }

    public static void publishExerciseMask(Context context, int workout, int exercise, int mask) {
        Context app = context.getApplicationContext();
        long timestamp = System.currentTimeMillis();
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt(seriesKey(workout, exercise), mask)
                .putLong(setTimestampKey(workout, exercise), timestamp)
                .apply();
        PutDataMapRequest request = PutDataMapRequest.create(SET_PATH_PREFIX + workout + "/" + exercise);
        DataMap map = request.getDataMap();
        map.putString("source", SOURCE);
        map.putString("week", weekKey());
        map.putInt("workout", workout);
        map.putInt("exercise", exercise);
        map.putInt("mask", mask);
        map.putLong("updated_at", timestamp);
        map.putLong("nonce", System.nanoTime());
        PutDataRequest dataRequest = request.asPutDataRequest();
        dataRequest.setUrgent();
        Wearable.getDataClient(app).putDataItem(dataRequest);
    }

    public static void publishWorkoutSummary(Context context, int workout, long startMillis,
                                             long endMillis, int completedExercises) {
        Context app = context.getApplicationContext();
        PutDataMapRequest request = PutDataMapRequest.create(
                SUMMARY_PATH_PREFIX + workout + "/" + System.currentTimeMillis());
        DataMap map = request.getDataMap();
        map.putString("source", SOURCE);
        map.putString("week", weekKey());
        map.putInt("workout", workout);
        map.putLong("start_millis", startMillis);
        map.putLong("end_millis", endMillis);
        map.putInt("completed_exercises", completedExercises);
        map.putLong("nonce", System.nanoTime());
        PutDataRequest dataRequest = request.asPutDataRequest();
        dataRequest.setUrgent();
        Wearable.getDataClient(app).putDataItem(dataRequest);
    }

    public static void pullRemote(Context context, PullCallback callback) {
        Context app = context.getApplicationContext();
        prepareWeek(app);
        Wearable.getDataClient(app).getDataItems()
                .addOnSuccessListener(buffer -> {
                    boolean changed = false;
                    try {
                        for (DataItem item : WearBufferCompat.<DataItem>iterable(buffer)) {
                            changed |= applyDataItem(app, item);
                        }
                    } finally {
                        WearBufferCompat.release(buffer);
                    }
                    callback.onComplete(changed);
                })
                .addOnFailureListener(error -> callback.onComplete(false));
    }

    public static boolean applyEvent(Context context, DataEvent event) {
        return event.getType() == DataEvent.TYPE_CHANGED
                && applyDataItem(context.getApplicationContext(), event.getDataItem());
    }

    public static boolean applyDataItem(Context context, DataItem item) {
        String path = item.getUri().getPath();
        if (path == null) return false;
        try {
            DataMap map = DataMapItem.fromDataItem(item).getDataMap();
            if (SOURCE.equals(map.getString("source"))) return false;
            if (path.startsWith(LOAD_PATH_PREFIX)) return applyLoad(context, map);
            if (path.startsWith(SET_PATH_PREFIX)) return applySetMask(context, map);
            if (path.startsWith(BLOCK_PATH_PREFIX)) return applyBlock(context, map);
        } catch (RuntimeException ignored) {
            return false;
        }
        return false;
    }

    private static boolean applyBlock(Context context, DataMap map) {
        if (!weekKey().equals(map.getString("week"))) return false;
        int workout = map.getInt("workout", -1);
        int block = map.getInt("block", -1);
        long timestamp = map.getLong("updated_at", 0L);
        if (workout < 0 || workout >= 4 || block < 0 || block >= 6 || timestamp == 0L) return false;
        prepareWeek(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (timestamp <= prefs.getLong(blockTimestampKey(workout, block), 0L)) return false;
        prefs.edit().putBoolean(blockKey(workout, block), map.getBoolean("done", false))
                .putLong(blockTimestampKey(workout, block), timestamp).apply();
        return true;
    }

    private static boolean applyLoad(Context context, DataMap map) {
        int workout = map.getInt("workout", -1);
        int exercise = map.getInt("exercise", -1);
        long timestamp = map.getLong("updated_at", 0L);
        if (workout < 0 || workout >= 4 || exercise < 0 || exercise >= 11 || timestamp == 0L) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (timestamp <= prefs.getLong(loadTimestampKey(workout, exercise), 0L)) return false;
        prefs.edit().putString(loadKey(workout, exercise), map.getString("load", ""))
                .putLong(loadTimestampKey(workout, exercise), timestamp).apply();
        return true;
    }

    private static boolean applySetMask(Context context, DataMap map) {
        if (!weekKey().equals(map.getString("week"))) return false;
        int workout = map.getInt("workout", -1);
        int exercise = map.getInt("exercise", -1);
        long timestamp = map.getLong("updated_at", 0L);
        if (workout < 0 || workout >= 4 || exercise < 0 || exercise >= 11 || timestamp == 0L) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (timestamp <= prefs.getLong(setTimestampKey(workout, exercise), 0L)) return false;
        prefs.edit().putInt(seriesKey(workout, exercise), map.getInt("mask", 0))
                .putLong(setTimestampKey(workout, exercise), timestamp).apply();
        return true;
    }

    private static String blockKey(int workout, int block) {
        return "w" + workout + "_b" + block;
    }

    private static String blockTimestampKey(int workout, int block) {
        return "sync_ts_w" + workout + "_b" + block;
    }

    private static String loadKey(int workout, int exercise) {
        return "load_w" + workout + "_e" + exercise;
    }

    private static String loadTimestampKey(int workout, int exercise) {
        return "load_sync_ts_w" + workout + "_e" + exercise;
    }

    private static String seriesKey(int workout, int exercise) {
        return "w" + workout + "_e" + exercise + "_mask";
    }

    private static String setTimestampKey(int workout, int exercise) {
        return "set_sync_ts_w" + workout + "_e" + exercise;
    }

    private static String weekKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.WEEK_OF_YEAR);
    }
}
