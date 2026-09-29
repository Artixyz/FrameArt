package dev.frameart.plugin.command.sub;

import dev.frameart.core.application.usecase.QueryImagesUseCase;

import java.util.List;

/** Sugestões de tab-complete com os nomes das imagens existentes. */
final class ImageNames {

    private ImageNames() {
    }

    static List<String> of(QueryImagesUseCase query, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return query.list().stream().map(placement -> placement.id().value()).toList();
    }
}
