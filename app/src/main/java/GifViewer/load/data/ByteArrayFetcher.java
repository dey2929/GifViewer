package GifViewer.load.data;

/**
 * Created by jabong on 8/7/16.
 */
import android.renderscript.RenderScript;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import GifViewer.Priority;
import GifViewer.load.DataSource;

public class ByteArrayFetcher implements DataFetcher<InputStream> {
    private final byte[] bytes;
    private final String id;

    public ByteArrayFetcher(byte[] bytes, String id) {
        this.bytes = bytes;
        this.id = id;
    }

    public InputStream loadData(Priority priority) {
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public void loadData(RenderScript.Priority priority, DataCallback<? super InputStream> callback) {

    }

    public void cleanup() {
    }

    public String getId() {
        return this.id;
    }

    public void cancel() {
    }

    @Override
    public Class<InputStream> getDataClass() {
        return null;
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }
}
