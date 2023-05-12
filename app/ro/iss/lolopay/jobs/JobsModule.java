package ro.iss.lolopay.jobs;

import com.google.inject.AbstractModule;

public class JobsModule extends AbstractModule {
  @Override
  protected void configure() {

    // bind jobs - callback processors
    bind(ProcessCallbacksJob.class).asEagerSingleton();
    // bind(GetProviderHooksJob.class).asEagerSingleton();
  }
}
