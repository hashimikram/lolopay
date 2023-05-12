package ro.iss.lolopay.programs.classes;

import akka.stream.Materializer;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;

/**
 * This is copied from play framework's WithApplication, because that class's jar is not available
 * in deployment, only for testing. This should not be used for unit tests.
 *
 * <p>Provides an application for JUnit tests. Make your test class extend this class and an
 * application will be started before each test is invoked. You can setup the application to use by
 * overriding the provideApplication method. Within a test, the running application is available
 * through the app field.
 */
public class WithApplicationForPrograms {

  protected Application app;

  /** The application's Akka streams Materializer. */
  protected Materializer mat;

  /**
   * Override this method to setup the application to use.
   *
   * @return The application to use
   */
  protected Application provideApplication() {

    return new GuiceApplicationBuilder().build();
  }

  /**
   * Provides an instance from the application.
   *
   * @param clazz the type's class.
   * @param <T> the type to return, using `app.injector.instanceOf`
   * @return an instance of type T.
   */
  protected <T> T instanceOf(Class<T> clazz) {

    return app.injector().instanceOf(clazz);
  }

  /**
   * Provides an instance from the application.
   *
   * @param clazz the type's class.
   * @param <T> the type to return, using `app.injector.instanceOf`
   * @return an instance of type T.
   * @deprecated As of 2.6.0. Use {@link #instanceOf(Class)}.
   */
  @Deprecated
  <T> T inject(Class<T> clazz) {

    return instanceOf(clazz);
  }

  public void startPlay() {

    app = provideApplication();
    play.api.Play.start(app.asScala());
    mat = app.asScala().materializer();
  }

  public void stopPlay() {

    if (app != null) {
      play.api.Play.stop(app.asScala());
      app = null;
    }
  }
}
