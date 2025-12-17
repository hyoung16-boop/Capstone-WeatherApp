package com.example.weatherproject.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class TestWorker_Factory {
  public TestWorker_Factory() {
  }

  public TestWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams);
  }

  public static TestWorker_Factory create() {
    return new TestWorker_Factory();
  }

  public static TestWorker newInstance(Context context, WorkerParameters workerParams) {
    return new TestWorker(context, workerParams);
  }
}
