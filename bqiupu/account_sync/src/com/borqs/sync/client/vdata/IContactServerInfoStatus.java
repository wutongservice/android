/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.client.vdata;

import android.content.Context;

public interface IContactServerInfoStatus {
    
    /**
     * callback when the sourceID is updated
     * @param context
     */
    public void onSourceIDReady(Context context);
    /**
     * callback when the sourceID update error
     * @param context
     */
    public void onSourceIDReadyError(Context context);
    /**
     * callback when the global borqsID is updated
     * @param context
     */
    public void onGBorqsIDReady(Context context);
    /**
     * callback when the global borqsID update error
     * @param context
     */
    public void onGBorqsIDReadyError(Context context);
    

}
