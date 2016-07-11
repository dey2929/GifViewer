package GifViewer.load.resource.file;

import GifViewer.load.resource.SimpleResource;

import java.io.File;

/**
 * A simple {@link GifViewer.load.engine.Resource} that wraps a {@link File}.
 */
public class FileResource extends SimpleResource<File> {
  public FileResource(File file) {
    super(file);
  }
}
