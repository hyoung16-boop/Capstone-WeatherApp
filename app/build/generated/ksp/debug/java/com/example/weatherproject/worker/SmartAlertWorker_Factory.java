package com.example.weatherproject.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.weatherproject.data.repository.WeatherRepository;
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

  private final Provider<PreferenceManager> prefManagerProvider;

  public SmartAlertWorker_Factory(Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> prefManagerProvider) {
    this.weatherRepositoryProvider = weatherRepositoryProvider;
    this.prefManagerProvider = prefManagerProvider;
  }

  public SmartAlertWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, weatherRepositoryProvider.get(), prefManagerProvider.get());
  }

  public static SmartAlertWorker_Factory create(
      Provider<WeatherRepository> weatherRepositoryProvider,
      Provider<PreferenceManager> prefManagerProvider) {
    return new SmartAlertWorker_Factory(weatherRepositoryProvider, prefManagerProvider);
  }

  public static SmartAlertWorker newInstance(Context appContext, WorkerParameters workerParams,
      WeatherRepository weatherRepository, PreferenceManager prefManager) {
    return new SmartAlertWorker(appContext, workerParams, weatherRepository, prefManager);
  }
}
