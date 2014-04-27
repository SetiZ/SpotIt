package com.spot.it;

import com.parse.Parse;

public class Application extends android.app.Application {

	public Application() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "wlmt8F0Q1NPM3JPninKpUVKzEjXpLaw4OyThbVQc",
				"r7DsHein7vkiq0oRx5JAfUZ6rlA6vJ8wCl5JFhek");
	}

}
