package GifViewer.load.resource.bitmap;

import GifViewer.load.engine.bitmap_recycle.BitmapPool;
import GifViewer.load.resource.drawable.DrawableResource;
import GifViewer.util.Util;

/**
 * A resource wrapper for {@link GifViewer.load.resource.bitmap.GlideBitmapDrawable}.
 */
public class GlideBitmapDrawableResource extends DrawableResource<GlideBitmapDrawable> {
    private final BitmapPool bitmapPool;

    public GlideBitmapDrawableResource(GlideBitmapDrawable drawable, BitmapPool bitmapPool) {
        super(drawable);
        this.bitmapPool = bitmapPool;
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
