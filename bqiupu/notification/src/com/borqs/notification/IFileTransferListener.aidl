package com.borqs.notification;

interface IFileTransferListener{
    /*
     * Notify when transfer file operation is finished.
     * @param success True if the file has been transfered.
     */
    void onFinished(boolean success);

}
