package shared;

import java.io.File;

public enum ResourcePaths {
  RESOURCE_PATH_CONTROLLERS(String.join(File.separator, "test", "resources", "controllers")),
  RESOURCE_PATH_SHARED(String.join(File.separator, "test", "resources", "shared"));

  private String path;

  /** @param text */
  ResourcePaths(String path) {
    this.path = path;
  }

  /** @return the path */
  public String getPath() {
    return this.path;
  }
}
