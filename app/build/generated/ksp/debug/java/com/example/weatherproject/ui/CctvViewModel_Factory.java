package com.example.weatherproject.ui;

import android.app.Application;
import com.example.weatherproject.data.repository.WeatherRepository;
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
public final class CctvViewModel_Factory implements Factory<CctvViewModel> {
  private final Provider<WeatherRepository> repositoryProvider;

  private final Provider<Application> applicationProvider;

  public CctvViewModel_Factory(Provider<WeatherRepository> repositoryProvider,
      Provider<Application> applicationProvider) {
    this.repositoryProvider = repositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public CctvViewModel get() {
    return newInstance(repositoryProvider.get(), applicationProvider.get());
  }

  public static CctvViewModel_Factory create(Provider<WeatherRepository> repositoryProvider,
      Provider<Application> applicationProvider) {
    return new CctvViewModel_Factory(repositoryProvider, applicationProvider);
  }

  public static CctvViewModel newInstance(WeatherRepository repository, Application application) {
    return new CctvViewModel(repository, application);
  }
}
