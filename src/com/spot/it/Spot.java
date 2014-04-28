package com.spot.it;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Spot extends ParseObject {
	public String getName() {
		return getString("name");
	}

	public void setName(String name) {
		put("name", name);
	}

	public String getType() {
		return getString("type");
	}

	public void setType(String type) {
		put("type", type);
	}

	public ParseGeoPoint getPosition() {
		return getParseGeoPoint("position");
	}

	public void setPosition(ParseGeoPoint position) {
		put("position", position);
	}

	public static ParseQuery<Spot> getQuery() {
		return ParseQuery.getQuery(Spot.class);
	}
}
