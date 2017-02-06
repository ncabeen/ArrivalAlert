package com.linkedin.ncabeen.arrivalalert;

import android.provider.BaseColumns;

/**
 * Created by ncabe on 2/5/2017.
 */

public final class DestinationContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DestinationContract() {}

    /* Inner class that defines the table contents */
    public static class DestinationEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }
}
