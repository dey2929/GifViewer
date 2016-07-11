package GifViewer.load.resource.bitmap;

import android.graphics.Bitmap;

import GifViewer.load.DecodeFormat;
import GifViewer.provider.DataLoadProvider;
import GifViewer.load.Encoder;
import GifViewer.load.ResourceDecoder;
import GifViewer.load.ResourceEncoder;
import GifViewer.load.engine.bitmap_recycle.BitmapPool;
import GifViewer.load.model.StreamEncoder;
import GifViewer.load.resource.file.FileToStreamDecoder;

import java.io.File;
import java.io.InputStream;

/**
 * An {@link GifViewer.provider.DataLoadProvider} that provides decoders and encoders for decoding and caching
 * {@link android.graphics.Bitmap}s using {@link java.io.InputStream} data.
 */
public class StreamBitmapDataLoadProvider implements DataLoadProvider<InputStream, Bitmap> {
    private final StreamBitmapDecoder decoder;
    private final BitmapEncoder encoder;
    private final StreamEncoder sourceEncoder;
    private final FileToStreamDecoder<Bitmap> cacheDecoder;

    public StreamBitmapDataLoadProvider(BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        sourceEncoder = new StreamEncoder();
        decoder = new StreamBitmapDecoder(bitmapPool, decodeFormat);
        encoder = new BitmapEncoder();
        cacheDecoder = new FileToStreamDecoder<Bitmap>(decoder);
    }

    @Override
    public ResourceDecoder<File, Bitmap> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<InputStream, Bitmap> getSourceDecoder() {
        return decoder;
    }

    @Override
    public Encoder<InputStream> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<Bitmap> getEncoder() {
        return encoder;
    }
}
