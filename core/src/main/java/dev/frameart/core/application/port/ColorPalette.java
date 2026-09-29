package dev.frameart.core.application.port;

/** Paleta de cores de mapa da versão do servidor em execução. */
public interface ColorPalette {

    /** Identificador estável da paleta; muda quando a versão do jogo muda (usado na chave de cache). */
    String id();

    /** Índice de cor mais próximo para um pixel ARGB (transparente quando alpha &lt; 128). */
    byte match(int argb);

    /** Cor RGB real de um índice da paleta. */
    int rgb(byte index);
}
