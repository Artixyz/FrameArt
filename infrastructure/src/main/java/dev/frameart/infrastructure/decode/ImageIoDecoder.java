package dev.frameart.infrastructure.decode;

import dev.frameart.core.application.port.ImageDecoder;
import dev.frameart.core.domain.FrameArtException;
import dev.frameart.core.domain.FrameArtException.Reason;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Decodifica com ImageIO (PNG, JPEG, GIF – primeiro quadro, BMP).
 * Lê as dimensões antes de decodificar para evitar "decompression bombs".
 */
public final class ImageIoDecoder implements ImageDecoder {

    private final int maxDimension;

    public ImageIoDecoder(int maxDimension) {
        this.maxDimension = maxDimension;
    }

    @Override
    public BufferedImage decode(byte[] data) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            Iterator<ImageReader> readers = input == null ? null : ImageIO.getImageReaders(input);
            if (readers == null || !readers.hasNext()) {
                throw new FrameArtException(Reason.INVALID_IMAGE, "unsupported format");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width > maxDimension || height > maxDimension) {
                    throw new FrameArtException(Reason.IMAGE_TOO_LARGE, maxDimension + "x" + maxDimension + " px");
                }
                BufferedImage image = reader.read(0);
                if (image == null || image.getWidth() < 1 || image.getHeight() < 1) {
                    throw new FrameArtException(Reason.INVALID_IMAGE, "empty image");
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new FrameArtException(Reason.INVALID_IMAGE, e.getMessage(), e);
        }
    }
}
