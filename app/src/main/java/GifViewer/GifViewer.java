package GifViewer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import GifViewer.load.DecodeFormat;
import GifViewer.load.data.InputStreamRewinder;
import GifViewer.load.engine.Engine;
import GifViewer.load.engine.bitmap_recycle.ArrayPool;
import GifViewer.load.engine.bitmap_recycle.BitmapPool;
import GifViewer.load.engine.cache.MemoryCache;
import GifViewer.load.engine.prefill.BitmapPreFiller;
import GifViewer.load.engine.prefill.PreFillType;
import GifViewer.load.model.AssetUriLoader;
import GifViewer.load.model.ByteArrayLoader;
import GifViewer.load.model.ByteBufferEncoder;
import GifViewer.load.model.ByteBufferFileLoader;
import GifViewer.load.model.DataUrlLoader;
import GifViewer.load.model.FileLoader;
import GifViewer.load.model.GenericLoaderFactory;
import GifViewer.load.model.GlideUrl;
import GifViewer.load.model.ImageVideoWrapper;
import GifViewer.load.model.MediaStoreFileLoader;
import GifViewer.load.model.ModelLoaderFactory;
import GifViewer.load.model.ResourceLoader;
import GifViewer.load.model.StreamEncoder;
import GifViewer.load.model.StringLoader;
import GifViewer.load.model.UnitModelLoader;
import GifViewer.load.model.UriLoader;
import GifViewer.load.model.UrlUriLoader;
import GifViewer.load.model.file_descriptor.FileDescriptorFileLoader;
import GifViewer.load.model.file_descriptor.FileDescriptorResourceLoader;
import GifViewer.load.model.file_descriptor.FileDescriptorStringLoader;
import GifViewer.load.model.file_descriptor.FileDescriptorUriLoader;
import GifViewer.load.model.stream.HttpGlideUrlLoader;
import GifViewer.load.model.stream.HttpUriLoader;
import GifViewer.load.model.stream.HttpUrlGlideUrlLoader;
import GifViewer.load.model.stream.MediaStoreImageThumbLoader;
import GifViewer.load.model.stream.MediaStoreVideoThumbLoader;
import GifViewer.load.model.stream.StreamByteArrayLoader;
import GifViewer.load.model.stream.StreamFileLoader;
import GifViewer.load.model.stream.StreamResourceLoader;
import GifViewer.load.model.stream.StreamStringLoader;
import GifViewer.load.model.stream.StreamUriLoader;
import GifViewer.load.model.stream.StreamUrlLoader;
import GifViewer.load.model.stream.UrlLoader;
import GifViewer.load.resource.bitmap.BitmapDrawableDecoder;
import GifViewer.load.resource.bitmap.BitmapDrawableEncoder;
import GifViewer.load.resource.bitmap.BitmapEncoder;
import GifViewer.load.resource.bitmap.ByteBufferBitmapDecoder;
import GifViewer.load.resource.bitmap.CenterCrop;
import GifViewer.load.resource.bitmap.Downsampler;
import GifViewer.load.resource.bitmap.FileDescriptorBitmapDataLoadProvider;
import GifViewer.load.resource.bitmap.FitCenter;
import GifViewer.load.resource.bitmap.GlideBitmapDrawable;
import GifViewer.load.resource.bitmap.ImageVideoDataLoadProvider;
import GifViewer.load.resource.bitmap.StreamBitmapDataLoadProvider;
import GifViewer.load.resource.bitmap.StreamBitmapDecoder;
import GifViewer.load.resource.bitmap.VideoBitmapDecoder;
import GifViewer.load.resource.bytes.ByteBufferRewinder;
import GifViewer.load.resource.drawable.GlideDrawable;
import GifViewer.load.resource.file.FileDecoder;
import GifViewer.load.resource.file.StreamFileDataLoadProvider;
import GifViewer.load.resource.gif.ByteBufferGifDecoder;
import GifViewer.load.resource.gif.GifDrawable;
import GifViewer.load.resource.gif.GifDrawableEncoder;
import GifViewer.load.resource.gif.GifDrawableLoadProvider;
import GifViewer.load.resource.gif.GifFrameResourceDecoder;
import GifViewer.load.resource.gif.StreamGifDecoder;
import GifViewer.load.resource.gifbitmap.GifBitmapWrapper;
import GifViewer.load.resource.gifbitmap.GifBitmapWrapperTransformation;
import GifViewer.load.resource.gifbitmap.ImageVideoGifDrawableLoadProvider;
import GifViewer.load.resource.transcode.BitmapBytesTranscoder;
import GifViewer.load.resource.transcode.BitmapDrawableTranscoder;
import GifViewer.load.resource.transcode.GifBitmapWrapperDrawableTranscoder;
import GifViewer.load.resource.transcode.GifDrawableBytesTranscoder;
import GifViewer.load.resource.transcode.GlideBitmapDrawableTranscoder;
import GifViewer.load.resource.transcode.ResourceTranscoder;
import GifViewer.load.resource.transcode.TranscoderRegistry;
import GifViewer.manager.ConnectivityMonitorFactory;
import GifViewer.manager.RequestManagerRetriever;
import GifViewer.module.GlideModule;
import GifViewer.module.ManifestParser;
import GifViewer.provider.DataLoadProvider;
import GifViewer.provider.DataLoadProviderRegistry;
import GifViewer.request.RequestOptions;
import GifViewer.request.target.ImageViewTargetFactory;
import GifViewer.request.target.Target;
import GifViewer.util.Util;
/**
 * Created by jabong on 1/7/16.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class GifViewer implements ComponentCallbacks2
{
    private static final String DEFAULT_DISK_CACHE_DIR = "image_manager_disk_cache";
    private static final String TAG = "Glide";
    private static volatile GifViewer gifViewer;

    private  CenterCrop bitmapCenterCrop;
    private  FitCenter bitmapFitCenter;
    private GifBitmapWrapperTransformation drawableFitCenter;
    private Handler mainHandler;
    private DecodeFormat decodeFormat;
    private GifBitmapWrapperTransformation drawableCenterCrop;
    private final DataLoadProviderRegistry dataLoadProviderRegistry = new DataLoadProviderRegistry();
    private final Engine engine;
    private final BitmapPool bitmapPool;
    private final MemoryCache memoryCache;
    private final BitmapPreFiller bitmapPreFiller;
    private final GlideContext glideContext;
    private final Registry registry;
    private final ArrayPool arrayPool;
    private final ConnectivityMonitorFactory connectivityMonitorFactory;
    private final List<RequestManager> managers = new ArrayList<>();
    private final GenericLoaderFactory loaderFactory;
    private final TranscoderRegistry transcoderRegistry = new TranscoderRegistry();

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context A context.
     * @see #getPhotoCacheDir(android.content.Context, String)
     */

    public static File getPhotoCacheDir(Context context) {
        return getPhotoCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    <T, Z> DataLoadProvider<T, Z> buildDataProvider(Class<T> dataClass, Class<Z> decodedClass) {
        return dataLoadProviderRegistry.get(dataClass, decodedClass);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getPhotoCacheDir(android.content.Context)
     */

    public static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    /**
     * Get the singleton.
     *
     * @return the singleton
     */
    public static GifViewer get(Context context) {
        if (gifViewer == null) {
            synchronized (GifViewer.class) {
                if (gifViewer == null) {
                    Context applicationContext = context.getApplicationContext();
                    List<GlideModule> modules = new ManifestParser(applicationContext).parse();

                    GlideBuilder builder = new GlideBuilder(applicationContext);
                    for (GlideModule module : modules) {
                        module.applyOptions(applicationContext, builder);
                    }
                    gifViewer = builder.createGlide();
                    for (GlideModule module : modules) {
                        module.registerComponents(applicationContext, gifViewer.registry);
                    }
                }
            }
        }

        return gifViewer;
    }


    public static void tearDown() {
        gifViewer = null;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    GifViewer(
            Context context,
            Engine engine,
            MemoryCache memoryCache,
            BitmapPool bitmapPool,
            ArrayPool arrayPool,
            ConnectivityMonitorFactory connectivityMonitorFactory,
            int logLevel,
            RequestOptions defaultRequestOptions) {
        this.engine = engine;
        this.bitmapPool = bitmapPool;
        this.arrayPool = arrayPool;
        this.memoryCache = memoryCache;
        this.connectivityMonitorFactory = connectivityMonitorFactory;

        DecodeFormat decodeFormat = defaultRequestOptions.getOptions().get(Downsampler.DECODE_FORMAT);
        bitmapPreFiller = new BitmapPreFiller(memoryCache, bitmapPool, decodeFormat);

        final Resources resources = context.getResources();

        Downsampler downsampler =
                new Downsampler(resources.getDisplayMetrics(), bitmapPool, arrayPool);
        ByteBufferGifDecoder byteBufferGifDecoder =
                new ByteBufferGifDecoder(context, bitmapPool, arrayPool);
        registry = new Registry()
                .register(ByteBuffer.class, new ByteBufferEncoder())
                .register(InputStream.class, new StreamEncoder(arrayPool))
        /* Bitmaps */
                .append(ByteBuffer.class, Bitmap.class,
                        new ByteBufferBitmapDecoder(downsampler))
                .append(InputStream.class, Bitmap.class,
                        new StreamBitmapDecoder(downsampler, arrayPool))
                .append(ParcelFileDescriptor.class, Bitmap.class, new VideoBitmapDecoder(bitmapPool))
                .register(Bitmap.class, new BitmapEncoder())
        /* GlideBitmapDrawables */
                .append(ByteBuffer.class, BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, bitmapPool,
                                new ByteBufferBitmapDecoder(downsampler)))
                .append(InputStream.class, BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, bitmapPool,
                                new StreamBitmapDecoder(downsampler, arrayPool)))
                .append(ParcelFileDescriptor.class, BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, bitmapPool, new VideoBitmapDecoder(bitmapPool)))
                .register(BitmapDrawable.class, new BitmapDrawableEncoder(bitmapPool, new BitmapEncoder()))
        /* Gifs */
                .prepend(InputStream.class, GifDrawable.class,
                        new StreamGifDecoder(byteBufferGifDecoder, arrayPool))
                .prepend(ByteBuffer.class, GifDrawable.class, byteBufferGifDecoder)
                .register(GifDrawable.class, new GifDrawableEncoder())
        /* Gif Frames */
                .append(GifDecoder.class, GifDecoder.class, new UnitModelLoader.Factory<GifDecoder>())
                .append(GifDecoder.class, Bitmap.class, new GifFrameResourceDecoder(bitmapPool))
        /* Files */
                .register(new ByteBufferRewinder.Factory())
                .append(File.class, ByteBuffer.class, new ByteBufferFileLoader.Factory())
                .append(File.class, InputStream.class, new FileLoader.StreamFactory())
                .append(File.class, File.class, new FileDecoder())
                .append(File.class, ParcelFileDescriptor.class, new FileLoader.FileDescriptorFactory())
                .append(File.class, File.class, new UnitModelLoader.Factory<File>())
        /* Models */
                .register(new InputStreamRewinder.Factory(arrayPool))
                .append(int.class, InputStream.class, new ResourceLoader.StreamFactory(resources))
                .append(
                        int.class,
                        ParcelFileDescriptor.class,
                        new ResourceLoader.FileDescriptorFactory(resources))
                .append(Integer.class, InputStream.class, new ResourceLoader.StreamFactory(resources))
                .append(
                        Integer.class,
                        ParcelFileDescriptor.class,
                        new ResourceLoader.FileDescriptorFactory(resources))
                .append(String.class, InputStream.class, new DataUrlLoader.StreamFactory())
                .append(String.class, InputStream.class, new StringLoader.StreamFactory())
                .append(String.class, ParcelFileDescriptor.class, new StringLoader.FileDescriptorFactory())
                .append(Uri.class, InputStream.class, new HttpUriLoader.Factory())
                .append(Uri.class, InputStream.class, new AssetUriLoader.StreamFactory(context.getAssets()))
                .append(
                        Uri.class,
                        ParcelFileDescriptor.class,
                        new AssetUriLoader.FileDescriptorFactory(context.getAssets()))
                .append(Uri.class, InputStream.class, new MediaStoreImageThumbLoader.Factory(context))
                .append(Uri.class, InputStream.class, new MediaStoreVideoThumbLoader.Factory(context))
                .append(
                        Uri.class,
                        InputStream.class,
                        new UriLoader.StreamFactory(context.getContentResolver()))
                .append(Uri.class, ParcelFileDescriptor.class,
                        new UriLoader.FileDescriptorFactory(context.getContentResolver()))
                .append(Uri.class, InputStream.class, new UrlUriLoader.StreamFactory())
                .append(URL.class, InputStream.class, new UrlLoader.StreamFactory())
                .append(Uri.class, File.class, new MediaStoreFileLoader.Factory(context))
                .append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory())
                .append(byte[].class, ByteBuffer.class, new ByteArrayLoader.ByteBufferFactory())
                .append(byte[].class, InputStream.class, new ByteArrayLoader.StreamFactory())
        /* Transcoders */
                .register(Bitmap.class, BitmapDrawable.class,
                        new BitmapDrawableTranscoder(resources, bitmapPool))
                .register(Bitmap.class, byte[].class, new BitmapBytesTranscoder())
                .register(GifDrawable.class, byte[].class, new GifDrawableBytesTranscoder());

        ImageViewTargetFactory imageViewTargetFactory = new ImageViewTargetFactory();
        glideContext = new GlideContext(context, registry, imageViewTargetFactory,
                defaultRequestOptions, engine, this, logLevel);
    }

    GifViewer(Engine engine, MemoryCache memoryCache, BitmapPool bitmapPool, Context context, DecodeFormat decodeFormat) {
        this.engine = engine;
        this.bitmapPool = bitmapPool;
        this.memoryCache = memoryCache;
        this.decodeFormat = decodeFormat;
        this.loaderFactory = new GenericLoaderFactory(context);
        mainHandler = new Handler(Looper.getMainLooper());
        bitmapPreFiller = new BitmapPreFiller(memoryCache, bitmapPool, decodeFormat);

        dataLoadProviderRegistry = new DataLoadProviderRegistry();

        StreamBitmapDataLoadProvider streamBitmapLoadProvider =
                new StreamBitmapDataLoadProvider(bitmapPool, decodeFormat);
        dataLoadProviderRegistry.register(InputStream.class, Bitmap.class, streamBitmapLoadProvider);

        FileDescriptorBitmapDataLoadProvider fileDescriptorLoadProvider =
                new FileDescriptorBitmapDataLoadProvider(bitmapPool, decodeFormat);
        dataLoadProviderRegistry.register(ParcelFileDescriptor.class, Bitmap.class, fileDescriptorLoadProvider);

        ImageVideoDataLoadProvider imageVideoDataLoadProvider =
                new ImageVideoDataLoadProvider(streamBitmapLoadProvider, fileDescriptorLoadProvider);
        dataLoadProviderRegistry.register(ImageVideoWrapper.class, Bitmap.class, imageVideoDataLoadProvider);

        GifDrawableLoadProvider gifDrawableLoadProvider =
                new GifDrawableLoadProvider(context, bitmapPool);
        dataLoadProviderRegistry.register(InputStream.class, GifDrawable.class, gifDrawableLoadProvider);

        dataLoadProviderRegistry.register(ImageVideoWrapper.class, GifBitmapWrapper.class,
                new ImageVideoGifDrawableLoadProvider(imageVideoDataLoadProvider, gifDrawableLoadProvider, bitmapPool));

        dataLoadProviderRegistry.register(InputStream.class, File.class, new StreamFileDataLoadProvider());

        register(File.class, ParcelFileDescriptor.class, new FileDescriptorFileLoader.Factory());
        register(File.class, InputStream.class, new StreamFileLoader.Factory());
        register(int.class, ParcelFileDescriptor.class, new FileDescriptorResourceLoader.Factory());
        register(int.class, InputStream.class, new StreamResourceLoader.Factory());
        register(Integer.class, ParcelFileDescriptor.class, new FileDescriptorResourceLoader.Factory());
        register(Integer.class, InputStream.class, new StreamResourceLoader.Factory());
        register(String.class, ParcelFileDescriptor.class, new FileDescriptorStringLoader.Factory());
        register(String.class, InputStream.class, new StreamStringLoader.Factory());
        register(Uri.class, ParcelFileDescriptor.class, new FileDescriptorUriLoader.Factory());
        register(Uri.class, InputStream.class, new StreamUriLoader.Factory());
        register(URL.class, InputStream.class, new StreamUrlLoader.Factory());
        register(GlideUrl.class, InputStream.class, new HttpUrlGlideUrlLoader.Factory());
        register(byte[].class, InputStream.class, new StreamByteArrayLoader.Factory());

        transcoderRegistry.register(Bitmap.class, GlideBitmapDrawable.class,
                new GlideBitmapDrawableTranscoder(context.getResources(), bitmapPool));
        transcoderRegistry.register(GifBitmapWrapper.class, GlideDrawable.class,
                new GifBitmapWrapperDrawableTranscoder(
                        new GlideBitmapDrawableTranscoder(context.getResources(), bitmapPool)));

        bitmapCenterCrop = new CenterCrop(bitmapPool);
        drawableCenterCrop = new GifBitmapWrapperTransformation(bitmapPool, bitmapCenterCrop);

        bitmapFitCenter = new FitCenter(bitmapPool);
        drawableFitCenter = new GifBitmapWrapperTransformation(bitmapPool, bitmapFitCenter);
    }

    /**
     * Returns the {@link GifViewer.load.engine.bitmap_recycle.BitmapPool} used to
     * temporarily store {@link android.graphics.Bitmap}s so they can be reused to avoid garbage
     * collections.
     *
     * <p> Note - Using this pool directly can lead to undefined behavior and strange drawing errors.
     * Any {@link android.graphics.Bitmap} added to the pool must not be currently in use in any other
     * part of the application. Any {@link android.graphics.Bitmap} added to the pool must be removed
     * from the pool before it is added a second time. </p>
     *
     * <p> Note - To make effective use of the pool, any {@link android.graphics.Bitmap} removed from
     * the pool must eventually be re-added. Otherwise the pool will eventually empty and will not
     * serve any useful purpose. </p>
     *
     * <p> The primary reason this object is exposed is for use in custom
     * {@link GifViewer.load.ResourceDecoder}s and
     * {@link GifViewer.load.Transformation}s. Use outside of these classes is not generally
     * recommended. </p>
     */
    public BitmapPool getBitmapPool() {
        return bitmapPool;
    }

    public ArrayPool getArrayPool() {
        return arrayPool;
    }

    /**
     * @return The context associated with this instance.
     */
    public Context getContext() {
        return glideContext.getBaseContext();
    }

    ConnectivityMonitorFactory getConnectivityMonitorFactory() {
        return connectivityMonitorFactory;
    }

    GlideContext getGlideContext() {
        return glideContext;
    }
    public <T, Y> void register(Class<T> modelClass, Class<Y> resourceClass, ModelLoaderFactory<T, Y> factory) {
        ModelLoaderFactory<T, Y> removed = loaderFactory.register(modelClass, resourceClass, factory);
        if (removed != null) {
            removed.teardown();
        }
    }

    /**
     * Pre-fills the {@link GifViewer.load.engine.bitmap_recycle.BitmapPool} using the given
     * sizes.
     *
     * <p> Enough Bitmaps are added to completely fill the pool, so most or all of the Bitmaps
     * currently in the pool will be evicted. Bitmaps are allocated according to the weights of the
     * given sizes, where each size gets (weight / prefillWeightSum) percent of the pool to fill.
     * </p>
     *
     * <p> Note - Pre-filling is done asynchronously using and
     * {@link android.os.MessageQueue.IdleHandler}. Any currently running pre-fill will be cancelled
     * and replaced by a call to this method. </p>
     *
     * <p> This method should be used with caution, overly aggressive pre-filling is substantially
     * worse than not pre-filling at all. Pre-filling should only be started in onCreate to avoid
     * constantly clearing and re-filling the
     * {@link GifViewer.load.engine.bitmap_recycle.BitmapPool}. Rotation should be carefully
     * considered as well. It may be worth calling this method only when no saved instance state
     * exists so that pre-filling only happens when the Activity is first created, rather than on
     * every rotation. </p>
     *
     * @param bitmapAttributeBuilders The list of
     * {@link GifViewer.load.engine.prefill.PreFillType.Builder Builders} representing
     * individual sizes and configurations of {@link android.graphics.Bitmap}s to be pre-filled.
     */
    public void preFillBitmapPool(PreFillType.Builder... bitmapAttributeBuilders) {
        bitmapPreFiller.preFill(bitmapAttributeBuilders);
    }

    /**
     * Clears as much memory as possible.
     *
     * @see android.content.ComponentCallbacks#onLowMemory()
     * @see android.content.ComponentCallbacks2#onLowMemory()
     */
    public void clearMemory() {
        // Engine asserts this anyway when removing resources, fail faster and consistently
        Util.assertMainThread();
        // memory cache needs to be cleared before bitmap pool to clear re-pooled Bitmaps too. See #687.
        memoryCache.clearMemory();
        bitmapPool.clearMemory();
        arrayPool.clearMemory();
    }

    /**
     * Clears some memory with the exact amount depending on the given level.
     *
     * @see android.content.ComponentCallbacks2#onTrimMemory(int)
     */
    public void trimMemory(int level) {
        // Engine asserts this anyway when removing resources, fail faster and consistently
        Util.assertMainThread();
        // memory cache needs to be trimmed before bitmap pool to trim re-pooled Bitmaps too. See #687.
        memoryCache.trimMemory(level);
        bitmapPool.trimMemory(level);
        arrayPool.trimMemory(level);
    }

    /**
     * Clears disk cache.
     *
     * <p>
     *     This method should always be called on a background thread, since it is a blocking call.
     * </p>
     */
    public void clearDiskCache() {
        Util.assertBackgroundThread();
        engine.clearDiskCache();
    }

    /**
     * Adjusts Glide's current and maximum memory usage based on the given {@link MemoryCategory}.
     *
     * <p> The default {@link MemoryCategory} is {@link MemoryCategory#NORMAL}.
     * {@link MemoryCategory#HIGH} increases Glide's maximum memory usage by up to 50% and
     * {@link MemoryCategory#LOW} decreases Glide's maximum memory usage by 50%. This method should be
     * used to temporarily increase or decrease memory usage for a single Activity or part of the app.
     * Use {@link GlideBuilder#setMemoryCache(MemoryCache)} to put a permanent memory size if you want
     * to change the default. </p>
     */
    public void setMemoryCategory(MemoryCategory memoryCategory) {
        // Engine asserts this anyway when removing resources, fail faster and consistently
        Util.assertMainThread();
        // memory cache needs to be trimmed before bitmap pool to trim re-pooled Bitmaps too. See #687.
        memoryCache.setSizeMultiplier(memoryCategory.getMultiplier());
        bitmapPool.setSizeMultiplier(memoryCategory.getMultiplier());
    }

    /**
     * Begin a load with Glide by passing in a context.
     *
     * <p> Any requests started using a context will only have the application level options applied
     * and will not be started or stopped based on lifecycle events. In general, loads should be
     * started at the level the result will be used in. If the resource will be used in a view in a
     * child fragment, the load should be started with {@link #with(android.app.Fragment)}} using that
     * child fragment. Similarly, if the resource will be used in a view in the parent fragment, the
     * load should be started with {@link #with(android.app.Fragment)} using the parent fragment. In
     * the same vein, if the resource will be used in a view in an activity, the load should be
     * started with {@link #with(android.app.Activity)}}. </p>
     *
     * <p> This method is appropriate for resources that will be used outside of the normal fragment
     * or activity lifecycle (For example in services, or for notification thumbnails). </p>
     *

     */
    public static RequestManager with(Context context) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(context);
    }

    /**
     * Begin a load with Glide that will be tied to the given {@link android.app.Activity}'s lifecycle
     * and that uses the given {@link Activity}'s default options.
     *
     * @param activity The activity to use.
     * @return A RequestManager for the given activity that can be used to start a load.
     */
    public static RequestManager with(Activity activity) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(activity);
    }

    /**
     * Begin a load with Glide that will tied to the give
     *
     * @param activity The activity to use.
     * @return A RequestManager for the given FragmentActivity that can be used to start a load.
     */
    public static RequestManager with(FragmentActivity activity) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(activity);
    }

    /**
     * Begin a load with Glide that will be tied to the given {@link android.app.Fragment}'s lifecycle
     * and that uses the given {@link android.app.Fragment}'s default options.
     *
     * @param fragment The fragment to use.
     * @return A RequestManager for the given Fragment that can be used to start a load.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static RequestManager with(Fragment fragment) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(fragment);
    }

    /**
     * Begin a load with Glide that will be tied to the given
     *
     *
     * @param fragment The fragment to use.
     * @return A RequestManager for the given Fragment that can be used to start a load.
     */
    public static RequestManager with(Fragment fragment) {
        RequestManagerRetriever retriever = RequestManagerRetriever.get();
        return retriever.get(fragment);
    }

    public Registry getRegistry() {
        return registry;
    }

    void removeFromManagers(Target<?> target) {
        synchronized (managers) {
            for (RequestManager requestManager : managers) {
                if (requestManager.untrack(target)) {
                    return;
                }
            }
        }
        throw new IllegalStateException("Failed to remove target from managers");
    }

    void registerRequestManager(RequestManager requestManager) {
        synchronized (managers) {
            if (managers.contains(requestManager)) {
                throw new IllegalStateException("Cannot register already registered manager");
            }
            managers.add(requestManager);
        }
    }

    void unregisterRequestManager(RequestManager requestManager) {
        synchronized (managers) {
            if (!managers.contains(requestManager)) {
                throw new IllegalStateException("Cannot register not yet registered manager");
            }
            managers.remove(requestManager);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        trimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Do nothing.
    }

    @Override
    public void onLowMemory() {
        clearMemory();
    }

    Engine getEngine() {
        return engine;
    }

    CenterCrop getBitmapCenterCrop() {
        return bitmapCenterCrop;
    }

    FitCenter getBitmapFitCenter() {
        return bitmapFitCenter;
    }

    GifBitmapWrapperTransformation getDrawableCenterCrop() {
        return drawableCenterCrop;
    }

    GifBitmapWrapperTransformation getDrawableFitCenter() {
        return drawableFitCenter;
    }

    Handler getMainHandler() {
        return mainHandler;
    }

    DecodeFormat getDecodeFormat() {
        return decodeFormat;
    }

    <Z, R> ResourceTranscoder<Z, R> buildTranscoder(Class<Z> decodedClass, Class<R> transcodedClass) {
        return transcoderRegistry.get(decodedClass, transcodedClass);
    }
}

