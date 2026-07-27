package de.tmjh.stroemwerk.hytale.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.World;
import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.hytale.StroemwerkRuntime;
import javax.annotation.Nonnull;

/**
 * Meldet dem Stroemungsnetz, wenn jemand baut oder abbaut.
 *
 * <p>Bau-Ereignisse sind ECS-Events, deshalb brauchen sie ein
 * {@code EntityEventSystem} statt eines gewoehnlichen Listeners. Beide Systeme
 * hier tun bewusst fast nichts: Position umrechnen, an die Laufzeit
 * weiterreichen. Was daraus folgt, entscheidet die getestete Netzlogik.
 */
public final class BlockChangeSystems {

    private BlockChangeSystems() {
    }

    /**
     * Neu gesetzte Bauteile ins Netz aufnehmen.
     */
    public static final class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

        private final StroemwerkRuntime runtime;

        public Place(StroemwerkRuntime runtime) {
            super(PlaceBlockEvent.class);
            this.runtime = runtime;
        }

        @Override
        public void handle(int index,
                           @Nonnull ArchetypeChunk<EntityStore> chunk,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> commandBuffer,
                           @Nonnull PlaceBlockEvent event) {
            if (event.isCancelled()) {
                return;
            }

            World world = store.getExternalData().getWorld();
            org.joml.Vector3i target = event.getTargetBlock();
            BlockPos pos = new BlockPos(target.x, target.y, target.z);

            // Beim Auswerten des Events steht der Block noch nicht in der Welt.
            // Erst auf dem Weltthread danach ist getBlock() aussagekraeftig.
            world.execute(() -> runtime.onBlockPlaced(world, pos));
        }

        @Override
        public Query<EntityStore> getQuery() {
            return Archetype.empty();
        }
    }

    /**
     * Rechtsklick auf eine Schleuse legt sie um.
     *
     * <p>Nur {@code Pre} wird ausgewertet - {@code Post} wuerde denselben Klick
     * ein zweites Mal melden und die Schleuse sofort zurueckschalten.
     */
    public static final class Use extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

        private final StroemwerkRuntime runtime;

        public Use(StroemwerkRuntime runtime) {
            super(UseBlockEvent.Pre.class);
            this.runtime = runtime;
        }

        @Override
        public void handle(int index,
                           @Nonnull ArchetypeChunk<EntityStore> chunk,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> commandBuffer,
                           @Nonnull UseBlockEvent.Pre event) {
            World world = store.getExternalData().getWorld();
            org.joml.Vector3i target = event.getTargetBlock();
            BlockPos pos = new BlockPos(target.x, target.y, target.z);

            // toggleGate prueft selbst, ob dort ueberhaupt eine Schleuse steht.
            world.execute(() -> runtime.toggleGate(world, pos));
        }

        @Override
        public Query<EntityStore> getQuery() {
            return Archetype.empty();
        }
    }

    /**
     * Abgebaute Bauteile aus dem Netz nehmen.
     */
    public static final class Break extends EntityEventSystem<EntityStore, BreakBlockEvent> {

        private final StroemwerkRuntime runtime;

        public Break(StroemwerkRuntime runtime) {
            super(BreakBlockEvent.class);
            this.runtime = runtime;
        }

        @Override
        public void handle(int index,
                           @Nonnull ArchetypeChunk<EntityStore> chunk,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> commandBuffer,
                           @Nonnull BreakBlockEvent event) {
            if (event.isCancelled()) {
                return;
            }

            World world = store.getExternalData().getWorld();
            org.joml.Vector3i target = event.getTargetBlock();
            BlockPos pos = new BlockPos(target.x, target.y, target.z);

            world.execute(() -> runtime.onBlockRemoved(world, pos));
        }

        @Override
        public Query<EntityStore> getQuery() {
            return Archetype.empty();
        }
    }
}
