package com.spot.it;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends Activity implements LocationListener,
		OnItemClickListener, OnEditorActionListener {

	private static final String LOG_TAG = "";

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String TYPE_DETAILS = "/details";
	private static final String OUT_JSON = "/json";

	private static final String API_KEY = "AIzaSyA8byrjC61ok3419M13-Icl1nt-uuo8lNw";
	private Circle mapCircle;
	private GoogleMap map;
	LatLng myPosition;
	// position when we click
	LatLng clicPosition;
	private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
	private ProgressDialog dlg;
	private ParseGeoPoint userLocation;
	private ArrayList<String> filters = new ArrayList<String>();
	private Marker user;
	private Marker baryMarker;

	ArrayList<String> referenceList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		final AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.search);
		autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this,
				R.layout.list_item));
		autoCompView.setOnEditorActionListener(this);
		autoCompView.setOnItemClickListener(this);

		CheckBox grind = (CheckBox) findViewById(R.id.grind);
		CheckBox rampe = (CheckBox) findViewById(R.id.rampe);
		CheckBox block = (CheckBox) findViewById(R.id.block);

		map.setMyLocationEnabled(true);

		// Getting LocationManager object from System Service LOCATION_SERVICE
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Creating a criteria object to retrieve provider
		Criteria criteria = new Criteria();

		// Getting the name of the best provider
		String provider = locationManager.getBestProvider(criteria, true);

		// Getting Current Location
		Location location = locationManager.getLastKnownLocation(provider);
		// Location location = map.getMyLocation();

		if (location != null) {
			// Getting latitude of the current location
			double latitude = location.getLatitude();

			// Getting longitude of the current location
			double longitude = location.getLongitude();

			// Creating a LatLng object for the current location
			myPosition = new LatLng(latitude, longitude);
			Log.i("my position", latitude + " " + longitude);

			// my position
			user = map.addMarker(new MarkerOptions()
					.position(myPosition)
					.title("Start")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
			updateCircle(myPosition);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 13));

			map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
				@Override
				public void onCameraChange(CameraPosition cameraPosition) {
					Log.i("camera", "zoom: " + cameraPosition.zoom);
					for (String objId : new HashSet<String>(mapMarkers.keySet())) {
						Marker marker = mapMarkers.get(objId);
						marker.setVisible(cameraPosition.zoom > 11);
					}
					;
				}
			});

			userLocation = new ParseGeoPoint(myPosition.latitude,
					myPosition.longitude);

			grind.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						filters.add("grind");
					} else {
						filters.remove("grind");
					}
					doQuery(filters);
				}
			});

			rampe.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						filters.add("rampe");
					} else {
						filters.remove("rampe");
					}
					doQuery(filters);
				}
			});

			block.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						filters.add("block");
					} else {
						filters.remove("block");
					}
					doQuery(filters);
				}
			});

			// krack.co marker :)
			map.addMarker(new MarkerOptions()
					.position(new LatLng(34.051795, -118.285390))
					.title("San Francisco")
					.snippet("Home of krack.co")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

			// doQuery(filters);
		}

	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		Log.i("button", "haha");
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			Log.i("button", "appui");
			return true;
		}
		return false;
	}

	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		String str = (String) adapterView.getItemAtPosition(position);
		GetPlaces task = new GetPlaces();
		ArrayList<String> latlng = null;
		try {
			latlng = task.execute(referenceList.get(position)).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		Double lat = Double.valueOf(latlng.get(0));
		Double lng = Double.valueOf(latlng.get(1));
		LatLng clicPosition = new LatLng(lat, lng);
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(clicPosition, 13));
		map.addMarker(
				new MarkerOptions()
						.position(clicPosition)
						.title(str)
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
				.showInfoWindow();
		updateCircle(clicPosition);
		updatePosition(clicPosition);
		user.remove();
		// doQuery(filters);
	}

	private void updateCircle(LatLng myLatLng) {
		if (mapCircle == null) {
			mapCircle = map.addCircle(new CircleOptions().center(myLatLng)
					.radius(1000));
			int baseColor = Color.DKGRAY;
			mapCircle.setStrokeColor(baseColor);
			mapCircle.setStrokeWidth(2);
			mapCircle.setFillColor(Color.argb(50, Color.red(baseColor),
					Color.green(baseColor), Color.blue(baseColor)));
		}
		mapCircle.setCenter(myLatLng);
		mapCircle.setRadius(1000); // Convert radius in feet
									// to meters.
	}

	private void updatePosition(LatLng myLatLng) {
		userLocation = new ParseGeoPoint(myLatLng.latitude, myLatLng.longitude);
	}

	public void doQuery(ArrayList<String> filters) {
		cleanUpMarkers();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("spots");
		query.whereNear("position", userLocation);
		// query.whereWithinRadians("position", userLocation, 1000);
		// query.whereWithinKilometers("position", userLocation, 1);
		query.whereContainedIn("type", filters);
		query.setLimit(10);

		// Set up a progress dialog
		dlg = new ProgressDialog(MainActivity.this);
		dlg.setTitle("Please wait.");
		dlg.setMessage("Looking around...");
		dlg.show();

		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> spotslist, ParseException e) {
				LatLng marker;
				Marker mark;
				if (e == null) {
					Log.d("score", "Retrieved " + spotslist.size() + " spots");
					dlg.hide();
					for (ParseObject spot : spotslist) {
						marker = new LatLng(spot.getParseGeoPoint("position")
								.getLatitude(), spot.getParseGeoPoint(
								"position").getLongitude());
						mark = map.addMarker(new MarkerOptions()
								.position(marker).title(spot.getString("name"))
								.snippet(spot.getString("type")));

						mapMarkers.put(spot.getObjectId(), mark);
						// remove barycenter marker before redrawing it
						try {
							baryMarker.remove();
						} catch (NullPointerException ex) {

						}
						barycentre();
					}
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});

	}

	/*
	 * Helper method to clean up old markers
	 */
	private void cleanUpMarkers() {
		for (String objId : new HashSet<String>(mapMarkers.keySet())) {
			Marker marker = mapMarkers.get(objId);
			marker.remove();
			mapMarkers.get(objId).remove();
			mapMarkers.remove(objId);
		}
	}

	private void barycentre() {
		Double lattitudes = 0.0;
		Double longitudes = 0.0;
		int size = 0;
		for (String objId : new HashSet<String>(mapMarkers.keySet())) {
			lattitudes = lattitudes
					+ mapMarkers.get(objId).getPosition().latitude;
			longitudes = longitudes
					+ mapMarkers.get(objId).getPosition().longitude;
			size++;
		}
		Log.i("lattitudes", "" + lattitudes);
		Log.i("longitudes", "" + longitudes);
		Log.i("size", "" + size);
		LatLng bary = new LatLng(lattitudes / size, longitudes / size);
		baryMarker = map.addMarker(new MarkerOptions()
				.position(bary)
				.title("" + size)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
	}

	@Override
	public void onLocationChanged(Location location) {
		LatLng myLatLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		// Update map radius indicator
		updateCircle(myLatLng);
		updatePosition(myLatLng);
	}

	private class GetPlaces extends AsyncTask<String, Void, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(String... input) {
			ArrayList<String> resultList = null;

			HttpURLConnection conn = null;
			StringBuilder jsonResults = new StringBuilder();
			try {
				StringBuilder sb = new StringBuilder(PLACES_API_BASE
						+ TYPE_DETAILS + OUT_JSON);
				sb.append("?sensor=false&key=" + API_KEY);
				sb.append("&reference=" + URLEncoder.encode(input[0], "utf8"));
				Log.d("results", sb.toString());
				URL url = new URL(sb.toString());
				conn = (HttpURLConnection) url.openConnection();
				InputStreamReader in = new InputStreamReader(
						conn.getInputStream());

				// Load the results into a StringBuilder
				int read;
				char[] buff = new char[1024];
				while ((read = in.read(buff)) != -1) {
					jsonResults.append(buff, 0, read);
				}
			} catch (MalformedURLException e) {
				Log.e(LOG_TAG, "Error processing Places API URL", e);
				return resultList;
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error connecting to Places API", e);
				return resultList;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

			try {
				// Create a JSON object hierarchy from the results
				Log.d("results", jsonResults.toString());
				JSONObject jsonObj = new JSONObject(jsonResults.toString());

				resultList = new ArrayList<String>(2);
				resultList.add(jsonObj.getJSONObject("result")
						.getJSONObject("geometry").getJSONObject("location")
						.getString("lat"));
				resultList.add(jsonObj.getJSONObject("result")
						.getJSONObject("geometry").getJSONObject("location")
						.getString("lng"));
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Cannot process JSON results", e);
			}

			return resultList;
		}

		protected void onPostExecute(ArrayList<String> result) {

		}
	}

	private ArrayList<String> autocomplete(String input) {
		ArrayList<String> resultList = null;

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			StringBuilder sb = new StringBuilder(PLACES_API_BASE
					+ TYPE_AUTOCOMPLETE + OUT_JSON);
			sb.append("?sensor=false&key=" + API_KEY);
			// sb.append("&type=geocode");
			sb.append("&input=" + URLEncoder.encode(input, "utf8"));
			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Error processing Places API URL", e);
			return resultList;
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error connecting to Places API", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			// Extract the Place descriptions from the results
			resultList = new ArrayList<String>(predsJsonArray.length());
			referenceList = new ArrayList<String>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				resultList.add(predsJsonArray.getJSONObject(i).getString(
						"description"));
				referenceList.add(predsJsonArray.getJSONObject(i).getString(
						"reference"));
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, "Cannot process JSON results", e);
		}

		return resultList;
	}

	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String>
			implements Filterable {
		private ArrayList<String> resultList;

		public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						// Retrieve the autocomplete results.
						resultList = autocomplete(constraint.toString());
						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}
	}

}
