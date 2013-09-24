package com.borqs.qiupu.db;

import android.content.SearchRecentSuggestionsProvider;

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {

	 public final static String AUTHORITY = "com.borqs.qiupu.db.MySuggestionProvider";
	    public final static int MODE = DATABASE_MODE_QUERIES;

	    public MySuggestionProvider() {
	    	super();
	        setupSuggestions(AUTHORITY, MODE);
	    }
}
