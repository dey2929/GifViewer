package GifViewer.load.resource.bitmap;

import android.graphics.drawable.BitmapDrawable;

import GifViewer.load.engine.bitmap_recycle.BitmapPool;
import GifViewer.load.resource.drawable.DrawableResource;
import GifViewer.util.Util;

/**
 * A {@link GifViewer.load.engine.Resource} that wraps an
 * {@link BitmapDrawable}
 *
 * <p> This class ensures that every call to {@link #get()}} always returns a new
 * {@link BitmapDrawable} to avoid rendering issues if used in multiple
 * views and is also responsible for returning the underlying {@link android.graphics.Bitmap} to the
 * given {@link GifViewer.load.engine.bitmap_recycle.BitmapPool} when the resource is
 * recycled. </p>
 */
public class BitmapDrawableResource extends DrawableResource<BitmapDrawable> {
  private final BitmapPool bitmapPool;

  public BitmapDrawableResource(BitmapDrawable drawable, BitmapPool bitmapPool) {
    super(drawable);
    this.bitmapPool = bitmapPool;
  }

  @Override
  public Class<BitmapDrawable> getResourceClass() {
    return BitmapDrawable.class;
  }

  @Override
  public int getSize() {
    return Util.getBitmapByteSize(drawable.getBitmap());
  }

  @Override
  public void recycle() {
    bitmapPool.put(drawable.getBitmap());
  }
}
