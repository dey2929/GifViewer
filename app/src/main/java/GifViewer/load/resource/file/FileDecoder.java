package GifViewer.load.resource.file;

import GifViewer.load.Options;
import GifViewer.load.ResourceDecoder;
import GifViewer.load.engine.Resource;

import java.io.File;

/**
 * A simple {@link GifViewer.load.ResourceDecoder} that creates resource for a given {@link
 * File}.
 */
public class FileDecoder implements ResourceDecoder<File, File> {

  @Override
  public boolean handles(File source, Options options) {
    return true;
  }

  @Override
  public Resource<File> decode(File source, int width, int height, Options options) {
    return new FileResource(source);
  }
}
