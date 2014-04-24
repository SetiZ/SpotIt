package com.spot.it;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;

public class Application extends android.app.Application {
  // Debugging switch
  public static final boolean APPDEBUG = false;
  
  // Debugging tag for the application
  public static final String APPTAG = "SpotIt";

  // Key for saving the search distance preference
  private static final String KEY_SEARCH_DISTANCE = "searchDistance";

  private static SharedPreferences preferences;

  public Application() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

    //ParseObject.registerSubclass(AnywallPost.class);
    Parse.initialize(this, "wlmt8F0Q1NPM3JPninKpUVKzEjXpLaw4OyThbVQc",
        "r7DsHein7vkiq0oRx5JAfUZ6rlA6vJ8wCl5JFhek");
    preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);
  }

  public static float getSearchDistance() {
    return preferences.getFloat(KEY_SEARCH_DISTANCE, 250);
  }

  public static void setSearchDistance(float value) {
    preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
  }

}
