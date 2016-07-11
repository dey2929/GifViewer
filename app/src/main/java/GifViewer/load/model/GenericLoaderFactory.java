package GifViewer.load.model;

/**
 * Created by jabong on 7/7/16.
 */
import android.content.Context;
import GifViewer.load.data.DataFetcher;
import GifViewer.load.model.ModelLoader;
import GifViewer.load.model.ModelLoaderFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GenericLoaderFactory {
    private final Map<Class, Map<Class, ModelLoaderFactory>> modelClassToResourceFactories = new HashMap();
    private final Map<Class, Map<Class, ModelLoader>> cachedModelLoaders = new HashMap();
    private static final ModelLoader NULL_MODEL_LOADER = new ModelLoader() {
        public DataFetcher getResourceFetcher(Object model, int width, int height) {
            throw new NoSuchMethodError("This should never be called!");
        }

        public String toString() {
            return "NULL_MODEL_LOADER";
        }
    };
    private final Context context;

    public GenericLoaderFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized <T, Y> ModelLoaderFactory<T, Y> unregister(Class<T> modelClass, Class<Y> resourceClass) {
        this.cachedModelLoaders.clear();
        ModelLoaderFactory result = null;
        Map resourceToFactories = (Map)this.modelClassToResourceFactories.get(modelClass);
        if(resourceToFactories != null) {
            result = (ModelLoaderFactory)resourceToFactories.remove(resourceClass);
        }

        return result;
    }

    public synchronized <T, Y> ModelLoaderFactory<T, Y> register(Class<T> modelClass, Class<Y> resourceClass, ModelLoaderFactory<T, Y> factory) {
        this.cachedModelLoaders.clear();
        Object resourceToFactories = (Map)this.modelClassToResourceFactories.get(modelClass);
        if(resourceToFactories == null) {
            resourceToFactories = new HashMap();
            this.modelClassToResourceFactories.put(modelClass, resourceToFactories);
        }

        ModelLoaderFactory previous = (ModelLoaderFactory)((Map)resourceToFactories).put(resourceClass, factory);
        if(previous != null) {
            Iterator i$ = this.modelClassToResourceFactories.values().iterator();

            while(i$.hasNext()) {
                Map factories = (Map)i$.next();
                if(factories.containsValue(previous)) {
                    previous = null;
                    break;
                }
            }
        }

        return previous;
    }

    /** @deprecated */
    @Deprecated
    public synchronized <T, Y> ModelLoader<T, Y> buildModelLoader(Class<T> modelClass, Class<Y> resourceClass, Context context) {
        return this.buildModelLoader(modelClass, resourceClass);
    }

    public synchronized <T, Y> ModelLoader<T, Y> buildModelLoader(Class<T> modelClass, Class<Y> resourceClass) {
        ModelLoader result = this.getCachedLoader(modelClass, resourceClass);
        if(result != null) {
            return NULL_MODEL_LOADER.equals(result)?null:result;
        } else {
            ModelLoaderFactory factory = this.getFactory(modelClass, resourceClass);
            if(factory != null) {
                result = factory.build(this.context, this);
                this.cacheModelLoader(modelClass, resourceClass, result);
            } else {
                this.cacheNullLoader(modelClass, resourceClass);
            }

            return result;
        }
    }

    private <T, Y> void cacheNullLoader(Class<T> modelClass, Class<Y> resourceClass) {
        this.cacheModelLoader(modelClass, resourceClass, NULL_MODEL_LOADER);
    }

    private <T, Y> void cacheModelLoader(Class<T> modelClass, Class<Y> resourceClass, ModelLoader<T, Y> modelLoader) {
        Object resourceToLoaders = (Map)this.cachedModelLoaders.get(modelClass);
        if(resourceToLoaders == null) {
            resourceToLoaders = new HashMap();
            this.cachedModelLoaders.put(modelClass, resourceToLoaders);
        }

        ((Map)resourceToLoaders).put(resourceClass, modelLoader);
    }

    private <T, Y> ModelLoader<T, Y> getCachedLoader(Class<T> modelClass, Class<Y> resourceClass) {
        Map resourceToLoaders = (Map)this.cachedModelLoaders.get(modelClass);
        ModelLoader result = null;
        if(resourceToLoaders != null) {
            result = (ModelLoader)resourceToLoaders.get(resourceClass);
        }

        return result;
    }

    private <T, Y> ModelLoaderFactory<T, Y> getFactory(Class<T> modelClass, Class<Y> resourceClass) {
        Map resourceToFactories = (Map)this.modelClassToResourceFactories.get(modelClass);
        ModelLoaderFactory result = null;
        if(resourceToFactories != null) {
            result = (ModelLoaderFactory)resourceToFactories.get(resourceClass);
        }

        if(result == null) {
            Iterator i$ = this.modelClassToResourceFactories.keySet().iterator();

            while(i$.hasNext()) {
                Class registeredModelClass = (Class)i$.next();
                if(registeredModelClass.isAssignableFrom(modelClass)) {
                    Map currentResourceToFactories = (Map)this.modelClassToResourceFactories.get(registeredModelClass);
                    if(currentResourceToFactories != null) {
                        result = (ModelLoaderFactory)currentResourceToFactories.get(resourceClass);
                        if(result != null) {
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }
}
