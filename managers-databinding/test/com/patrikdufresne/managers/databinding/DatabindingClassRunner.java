package com.patrikdufresne.managers.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;


public class DatabindingClassRunner extends BlockJUnit4ClassRunner {

	public DatabindingClassRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public void run(final RunNotifier notifier) {
		DefaultRealm realm = new DefaultRealm();
		Realm.runWithDefault(realm, new Runnable() {
			@Override
			public void run() {
				DatabindingClassRunner.super.run(notifier);
			}
		});
	}
}
