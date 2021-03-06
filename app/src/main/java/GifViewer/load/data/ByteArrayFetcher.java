package GifViewer.load.data;

/**
 * Created by jabong on 8/7/16.
 */
import GifViewer.Priority;
import GifViewer.load.data.DataFetcher;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

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

    public void cleanup() {
    }

    public String getId() {
        return this.id;
    }

    public void cancel() {
    }
}
