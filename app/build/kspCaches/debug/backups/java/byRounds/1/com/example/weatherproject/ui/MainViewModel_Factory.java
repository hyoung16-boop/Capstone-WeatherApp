package com.example.weatherproject.ui;

import com.example.weatherproject.data.repository.WeatherRepository;
import com.example.weatherproject.util.LocationProvider;
import com.example.weatherproject.util.PreferenceManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<WeatherRepository> weatherRepositoryProvider;

  private final Provider<LocationProvider> locationProvider;

  private final Provider<PreferenceManager> preferenceManagerProvider;

  public MainViewModel_Factory(Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<LocationProvider> locationProvider,
      Provider<PreferenceManager> preferenceManagerProvider) {
    this.weatherRepositoryProvider = weatherRepositoryProvider;
    this.locationProvider = locationProvider;
    this.preferenceManagerProvider = preferenceManagerProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(weatherRepositoryProvider.get(), locationProvider.get(), preferenceManagerProvider.get());
  }

  public static MainViewModel_Factory create(Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<LocationProvider> locationProvider,
      Provider<PreferenceManager> preferenceManagerProvider) {
    return new MainViewModel_Factory(weatherRepositoryProvider, locationProvider, preferenceManagerProvider);
  }

  public static MainViewModel newInstance(WeatherRepository weatherRepository,
      LocationProvider locationProvider, PreferenceManager preferenceManager) {
    return new MainViewModel(weatherRepository, locationProvider, preferenceManager);
  }
}
