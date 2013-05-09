package jas.common.spawner;

import java.util.concurrent.ConcurrentHashMap;

public class EntityCounter {

    private ConcurrentHashMap<String, CountableInt> countingHash = new ConcurrentHashMap<String, CountableInt>();

    public static class CountableInt {
        int value = 1;

        CountableInt(int startValue) {
            value = startValue;
        }

        public int increment() {
            return ++value;
        }

        public int get() {
            return value;
        }
    }

    /**
     * Gets or Puts if absent a CountableInt from the provided Map.
     * 
     * @param key Key for Value inside the hash we want to get
     * @param countingHash Hash that is being used for Counting
     * @param defaultValue Default Value CountableInt is initialized to
     * @return CountableInt that has not been iterated
     */
    public CountableInt getOrPutIfAbsent(String key, int defaultValue) {
        CountableInt count = countingHash.get(key);
        if (count == null) {
            count = new CountableInt(defaultValue);
            countingHash.put(key, count);
        }
        return count;
    }

    /**
     * Gets or Puts if absent a CountableInt from the provided Map.
     * 
     * @param key Key for Value inside the hash we want to iterate
     * @param countingHash Hash that is being used for Counting
     * @param defaultValue Default Value CountableInt is initialized to
     * @return CountableInt that has been iterated
     */
    public CountableInt incrementOrPutIfAbsent(String key, int defaultValue) {
        CountableInt count = countingHash.get(key);
        if (count == null) {
            count = new CountableInt(defaultValue);
            countingHash.put(key, count);
        } else {
            count.increment();
        }
        return count;
    }
}
