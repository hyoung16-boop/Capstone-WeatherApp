package com.example.weatherproject.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class TestWorker_AssistedFactory_Impl implements TestWorker_AssistedFactory {
  private final TestWorker_Factory delegateFactory;

  TestWorker_AssistedFactory_Impl(TestWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public TestWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<TestWorker_AssistedFactory> create(TestWorker_Factory delegateFactory) {
    return InstanceFactory.create(new TestWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<TestWorker_AssistedFactory> createFactoryProvider(
      TestWorker_Factory delegateFactory) {
    return InstanceFactory.create(new TestWorker_AssistedFactory_Impl(delegateFactory));
  }
}
