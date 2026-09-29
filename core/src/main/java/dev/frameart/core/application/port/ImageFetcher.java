package dev.frameart.core.application.port;

/** Obtém os bytes brutos de uma imagem a partir de uma origem (URL, arquivo...). */
public interface ImageFetcher {

    boolean supports(String source);

    byte[] fetch(String source);
}
