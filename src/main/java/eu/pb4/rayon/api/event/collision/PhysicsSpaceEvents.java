package eu.pb4.rayon.api.event.collision;

import eu.pb4.rayon.impl.bullet.collision.body.ElementRigidBody;
import eu.pb4.rayon.impl.bullet.collision.space.MinecraftSpace;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * @since 1.0.0
 */
public final class PhysicsSpaceEvents {
    public static final Event<Init> INIT = EventFactory.createArrayBacked(Init.class, (events) -> (space) -> {
        for (var e : events) {
            e.onInit(space);
        }
    });
    public static final Event<Step> STEP = EventFactory.createArrayBacked(Step.class, (events) -> (space) -> {
        for (var e : events) {
            e.onStep(space);
        }
    });
    public static final Event<ElementAdded> ELEMENT_ADDED = EventFactory.createArrayBacked(ElementAdded.class, (events) -> (space, body) -> {
        for (var e : events) {
            e.onElementAdded(space, body);
        }
    });
    public static final Event<ElementRemoved> ELEMENT_REMOVED = EventFactory.createArrayBacked(ElementRemoved.class, (events) -> (space, body) -> {
        for (var e : events) {
            e.onElementRemoved(space, body);
        }
    });

    private PhysicsSpaceEvents() { }

    @FunctionalInterface
    public interface Init {
        /**
         * Invoked each time a new {@link MinecraftSpace} is created.
         * @param space the minecraft space
         */
        void onInit(MinecraftSpace space);
    }

    @FunctionalInterface
    public interface Step {
        /**
         * Invoked each time the {@link MinecraftSpace} is stepped.
         * @param space the minecraft space
         */
        void onStep(MinecraftSpace space);
    }

    @FunctionalInterface
    public interface ElementAdded {
        /**
         * Invoked each time a new {@link ElementRigidBody} is added to the environment.
         * @param space the minecraft space
         * @param rigidBody the element rigid body being added
         */
        void onElementAdded(MinecraftSpace space, ElementRigidBody rigidBody);
    }

    @FunctionalInterface
    public interface ElementRemoved {
        /**
         * Invoked each time an {@link ElementRigidBody} is removed from the environment.
         * @param space the minecraft space
         * @param rigidBody the element rigid body being removed
         */
        void onElementRemoved(MinecraftSpace space, ElementRigidBody rigidBody);
    }
}