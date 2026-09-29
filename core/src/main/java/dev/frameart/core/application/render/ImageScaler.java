package dev.frameart.core.application.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Redimensiona mantendo a proporção (letterbox transparente), com redução progressiva para qualidade. */
final class ImageScaler {

    private ImageScaler() {
    }

    static int[] fit(BufferedImage source, int width, int height) {
        double scale = Math.min(width / (double) source.getWidth(), height / (double) source.getHeight());
        int drawWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int drawHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));

        BufferedImage scaled = progressiveDownscale(toArgb(source), drawWidth, drawHeight);

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            applyQualityHints(graphics);
            graphics.drawImage(scaled, (width - drawWidth) / 2, (height - drawHeight) / 2, drawWidth, drawHeight, null);
        } finally {
            graphics.dispose();
        }
        return canvas.getRGB(0, 0, width, height, null, 0, width);
    }

    /** Reduz pela metade até chegar perto do alvo, evitando serrilhado ao reduzir muito de uma vez. */
    private static BufferedImage progressiveDownscale(BufferedImage image, int targetWidth, int targetHeight) {
        BufferedImage current = image;
        while (current.getWidth() / 2 >= targetWidth && current.getHeight() / 2 >= targetHeight) {
            current = resize(current, current.getWidth() / 2, current.getHeight() / 2);
        }
        return current;
    }

    private static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        try {
            applyQualityHints(graphics);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private static BufferedImage toArgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }
        return resize(image, image.getWidth(), image.getHeight());
    }

    private static void applyQualityHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }
}
