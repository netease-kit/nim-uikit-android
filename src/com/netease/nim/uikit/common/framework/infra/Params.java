package com.netease.nim.uikit.common.framework.infra;

public final class Params {
    public static final int[] getDimension(Object[] params, int index, int[] def) {
        if (params != null && params.length > index && params[index] != null) {
            if (params[index] instanceof int[]) {
                int[] param = (int[]) params[index];
                if (param.length > 1) {
                    return param;
                }
            }
        }

        return def;
    }

    public static final String getString(Object[] params, int index, String def) {
        if (params != null && params.length > index && params[index] != null) {
            if (params[index] instanceof String) {
                String param = (String) params[index];
                return param;
            }
        }

        return def;
    }

    public static final boolean getBoolean(Object[] params, int index, boolean def) {
        if (params != null && params.length > index && params[index] != null) {
            if (params[index] instanceof Boolean) {
                Boolean param = (Boolean) params[index];
                return param;
            }
        }

        return def;
    }

    public static final Object getObject(Object[] params, int index, Object def) {
        if (params != null && params.length > index && params[index] != null) {
            return params[index];
        }

        return def;
    }
}
