package com.borqs.sync.client.vdata.card;

import java.util.ArrayList;

import android.text.TextUtils;

public class AddressFieldsParser {
	private String mailbox;
	private String detail;
	private String street;
	private String locality;
	private String region;
	private String postalCode;
	private String country;

	public AddressFieldsParser(String fullData) {
		if (fullData != null) {
			parseFullData(fullData);
		}
	}

	public AddressFieldsParser(String mailbox, String detail, String street,
									String locality, String region, 
									String postalCode, String country) {
		this.mailbox = mailbox;
		this.detail = detail;
		this.street = street;
		this.locality = locality;
		this.region = region;
		this.postalCode = postalCode;
		this.country = country;
	}

	public String getFullData() {
		return genFullData();
	}

	static public String getDisplayString(String fullData) {
		AddressFieldsParser address = new AddressFieldsParser(fullData);

		if (address.detail == null && address.street == null
				&& address.locality == null && address.region == null
				&& address.postalCode == null && address.country == null) {
			return address.mailbox;
		} else {
			return address.mailbox + " " + address.detail + " "
					+ address.street + " " + address.locality + " "
					+ address.region + " " + address.postalCode + " "
					+ address.country;
		}
	}

	private String genFullData() {
		if (TextUtils.isEmpty(detail) && TextUtils.isEmpty(street)
				&& TextUtils.isEmpty(locality) && TextUtils.isEmpty(region)
				&& TextUtils.isEmpty(postalCode) && TextUtils.isEmpty(country)) {
			return getData(mailbox);
		} else {
			return 	getData(mailbox) + ";"
					+ getData(detail) + ";"
					+ getData(street) + ";"
					+ getData(locality) + ";"
					+ getData(region) + ";"
					+ getData(postalCode) + ";"
					+ getData(country);
		}
	}
	
	private String getData(String data){
//		String formatData = VCardComposer.escapeTranslator(data);
//		if(formatData == null){
//			return "";
//		}
//		return formatData;
	    return data;
	}

	private void parseFullData(String data) {
		// split data
		ArrayList<String> arrayList = VCardUtil.spliteString(data);
		
		// get the five fields value 
		mailbox = arrayList.size() > 0 ? arrayList.get(0) : "";
		detail = arrayList.size() > 1 ? arrayList.get(1) : "";
		street = arrayList.size() > 2 ? arrayList.get(2) : "";
		locality = arrayList.size() > 3 ? arrayList.get(3) : "";
		region = arrayList.size() > 4 ? arrayList.get(4) : "";
		postalCode = arrayList.size() > 5 ? arrayList.get(5) : "";
		country = arrayList.size() > 6 ? arrayList.get(6) : "";
	}
	
	public void setMailboxl(String mailbox) {
		this.mailbox = mailbox;
	}

	public String getMailbox() {
		return mailbox;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getDetail() {
		return detail;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreet() {
		return street;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getLocality() {
		return locality;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRegion() {
		return region;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}
}
