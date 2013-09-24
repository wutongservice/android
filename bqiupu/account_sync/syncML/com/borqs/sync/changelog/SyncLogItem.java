package com.borqs.sync.changelog;

public class SyncLogItem implements Comparable<SyncLogItem> {
		SyncLogItem(String key, String hash) {
			this.key = key;
			this.hash = hash;
		}

		private String key;
		private String hash;
		
		public String getKey(){
			return key;
		}
		
		public String getHash(){
			return hash;
		}

		@Override
		public int compareTo(SyncLogItem that) {
			return this.key.compareTo(((SyncLogItem) that).key);
		}

		public String toString() {
			return "key" + key + ",hash" + hash;
		}
	}