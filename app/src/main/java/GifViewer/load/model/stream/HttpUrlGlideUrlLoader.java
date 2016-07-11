package GifViewer.load.model.stream;

/**
 * Created by jabong on 7/7/16.
 */
import android.content.Context;
import GifViewer.load.data.DataFetcher;
import GifViewer.load.data.HttpUrlFetcher;
import GifViewer.load.model.GenericLoaderFactory;
import GifViewer.load.model.GlideUrl;
import GifViewer.load.model.ModelCache;
import GifViewer.load.model.ModelLoader;
import GifViewer.load.model.ModelLoaderFactory;
import java.io.InputStream;

public class HttpUrlGlideUrlLoader implements ModelLoader<GlideUrl, InputStream> {
    private final ModelCache<GlideUrl, GlideUrl> modelCache;

    public HttpUrlGlideUrlLoader() {
        this((ModelCache)null);
    }

    public HttpUrlGlideUrlLoader(ModelCache<GlideUrl, GlideUrl> modelCache) {
        this.modelCache = modelCache;
    }

    public DataFetcher<InputStream> getResourceFetcher(GlideUrl model, int width, int height) {
        GlideUrl url = model;
        if(this.modelCache != null) {
            url = (GlideUrl)this.modelCache.get(model, 0, 0);
            if(url == null) {
                this.modelCache.put(model, 0, 0, model);
                url = model;
            }
        }

        return new HttpUrlFetcher(url);
    }

    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private final ModelCache<GlideUrl, GlideUrl> modelCache = new ModelCache(500);

        public Factory() {
        }

        public ModelLoader<GlideUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new HttpUrlGlideUrlLoader(this.modelCache);
        }

        public void teardown() {
        }
    }
}
