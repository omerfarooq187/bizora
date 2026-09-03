package com.innovatewithomer.bizora.util;

/** A cached screen can implement this hook to reload only its changing data. */
public interface RefreshableView {
    void refreshView();
}
