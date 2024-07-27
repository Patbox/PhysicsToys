package dev.lazurite.toolbox.impl.event;

import dev.lazurite.toolbox.api.event.Event;

import java.util.ArrayList;

/**
 * The implementation of {@link Event}.
 * @param <T> the functional interface
 */
public class EventImpl<T> implements Event<T> {
    private final ArrayList<T> events = new ArrayList<>();

    @Override
    public void register(T t) {
        events.add(t);
    }

    @Override
    public void invoke(Object... params) {
        for (T event : events) {
            var method = event.getClass().getMethods()[0];

            try {
                method.setAccessible(true);
                method.invoke(event, params);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Event %s requires %d parameters but %d parameters were given.", event.getClass().getName(), method.getParameterCount(), params.length));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Failed to invoke %s in %s.", method.getName(), event.getClass().getName()));
            }
        }
    }
}