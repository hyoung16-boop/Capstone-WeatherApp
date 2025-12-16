package com.example.weatherproject.ui;

import android.app.Application;
import com.example.weatherproject.data.local.AlarmDao;
import com.example.weatherproject.data.repository.SettingsRepository;
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
public final class AlarmViewModel_Factory implements Factory<AlarmViewModel> {
  private final Provider<AlarmDao> alarmDaoProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<Application> applicationProvider;

  public AlarmViewModel_Factory(Provider<AlarmDao> alarmDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.alarmDaoProvider = alarmDaoProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public AlarmViewModel get() {
    return newInstance(alarmDaoProvider.get(), settingsRepositoryProvider.get(), applicationProvider.get());
  }

  public static AlarmViewModel_Factory create(Provider<AlarmDao> alarmDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new AlarmViewModel_Factory(alarmDaoProvider, settingsRepositoryProvider, applicationProvider);
  }

  public static AlarmViewModel newInstance(AlarmDao alarmDao, SettingsRepository settingsRepository,
      Application application) {
    return new AlarmViewModel(alarmDao, settingsRepository, application);
  }
}
