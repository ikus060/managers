package com.patrikdufresne.managers.databinding;

import org.eclipse.core.databinding.observable.IObservable;

import com.patrikdufresne.managers.Managers;


public interface IManagerObservable extends IObservable {

	Managers getManagers();
	
}
