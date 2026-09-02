package com.luanarabelo.treinodaluana.v12;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

/* JADX INFO: loaded from: classes3.dex */
public class PhoneProgressSyncService extends WearableListenerService {
    @Override // com.google.android.gms.wearable.WearableListenerService, com.google.android.gms.wearable.DataApi.DataListener
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : WearBufferCompat.<DataEvent>iterable(dataEvents)) {
            PhoneProgressSync.applyEvent(this, event);
        }
    }
}
