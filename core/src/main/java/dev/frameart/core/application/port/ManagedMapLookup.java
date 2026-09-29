package dev.frameart.core.application.port;

/** Consulta rápida (O(1)) para saber se um ID de mapa pertence a uma imagem do plugin. */
public interface ManagedMapLookup {

    boolean isManaged(int mapId);
}
