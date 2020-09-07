package com.wuwang.aavt.core;

/**
 * Created by wuwang on 2017/10/20.
 */

public interface IObservable<Type> {

    void addObserver(IObserver<Type> observer);

    boolean removeObserver(IObserver<Type> observer);

    int clearObserver();

    void notify(Type type);

}
