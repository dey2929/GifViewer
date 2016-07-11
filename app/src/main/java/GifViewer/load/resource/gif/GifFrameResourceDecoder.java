package GifViewer.load.resource.gif;

import android.graphics.Bitmap;

import GifViewer.gifdecoder.GifDecoder;
import GifViewer.load.Options;
import GifViewer.load.ResourceDecoder;
import GifViewer.load.engine.Resource;
import GifViewer.load.engine.bitmap_recycle.BitmapPool;
import GifViewer.load.resource.bitmap.BitmapResource;

/**
 * Decodes {@link Bitmap}s from {@link GifDecoder}s representing a particular frame of a particular
 * GIF image.
 */
public final class GifFrameResourceDecoder implements ResourceDecoder<GifDecoder, Bitmap> {
  private final BitmapPool bitmapPool;

  public GifFrameResourceDecoder(BitmapPool bitmapPool) {
    this.bitmapPool = bitmapPool;
  }

  @Override
  public boolean handles(GifDecoder source, Options options) {
    return true;
  }

  @Override
  public Resource<Bitmap> decode(GifDecoder source, int width, int height,
      Options options) {
    Bitmap bitmap = source.getNextFrame();
    return BitmapResource.obtain(bitmap, bitmapPool);
  }
}
