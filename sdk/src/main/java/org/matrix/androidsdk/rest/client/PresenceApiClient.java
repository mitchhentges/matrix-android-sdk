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
package org.matrix.androidsdk.rest.client;

import org.matrix.androidsdk.MXApiClient;
import org.matrix.androidsdk.api.PresenceApi;
import org.matrix.androidsdk.api.response.User;

import retrofit.RestAdapter;
import retrofit.client.Response;

public class PresenceApiClient extends MXApiClient {

    PresenceApi mApi;

    @Override
    protected void initApi(RestAdapter restAdapter) {
        mApi = restAdapter.create(PresenceApi.class);
    }

    /**
     * Protected setter for injection by unit tests.
     * @param api the api object
     */
    protected void setApi(PresenceApi api) {
        mApi = api;
    }

    /**
     * Set this user's presence.
     * @param presence the presence state
     * @param statusMsg a status message
     * @param callback on success callback
     */
    public void setPresence(String presence, String statusMsg, final ApiCallback<Void> callback) {
        User userPresence = new User();
        userPresence.presence = presence;
        userPresence.statusMsg = statusMsg;

        mApi.presenceStatus(mCredentials.userId, userPresence, new DefaultCallback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                callback.onSuccess(aVoid);
            }
        });
    }

    /**
     * Get a user's presence state.
     * @param userId the user id
     * @param callback on success callback containing a User object with populated presence and statusMsg fields
     */
    public void getPresence(String userId, final ApiCallback<User> callback) {
        mApi.presenceStatus(userId, new DefaultCallback<User>() {
            @Override
            public void success(User user, Response response) {
                callback.onSuccess(user);
            }
        });
    }
}