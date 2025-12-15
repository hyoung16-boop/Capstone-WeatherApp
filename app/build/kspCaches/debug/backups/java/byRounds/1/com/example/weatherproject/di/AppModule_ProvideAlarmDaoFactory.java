package com.example.weatherproject.di;

import com.example.weatherproject.data.local.AlarmDao;
import com.example.weatherproject.data.local.AppDatabase;
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
public final class AppModule_ProvideAlarmDaoFactory implements Factory<AlarmDao> {
  private final Provider<AppDatabase> appDatabaseProvider;

  public AppModule_ProvideAlarmDaoFactory(Provider<AppDatabase> appDatabaseProvider) {
    this.appDatabaseProvider = appDatabaseProvider;
  }

  @Override
  public AlarmDao get() {
    return provideAlarmDao(appDatabaseProvider.get());
  }

  public static AppModule_ProvideAlarmDaoFactory create(Provider<AppDatabase> appDatabaseProvider) {
    return new AppModule_ProvideAlarmDaoFactory(appDatabaseProvider);
  }

  public static AlarmDao provideAlarmDao(AppDatabase appDatabase) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAlarmDao(appDatabase));
  }
}
