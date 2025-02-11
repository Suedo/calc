package example.demo.shared.config;

/**
 * Example of a wrapper around ThreadLocal values.
 */
public class ObservationThreadLocalHolder {

    private static final ThreadLocal<String> holder = new ThreadLocal<>();

    public static void setValue(String value) {
        holder.set(value);
    }

    public static String getValue() {
        return holder.get();
    }

    public static void reset() {
        holder.remove();
    }

}