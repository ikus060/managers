package com.patrikdufresne.managers;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ManagersTest.class, ObjectIdentityTrackerTest.class,
		ManagersUpdateValidateTest.class })
public class AllTests {

}
