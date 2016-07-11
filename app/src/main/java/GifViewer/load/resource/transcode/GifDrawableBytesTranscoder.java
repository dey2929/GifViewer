package GifViewer.load.resource.transcode;

import GifViewer.load.engine.Resource;
import GifViewer.load.resource.bytes.BytesResource;
import GifViewer.load.resource.gif.GifDrawable;
import GifViewer.util.ByteBufferUtil;

import java.nio.ByteBuffer;

/**
 * An {@link GifViewer.load.resource.transcode.ResourceTranscoder} that converts {@link
 * GifViewer.load.resource.gif.GifDrawable} into bytes by obtaining the original bytes of
 * the GIF from the {@link GifViewer.load.resource.gif.GifDrawable}.
 */
public class GifDrawableBytesTranscoder implements ResourceTranscoder<GifDrawable, byte[]> {
  @Override
  public Resource<byte[]> transcode(Resource<GifDrawable> toTranscode) {
    GifDrawable gifData = toTranscode.get();
    ByteBuffer byteBuffer = gifData.getBuffer();
    return new BytesResource(ByteBufferUtil.toBytes(byteBuffer));
  }
}
