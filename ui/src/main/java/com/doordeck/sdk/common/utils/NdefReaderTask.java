package com.doordeck.sdk.common.utils;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;


import com.doordeck.sdk.common.executor.Executor;
import com.doordeck.sdk.common.executor.MainThread;
import com.doordeck.sdk.common.executor.ThreadedService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Gregory on 26/06/2017.
 */

public class NdefReaderTask implements ThreadedService {

    private final Executor executor;
    private final MainThread mainThread;
    private final String TAG = "NdefReaderTask";
    private Tag nTag;
    private Callback callback;


    public NdefReaderTask(Executor executor, MainThread mainThread){
        this.executor = executor;
        this.mainThread = mainThread;
    }

    public void execute(Tag tag, Callback callback){
        nTag = tag;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        Ndef ndef = Ndef.get(nTag);
        if (ndef == null) {
            // NDEF is not supported by this Tag.
            notifyError("NDEF not supported");
        }

        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    notifyReadSucces(readText(ndefRecord));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Unsupported Encoding", e);
                    notifyError("Unsupported Encoding");
                }
            }
        }


    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }


    private void notifyError(final String message) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onError(message);
            }
        });
    }

    private void notifyReadSucces(final String readText) {
        mainThread.post(new Runnable() {
            @Override public void run() {
                callback.onReadSuccess(readText);
            }
        });
    }

    public interface Callback {
        void onReadSuccess(String value);
        void onError(String message);
    }
}
