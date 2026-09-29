package dev.frameart.core.application.port;

import dev.frameart.core.domain.ImagePlacement;
import dev.frameart.core.domain.PlacementArea;
import dev.frameart.core.domain.RenderedImage;

import java.util.List;

/** Porta para o mundo do jogo. Todos os métodos devem ser chamados na thread principal. */
public interface WorldDisplay {

    /** Lança {@link dev.frameart.core.domain.FrameArtException} se a área não puder receber molduras. */
    void ensurePlaceable(PlacementArea area);

    /** Cria mapas e molduras; retorna os IDs de mapa em ordem row-major. */
    List<Integer> place(PlacementArea area, RenderedImage image);

    /** Reconecta os renderizadores aos mapas existentes; retorna quantos tiles foram restaurados. */
    int attach(ImagePlacement placement, RenderedImage image);

    void remove(ImagePlacement placement);
}
