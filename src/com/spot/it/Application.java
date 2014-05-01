package com.spot.it;

import com.parse.Parse;
import com.parse.ParseTwitterUtils;

public class Application extends android.app.Application {

	public Application() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "wlmt8F0Q1NPM3JPninKpUVKzEjXpLaw4OyThbVQc",
				"r7DsHein7vkiq0oRx5JAfUZ6rlA6vJ8wCl5JFhek");
		ParseTwitterUtils.initialize("ltsNHwlTmS1EElmKKVl9fr1Iy", "0gQytJG4ncT1YmRbLZtEQWTKVkI8POdInyxBUmAUz5G1RSO4Zw");
	}

}
