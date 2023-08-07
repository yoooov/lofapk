package com.valeo.app.lofapk.utils.scanner;


public interface OnSelectStateListener<T> {

    void OnSelectStateChanged(boolean state, T file);

}
