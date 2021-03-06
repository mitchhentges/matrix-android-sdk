/*
 * Copyright 2014 OpenMarket Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrix.androidsdk.sync;

import android.util.Log;

import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.InitialSyncResponse;
import org.matrix.androidsdk.rest.model.RoomResponse;
import org.matrix.androidsdk.rest.model.SyncV2.SyncResponse;

import java.util.List;

/**
 * Listener for the events thread that sends data back to a data handler.
 */
public class DefaultEventsThreadListener implements EventsThreadListener {
    private static final String LOG_TAG = "DfltEventsThread";

    private MXDataHandler mData;

    public DefaultEventsThreadListener(MXDataHandler data) {
        mData = data;
    }

    @Override
    public void onInitialSyncComplete(InitialSyncResponse response) {
        // sanity check
        if (null != response) {
            // Handle presence events
            mData.handleLiveEvents(response.presence);

            // Convert rooms from response
            for (RoomResponse roomResponse : response.rooms) {
                mData.handleInitialRoomResponse(roomResponse);
            }

            // handle the receipts
            mData.handleLiveEvents(response.receipts);

            // save the latest token
            mData.getStore().setEventStreamToken(response.end);
            Log.d(LOG_TAG, "onInitialSyncComplete : commit");
            mData.getStore().commit();
        }

        mData.onInitialSyncComplete();
    }

    @Override
    public void onSyncV2Response(SyncResponse syncResponse, Boolean isInitialSync) {
        mData.onSyncV2Complete(syncResponse, isInitialSync);
    }

    /**
     * Called when getting the users presences.
     * @param presence the users presence
     */
    @Override
    public void onMembersPresencesSyncComplete(List<Event> presence) {
        // Handle presence events
        mData.handleLiveEvents(presence);

        mData.onPresencesSyncComplete();
    }

    @Override
    public void onEventsReceived(List<Event> events, String latestToken) {
        // sanity check
        if ((null != events) && (0 != events.size())) {
            boolean presencesEvent = true;

            // store the matrix id
            for (Event event : events) {
                event.setMatrixId(mData.getUserId());

                if (presencesEvent) {
                    presencesEvent &= (Event.EVENT_TYPE_PRESENCE.equals(event.type)) || (Event.EVENT_TYPE_TYPING.equals(event.type));
                }
            }

            mData.handleLiveEvents(events);

            // do not update the store if the list contains only presence updates or typing
            // it should save some ms avoiding useless file writings.
            if (!presencesEvent) {
                mData.getStore().setEventStreamToken(latestToken);
                Log.d(LOG_TAG, "onEventsReceived : commit");
                mData.getStore().commit();
            }
        }
    }
}
