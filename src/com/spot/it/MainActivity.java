package com.spot.it;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends Activity implements LocationListener,
		LoaderCallbacks<Cursor> {
	private GoogleMap map;
	LatLng myPosition;
	private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

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

		if (location != null) {
			// Getting latitude of the current location
			double latitude = location.getLatitude();

			// Getting longitude of the current location
			double longitude = location.getLongitude();

			// Creating a LatLng object for the current location
			myPosition = new LatLng(latitude, longitude);
			Log.i("my position", latitude + " " + longitude);

			// my position
			map.addMarker(new MarkerOptions()
					.position(myPosition)
					.title("Start")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 13));

			final ParseGeoPoint userLocation = new ParseGeoPoint(
					myPosition.latitude, myPosition.longitude);

			final ArrayList<String> filters = new ArrayList<String>();

			grind.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						filters.add("grind");
					} else {
						filters.remove("grind");
					}
					doQuery(userLocation, filters);
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
					doQuery(userLocation, filters);
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
					doQuery(userLocation, filters);
				}
			});

			// krack.co marker :)
			map.addMarker(new MarkerOptions()
					.position(new LatLng(34.051795, -118.285390))
					.title("San Francisco")
					.snippet("Home of krack.co")
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

			// doQuery(userLocation, filters);
		}

	}

	public void doQuery(ParseGeoPoint userLocation, ArrayList<String> filters) {
		cleanUpMarkers();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("spots");
		query.whereNear("position", userLocation);
		query.whereWithinRadians("position", userLocation, 100);
		query.whereContainedIn("type", filters);
		query.setLimit(10);

		// Set up a progress dialog
		final ProgressDialog dlg = new ProgressDialog(MainActivity.this);
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
					}
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
		handleIntent(getIntent());
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

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	private void handleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			doSearch(intent.getStringExtra(SearchManager.QUERY));
		} else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void doSearch(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		Log.i("data", "" + data);
		// getLoaderManager().restartLoader(0, data, this);
	}

	private void getPlace(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		Log.i("data", "" + data);
		// getLoaderManager().restartLoader(1, data, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_actions, menu);
		// Associate searchable configuration with the SearchView
		/*
		 * SearchManager searchManager = (SearchManager)
		 * getSystemService(Context.SEARCH_SERVICE); SearchView searchView =
		 * (SearchView) menu.findItem(R.id.action_search).getActionView();
		 * searchView.setSearchableInfo(
		 * searchManager.getSearchableInfo(getComponentName()));
		 */
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			onSearchRequested();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
		CursorLoader cLoader = null;
		if (arg0 == 0)
			cLoader = new CursorLoader(getBaseContext(),
					PlaceProvider.SEARCH_URI, null, null,
					new String[] { query.getString("query") }, null);
		else if (arg0 == 1)
			cLoader = new CursorLoader(getBaseContext(),
					PlaceProvider.DETAILS_URI, null, null,
					new String[] { query.getString("query") }, null);
		return cLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		showLocations(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
	}

	private void showLocations(Cursor c) {
		MarkerOptions markerOptions = null;
		LatLng position = null;
		map.clear();
		while (c.moveToNext()) {
			markerOptions = new MarkerOptions();
			position = new LatLng(Double.parseDouble(c.getString(1)),
					Double.parseDouble(c.getString(2)));
			markerOptions.position(position);
			markerOptions.title(c.getString(0));
			map.addMarker(markerOptions);
		}
		if (position != null) {
			CameraUpdate cameraPosition = CameraUpdateFactory
					.newLatLng(position);
			map.animateCamera(cameraPosition);
		}
	}
}
