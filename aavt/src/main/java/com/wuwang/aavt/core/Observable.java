package com.wuwang.aavt.core;

import java.util.ArrayList;
import java.util.Observer;

/*
 * Created by Wuwang on 2017/10/23
 */
public class Observable<Type> implements IObservable<Type> {

    private ArrayList<IObserver<Type>> temp;

    @Override
    public void addObserver(IObserver<Type> observer) {
        if(temp==null){
            temp=new ArrayList<>();
        }
        temp.add(observer);
    }

    @Override
    public boolean removeObserver(IObserver<Type> observer) {
        if(temp==null){
            return true ;
        }
        return temp.remove(observer);
    }

    @Override
    public int clearObserver() {
        if(temp == null) return 0 ;
        int size = temp.size() ;
        temp.clear();
        temp = null ;
        return size;
    }

    @Override
    public void notify(Type type) {
        for (IObserver<Type> t:temp){
            t.onCall(type);
        }
    }

}
