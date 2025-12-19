package com.example.weatherproject.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.weatherproject.data.repository.WeatherRepository;
import com.example.weatherproject.util.LocationProvider;
import com.example.weatherproject.util.PreferenceManager;
import dagger.internal.DaggerGenerated;
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
public final class SmartAlertWorker_Factory {
  private final Provider<WeatherRepository> weatherRepositoryProvider;

  private final Provider<PreferenceManager> preferenceManagerProvider;

  private final Provider<LocationProvider> locationProvider;

  public SmartAlertWorker_Factory(Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> preferenceManagerProvider,
      Provider<LocationProvider> locationProvider) {
    this.weatherRepositoryProvider = weatherRepositoryProvider;
    this.preferenceManagerProvider = preferenceManagerProvider;
    this.locationProvider = locationProvider;
  }

  public SmartAlertWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, weatherRepositoryProvider.get(), preferenceManagerProvider.get(), locationProvider.get());
  }

  public static SmartAlertWorker_Factory create(
      Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> preferenceManagerProvider,
      Provider<LocationProvider> locationProvider) {
    return new SmartAlertWorker_Factory(weatherRepositoryProvider, preferenceManagerProvider, locationProvider);
  }

  public static SmartAlertWorker newInstance(Context appContext, WorkerParameters workerParams,
      WeatherRepository weatherRepository, PreferenceManager preferenceManager,
      LocationProvider locationProvider) {
    return new SmartAlertWorker(appContext, workerParams, weatherRepository, preferenceManager, locationProvider);
  }
}
