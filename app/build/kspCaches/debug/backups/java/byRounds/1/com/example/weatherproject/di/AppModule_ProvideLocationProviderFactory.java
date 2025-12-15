package com.example.weatherproject.di;

import android.app.Application;
import com.example.weatherproject.util.LocationProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class AppModule_ProvideLocationProviderFactory implements Factory<LocationProvider> {
  private final Provider<Application> applicationProvider;

  public AppModule_ProvideLocationProviderFactory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public LocationProvider get() {
    return provideLocationProvider(applicationProvider.get());
  }

  public static AppModule_ProvideLocationProviderFactory create(
      Provider<Application> applicationProvider) {
    return new AppModule_ProvideLocationProviderFactory(applicationProvider);
  }

  public static LocationProvider provideLocationProvider(Application application) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideLocationProvider(application));
  }
}
