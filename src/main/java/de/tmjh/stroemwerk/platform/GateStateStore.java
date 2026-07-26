package de.tmjh.stroemwerk.platform;

import de.tmjh.stroemwerk.flow.BlockPos;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merkt sich pro Welt, welche Schleusen geschlossen sind.
 *
 * <p>Gespeichert werden nur die geschlossenen - offen ist der Normalfall und
 * braucht keinen Eintrag. Eine frisch platzierte Schleuse steht damit offen.
 *
 * <p>Wie {@link PlacedFacingStore} nur im Speicher: nach einem Serverneustart
 * stehen alle Schleusen wieder offen.
 */
public final class GateStateStore {

    private final Map<String, Set<BlockPos>> closedByWorld = new ConcurrentHashMap<>();

    public boolean isOpen(String worldName, BlockPos pos) {
        Set<BlockPos> closed = closedByWorld.get(worldName);
        return closed == null || !closed.contains(pos);
    }

    public void setOpen(String worldName, BlockPos pos, boolean open) {
        if (open) {
            Set<BlockPos> closed = closedByWorld.get(worldName);
            if (closed != null) {
                closed.remove(pos);
            }
        } else {
            closedByWorld.computeIfAbsent(worldName, key -> ConcurrentHashMap.newKeySet()).add(pos);
        }
    }

    /**
     * Schaltet um und liefert den neuen Zustand.
     */
    public boolean toggle(String worldName, BlockPos pos) {
        boolean nowOpen = !isOpen(worldName, pos);
        setOpen(worldName, pos, nowOpen);
        return nowOpen;
    }

    /** Beim Abbauen: der Block ist weg, der Zustand darf nicht zurueckbleiben. */
    public void remove(String worldName, BlockPos pos) {
        setOpen(worldName, pos, true);
    }

    public void clear(String worldName) {
        closedByWorld.remove(worldName);
    }

    public int closedCount() {
        return closedByWorld.values().stream().mapToInt(Set::size).sum();
    }
}
