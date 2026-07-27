package de.tmjh.stroemwerk.hytale.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.tmjh.stroemwerk.flow.Vec3d;
import de.tmjh.stroemwerk.hytale.StroemwerkRuntime;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

/**
 * Traegt herumliegende Gegenstaende mit der Stroemung.
 *
 * <p>Laeuft ueber alle Gegenstands-Entitaeten und fragt fuer jede, wie schnell
 * das Wasser an dieser Stelle traegt. Liegt dort keine Wasserbahn, bleibt der
 * Gegenstand unangetastet und faellt wie gewohnt.
 *
 * <p>Was hier passiert, ist nur Nachschlagen und Setzen - wie schnell und in
 * welche Richtung, rechnet die getestete Logik im Paket {@code flow} aus.
 */
public final class ItemFlowSystem extends EntityTickingSystem<EntityStore> {

    private final StroemwerkRuntime runtime;

    public ItemFlowSystem(StroemwerkRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Nur Gegenstaende, die eine Position und eine Geschwindigkeit haben.
     */
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                ItemComponent.getComponentType(),
                TransformComponent.getComponentType(),
                Velocity.getComponentType());
    }

    /**
     * Nacheinander statt parallel: die Abfrage kann eine Neuberechnung des
     * Stroemungsnetzes ausloesen, und das ist nicht threadsicher.
     */
    @Override
    public boolean isParallel(int entityCount, int chunkCount) {
        return false;
    }

    @Override
    public void tick(float dt, int index,
                     @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
        if (transform == null) {
            return;
        }
        Velocity velocity = chunk.getComponent(index, Velocity.getComponentType());
        if (velocity == null) {
            return;
        }

        World world = store.getExternalData().getWorld();
        Vector3d position = transform.getPosition();
        Vec3d push = runtime.velocityAt(world, position.x, position.y, position.z);

        // Ausserhalb eines fliessenden Kanals mischen wir uns nicht ein,
        // sonst blieben Gegenstaende ueberall in der Luft stehen.
        if (push.length() == 0) {
            return;
        }

        velocity.set(push.x(), push.y(), push.z());
    }
}
