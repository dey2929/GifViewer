package GifViewer.load.engine;

import GifViewer.load.Key;

interface EngineJobListener {

  void onEngineJobComplete(Key key, EngineResource<?> resource);

  void onEngineJobCancelled(EngineJob engineJob, Key key);
}
