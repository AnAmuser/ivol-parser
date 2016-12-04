package dk.anamuser.ivolparser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImgUtils {

	public static int[] findSubimage(BufferedImage im1, BufferedImage im2) throws IOException {
		int w1 = im1.getWidth();
		int h1 = im1.getHeight();
		int w2 = im2.getWidth();
		int h2 = im2.getHeight();
		assert (w2 <= w1 && h2 <= h1);
		int bestX = -1;
		int bestY = -1;
		for (int x = 0; x <= w1 - w2; x++) {
			for (int y = 0; y <= h1 - h2; y++) {
				BufferedImage subimage = im1.getSubimage(x, y, w2, h2);
				if (imagesEqual(subimage, im2)) {
					bestX = x;
					bestY = y;
				}
			}
		}

		if (bestX < 0 || bestY < 0) {
			return null;
		}

		return new int[]{bestX, bestY};
	}

	public static boolean imagesEqual(BufferedImage img1, BufferedImage img2) {
		if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
			for (int x = 0; x < img1.getWidth(); x++) {
				for (int y = 0; y < img1.getHeight(); y++) {
					if (img1.getRGB(x, y) != img2.getRGB(x, y))
						return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public static boolean isAllWhite(BufferedImage img) {
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				if (img.getRGB(j, i) != Color.WHITE.getRGB()) {
					return false;
				}
			}
		}
		return true;
	}

	public static BufferedImage trim(BufferedImage img) {
		int widthRight = getTrimmedRight(img);
		int widthLeft = getTrimmedLeft(img);
		int heightHigh = getTrimmedHeightHigh(img);
		int heightLow = getTrimmedHeightLow(img);

		return img.getSubimage(widthLeft, heightLow, widthRight - widthLeft + 1, heightHigh - heightLow + 1);
	}

	private static int getTrimmedLeft(BufferedImage img) {
		int trimmedWidth = Integer.MAX_VALUE;

		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				if (img.getRGB(j, i) != Color.WHITE.getRGB() && j < trimmedWidth) {
					trimmedWidth = j;
					break;
				}
			}
		}

		return trimmedWidth;
	}

	private static int getTrimmedRight(BufferedImage img) {
		int trimmedWidth = 0;

		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = img.getWidth() - 1; j >= 0; j--) {
				if (img.getRGB(j, i) != Color.WHITE.getRGB() && j > trimmedWidth) {
					trimmedWidth = j;
					break;
				}
			}
		}

		return trimmedWidth;
	}

	private static int getTrimmedHeightHigh(BufferedImage img) {
		int trimmedHeight = 0;

		for (int i = 0; i < img.getWidth() - 1; i++) {
			for (int j = img.getHeight() - 1; j >= 0; j--) {
				if (img.getRGB(i, j) != Color.WHITE.getRGB() && j > trimmedHeight) {
					trimmedHeight = j;
					break;
				}
			}
		}

		return trimmedHeight;
	}

	private static int getTrimmedHeightLow(BufferedImage img) {
		int trimmedHeight = Integer.MAX_VALUE;

		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				if (img.getRGB(i, j) != Color.WHITE.getRGB() && j < trimmedHeight) {
					trimmedHeight = j;
					break;
				}
			}
		}

		return trimmedHeight;
	}
}