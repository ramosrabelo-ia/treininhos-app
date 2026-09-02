package com.luanarabelo.treinodaluana.v12.wear;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchProgressSyncService extends WearableListenerService {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : WearBufferCompat.<DataEvent>iterable(dataEvents)) {
            WatchProgressSync.applyEvent(this, event);
        }
    }
}
