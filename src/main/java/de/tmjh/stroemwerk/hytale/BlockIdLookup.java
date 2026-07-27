package de.tmjh.stroemwerk.hytale;

import de.tmjh.stroemwerk.platform.BlockIds;
import java.util.ArrayList;
import java.util.List;

/**
 * Haelt die Block-IDs und loest sie beim ersten Bedarf auf.
 *
 * <p>Beim Start des Plugins stehen die Asset-Packs noch nicht zwingend bereit -
 * eine Aufloesung in {@code setup()} liefert dann fuer alle Bauteile
 * {@link BlockIds#UNKNOWN}, und der Mod haelt anschliessend jeden Block fuer
 * Luft. Statt auf den richtigen Zeitpunkt zu wetten, wird hier so lange bei
 * jedem Zugriff neu gesucht, bis es klappt. Danach steht das Ergebnis fest.
 */
public final class BlockIdLookup {

    private volatile BlockIds resolved;
    private volatile List<String> missing = List.of();
    private int attempts;

    /**
     * Die IDs, notfalls nach einem neuen Anlauf.
     */
    public BlockIds get() {
        BlockIds current = resolved;
        if (current != null) {
            return current;
        }
        return resolveOnce();
    }

    private synchronized BlockIds resolveOnce() {
        if (resolved != null) {
            return resolved;
        }

        List<String> notFound = new ArrayList<>();
        BlockIds ids = BlockIdResolver.resolve(notFound::add);
        attempts++;
        missing = List.copyOf(notFound);

        // Erst festhalten, wenn wirklich alles gefunden wurde. Sonst bliebe ein
        // halb geladener Zustand fuer immer bestehen.
        if (notFound.isEmpty()) {
            resolved = ids;
        }
        return ids;
    }

    /**
     * Ob alle vier Bauteile gefunden wurden.
     */
    public boolean isComplete() {
        return resolved != null;
    }

    /**
     * Bauteile, die beim letzten Versuch gefehlt haben.
     */
    public List<String> missing() {
        return missing;
    }

    public int attempts() {
        return attempts;
    }
}
