package dev.frameart.core.application.port;

import java.awt.image.BufferedImage;

/** Decodifica bytes (PNG, JPEG, GIF, BMP...) em uma imagem. */
public interface ImageDecoder {

    BufferedImage decode(byte[] data);
}
