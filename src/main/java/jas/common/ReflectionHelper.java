package jas.common;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
    /**
     * Helper used to See if we are unObfuscated by checking for a known non-Obfuscated name
     * return true if unObfuscated (eclipse), false if obfuscated (Minecraft)
     * @param list
     */
    public static boolean isUnObfuscated(Class<?> regularClass, String regularClassName){
        return regularClass.getSimpleName().equals(regularClassName);
    }
    
    private static Method getIntanceOfMethod(String eclipseName, String seargeName, Object containterInstance,
            Class<?>... topClassToLook) {
        Class<?> classToSearch = containterInstance.getClass();
        while (classToSearch.getSuperclass() != null) {
            for (Method method : classToSearch.getDeclaredMethods()) {
                if (eclipseName.equalsIgnoreCase(method.getName()) || seargeName.equalsIgnoreCase(method.getName())) {
                    return method;
                }
            }
            classToSearch = classToSearch.getSuperclass();
        }
        return null;
    }

    public static Object invokeMethod(String eclipseName, String seargeName, Object containterInstance, Object... args) {
        try {
            Method method;
            method = getIntanceOfMethod(eclipseName, seargeName, containterInstance);
            if (method == null) {
                throw new NoSuchMethodException();
            }
            method.setAccessible(true);
            return method.invoke(containterInstance, args);
        } catch (NoSuchMethodException e) {
            JASLog.log().severe("Obfuscation needs updating to access method %s. Notify modmaker.", eclipseName);
            e.printStackTrace();
        } catch (SecurityException e) {
            JASLog.log().severe("SecurityException accessing method %s. Notify modmaker.", eclipseName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            JASLog.log().severe("IllegalAccessException accessing method %s. Notify modmaker.", eclipseName);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            JASLog.log().severe("IllegalArgumentException accessing method %s. Notify modmaker.", eclipseName);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            JASLog.log().severe("InvocationTargetException accessing method %s. Notify modmaker.", eclipseName);
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Helper method to Perform Reflection to Set non-static Field of Provided Type. Field is assumed Private.
     * 
     * @param fieldName
     * @param containingClass Class that contains desired field containerInstance should be castable to it. Required to
     *            get fields from parent classes
     * @param containterInstance Instance of the Object to get the non-static Field
     * @param isPrivate Whether the field is private and requires setAccessible(true)
     * @param type
     * @param value
     * @return
     */
    public static <T> void setFieldUsingReflection(String fieldName, Class<?> containingClass, Object containterInstance, boolean isPrivate, T value) {
        try {
            Field desiredField = containingClass.getDeclaredField(fieldName);
            if (isPrivate) {
                desiredField.setAccessible(true);
            }
            desiredField.set(containterInstance, value);
        }catch (NoSuchFieldException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }catch (IllegalArgumentException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }catch (SecurityException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to Perform Reflection to Set non-static Field of Provided Type. Field is assumed Private.
     * 
     * @param fieldName
     * @param containingClass Class that contains desired field containerInstance should be castable to it. Required to
     *            get fields from parent classes
     * @param containterInstance Instance of the Object to get the non-static Field
     * @param isPrivate Whether the field is private and requires setAccessible(true)
     * @param type
     * @param value
     * @return
     * @throws NoSuchFieldException
     */
    public static <T> void setCatchableFieldUsingReflection(String fieldName, Class<?> containingClass,
            Object containterInstance, boolean isPrivate, T value) throws NoSuchFieldException {
        try {
            Field desiredField = containingClass.getDeclaredField(fieldName);
            if (isPrivate) {
                desiredField.setAccessible(true);
            }
            desiredField.set(containterInstance, value);
        } catch (IllegalArgumentException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.",
                    fieldName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.",
                    fieldName);
            e.printStackTrace();
        } catch (SecurityException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.",
                    fieldName);
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to Perform Reflection to Get non-static Field of Provided Type. Field is assumed Private.
     * @param fieldName
     * @param type
     * @return
     */
    public static <T> T getFieldFromReflection(String fieldName, Object containterInstance, Class<T> type) {
        try {
            Field desiredField = containterInstance.getClass().getDeclaredField(fieldName);
            desiredField.setAccessible(true);
            return type.cast(desiredField.get(containterInstance));
        }catch (NoSuchFieldException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (IllegalArgumentException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (SecurityException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Helper method to Perform Reflection to Get non-static Field of Provided Type. Field is assumed Private.
     * @param fieldName
     * @param type
     * @return
     */
    public static <T> T getFieldFromReflection(String fieldName, Class<?> fieldClass, Object containterInstance, Class<T> type) {
        try {
            Field desiredField = fieldClass.getDeclaredField(fieldName);
            desiredField.setAccessible(true);
            return type.cast(desiredField.get(containterInstance));
        }catch (NoSuchFieldException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (IllegalArgumentException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (SecurityException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Helper method to Perform Reflection to Get non-static Field of Provided Type. Field is assumed Private.
     * @param fieldName
     * @param type
     * @return
     */
    public static <T> T getCatchableFieldFromReflection(String fieldName, Object containterInstance, Class<T> type) throws NoSuchFieldException {
        try {
            Field desiredField = containterInstance.getClass().getDeclaredField(fieldName);
            desiredField.setAccessible(true);
            return type.cast(desiredField.get(containterInstance));
        }catch (IllegalArgumentException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (SecurityException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Helper method to Perform Reflection to Get non-static Field of Provided Type. Field is assumed Private.
     * @param fieldName
     * @param type
     * @return
     */
    public static <T> T getCatchableFieldFromReflection(String fieldName, Class<?> containingClass, Object containterInstance, Class<T> type) throws NoSuchFieldException {
        try {
            Field desiredField = containingClass.getDeclaredField(fieldName);
            desiredField.setAccessible(true);
            return type.cast(desiredField.get(containterInstance));
        }catch (IllegalArgumentException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }catch (SecurityException e) {
            JASLog.log().severe("Obfuscation needs to be updated to access the %s %s. Please notify modmaker Immediately.", fieldName, type.getSimpleName());
            e.printStackTrace();
        }
        return null;
    }
    
}
