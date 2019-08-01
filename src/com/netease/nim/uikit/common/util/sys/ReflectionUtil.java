package com.netease.nim.uikit.common.util.sys;

import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射工具类
 * 通过反射获得对应函数功能
 */
public class ReflectionUtil {

    /**
     * 调用Class的静态方法
     */
    public static <T> T invokeClassMethod(String classPath, String methodName, Class[] paramClasses, Object[] params) {
        return (T) executeClassLoad(getClass(classPath), methodName, paramClasses, params);
    }

    /**
     * 调用Class的无参静态方法
     */
    public static Object invokeMethod(Object obj, String methodName, Object[] params) {
        return invokeMethod(obj, methodName, null, params);
    }

    /**
     * 通过类对象，运行指定方法
     *
     * @param obj        类对象
     * @param methodName 方法名
     * @param paramTypes 参数对应的类型（如果不指定，那么从params来判断，可能会判断不准确，例如把CharSequence 判断成String，导致反射时方法找不到）
     * @param params     参数值
     * @return 失败返回null
     */
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] paramTypes, Object[] params) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        try {
            if (paramTypes == null) {
                if (params != null) {
                    paramTypes = new Class[params.length];
                    for (int i = 0; i < params.length; ++i) {
                        paramTypes[i] = params[i].getClass();
                    }
                }
            }
            Method method = clazz.getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 反射获取对象属性值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

    /**
     * 反射修改对象属性值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 获取Class所有的静态成员
     */
    public static List<Field> getClassStaticField(Class clazz) {
        Field[] fields = clazz.getFields();
        List<Field> ret = new ArrayList<>();

        for (Field field : fields) {
            String m = Modifier.toString(field.getModifiers());
            if (m.contains("static")) {
                ret.add(field);
            }
        }
        return ret;
    }

    /**
     * 获取Class所有静态字段的值
     */
    public static List<Object> getClassStaticFieldValue(Class clazz) {
        Field[] fields = clazz.getFields();
        List<Object> ret = new ArrayList<>();

        for (Field field : fields) {
            String m = Modifier.toString(field.getModifiers());
            if (m.contains("static")) {
                try {
                    ret.add(field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    /**
     * 获取某个字段的@annotation
     */
    public static <A extends Annotation> A getFieldAnnotationsByType(Field field, Class<A> annotationType) {
        return field.getAnnotation(annotationType);
    }

    /**
     * 判断某个类是否包含某个方法，前提是这个类也存在
     */
    public static boolean hasMethod(String classPath, String methodName, Class[] paramClasses) {
        return getMethod(getClass(classPath), methodName, paramClasses) != null;
    }

    /**
     * 反射修改类的私有静态final变量(常量)的值
     * 注意：对于基本类型的静态常量，JAVA在编译的时候就会把代码中对此常量中引用的地方替换成相应常量值。
     * 结论：反射失效的情会发生在int、long、boolean以及String这些基本类型上，而如果把类型改成Integer、Long、Boolean这种包装类型，或者其他诸如Date、Object都不会出现失效的情况。
     */
    public static void setFinalStaticField(final Class clazz, final String fieldName, final Object newValue) {
        try {
            // 获取要反射的类字段
            Field field = clazz.getDeclaredField(fieldName);
            // 打开字段的访问权权限，去除private修饰符的影响
            field.setAccessible(true);
            // 去除final修饰符的影响，将字段设为可修改的
//            Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            // 设置新的值
            field.set(null, newValue);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    /**
     * ****************************** basic ******************************
     */
    private static Class getClass(String str) {
        Class cls = null;
        try {
            cls = Class.forName(str);
        } catch (ClassNotFoundException ignored) {
            ignored.printStackTrace();
        }
        return cls;
    }

    private static Object executeClassLoad(Class cls, String str, Class[] clsArr, Object[] objArr) {
        Object obj = null;
        if (!(cls == null || checkObjExists(str))) {
            Method method = getMethod(cls, str, clsArr);
            if (method != null) {
                method.setAccessible(true);
                try {
                    obj = method.invoke(null, objArr);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    private static Method getMethod(Class cls, String str, Class[] clsArr) {
        Method method = null;
        if (cls == null || checkObjExists(str)) {
            return null;
        }
        try {
            cls.getMethods();
            cls.getDeclaredMethods();
            return cls.getDeclaredMethod(str, clsArr);
        } catch (Exception e) {
            try {
                return cls.getMethod(str, clsArr);
            } catch (Exception e2) {
                return cls.getSuperclass() != null ? getMethod(cls.getSuperclass(), str, clsArr) : method;
            }
        }
    }

    private static boolean checkObjExists(Object obj) {
        return obj == null || obj.toString().equals("") || obj.toString().trim().equals("null");
    }
}