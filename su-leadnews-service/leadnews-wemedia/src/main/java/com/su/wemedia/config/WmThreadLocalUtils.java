package com.su.wemedia.config;

import com.su.model.common.wemedia.pojos.WmUser;

public class WmThreadLocalUtils {
    private final static ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<WmUser>();

    public static void set(WmUser wmUser) {
        WM_USER_THREAD_LOCAL.set(wmUser);

    }

    public static WmUser get(){
        return WM_USER_THREAD_LOCAL.get();
    }
    public static void remove() {
        WM_USER_THREAD_LOCAL.remove();
    }

}
