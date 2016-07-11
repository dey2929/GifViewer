
package GifViewer.load.resource.transcode;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import GifViewer.GifViewer;
import GifViewer.load.engine.Resource;
import GifViewer.load.engine.bitmap_recycle.BitmapPool;
import GifViewer.load.resource.bitmap.GlideBitmapDrawable;
import GifViewer.load.resource.bitmap.GlideBitmapDrawableResource;

/**
 * An {@link GifViewer.load.resource.transcode.ResourceTranscoder} that converts
 * {@link android.graphics.Bitmap}s into {@link android.graphics.drawable.BitmapDrawable}s.
 */
public class GlideBitmapDrawableTranscoder implements ResourceTranscoder<Bitmap, GlideBitmapDrawable> {
    private final Resources resources;
    private final BitmapPool bitmapPool;

    public GlideBitmapDrawableTranscoder(Context context) {
        this(context.getResources(), GifViewer.get(context).getBitmapPool());
    }

    public GlideBitmapDrawableTranscoder(Resources resources, BitmapPool bitmapPool) {
        this.resources = resources;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<GlideBitmapDrawable> transcode(Resource<Bitmap> toTranscode) {
        GlideBitmapDrawable drawable = new GlideBitmapDrawable(resources, toTranscode.get());
        return new GlideBitmapDrawableResource(drawable, bitmapPool);
    }


    public String getId() {
        return "GlideBitmapDrawableTranscoder.GifViewer.load.resource.transcode";
    }
}