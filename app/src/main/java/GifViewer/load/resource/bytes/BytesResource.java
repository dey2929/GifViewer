package GifViewer.load.resource.bytes;

import GifViewer.load.engine.Resource;
import GifViewer.util.Preconditions;

/**
 * An {@link GifViewer.load.engine.Resource} wrapping a byte array.
 */
public class BytesResource implements Resource<byte[]> {
  private final byte[] bytes;

  public BytesResource(byte[] bytes) {
    this.bytes = Preconditions.checkNotNull(bytes);
  }

  @Override
  public Class<byte[]> getResourceClass() {
    return byte[].class;
  }

  @Override
  public byte[] get() {
    return bytes;
  }

  @Override
  public int getSize() {
    return bytes.length;
  }

  @Override
  public void recycle() {
    // Do nothing.
  }
}
