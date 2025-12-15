package com.example.weatherproject.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WeatherDao_Impl implements WeatherDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WeatherCacheEntity> __insertionAdapterOfWeatherCacheEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearCache;

  public WeatherDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWeatherCacheEntity = new EntityInsertionAdapter<WeatherCacheEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `weather_cache` (`id`,`current_iconUrl`,`current_temperature`,`current_description`,`current_maxTemp`,`current_minTemp`,`current_feelsLike`,`details_feelsLike`,`details_humidity`,`details_precipitation`,`details_wind`,`details_pm10`,`details_pressure`,`details_visibility`,`details_uvIndex`,`hourlyForecastJson`,`weeklyForecastJson`,`latitude`,`longitude`,`address`,`lastUpdated`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WeatherCacheEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCurrent_iconUrl());
        statement.bindString(3, entity.getCurrent_temperature());
        statement.bindString(4, entity.getCurrent_description());
        statement.bindString(5, entity.getCurrent_maxTemp());
        statement.bindString(6, entity.getCurrent_minTemp());
        statement.bindString(7, entity.getCurrent_feelsLike());
        statement.bindString(8, entity.getDetails_feelsLike());
        statement.bindString(9, entity.getDetails_humidity());
        statement.bindString(10, entity.getDetails_precipitation());
        statement.bindString(11, entity.getDetails_wind());
        statement.bindString(12, entity.getDetails_pm10());
        statement.bindString(13, entity.getDetails_pressure());
        statement.bindString(14, entity.getDetails_visibility());
        statement.bindString(15, entity.getDetails_uvIndex());
        statement.bindString(16, entity.getHourlyForecastJson());
        statement.bindString(17, entity.getWeeklyForecastJson());
        statement.bindDouble(18, entity.getLatitude());
        statement.bindDouble(19, entity.getLongitude());
        statement.bindString(20, entity.getAddress());
        statement.bindString(21, entity.getLastUpdated());
      }
    };
    this.__preparedStmtOfClearCache = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM weather_cache";
        return _query;
      }
    };
  }

  @Override
  public Object upsertWeatherCache(final WeatherCacheEntity weatherCache,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWeatherCacheEntity.insert(weatherCache);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearCache(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearCache.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearCache.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getWeatherCache(final Continuation<? super WeatherCacheEntity> $completion) {
    final String _sql = "SELECT * FROM weather_cache WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WeatherCacheEntity>() {
      @Override
      @Nullable
      public WeatherCacheEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCurrentIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "current_iconUrl");
          final int _cursorIndexOfCurrentTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "current_temperature");
          final int _cursorIndexOfCurrentDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "current_description");
          final int _cursorIndexOfCurrentMaxTemp = CursorUtil.getColumnIndexOrThrow(_cursor, "current_maxTemp");
          final int _cursorIndexOfCurrentMinTemp = CursorUtil.getColumnIndexOrThrow(_cursor, "current_minTemp");
          final int _cursorIndexOfCurrentFeelsLike = CursorUtil.getColumnIndexOrThrow(_cursor, "current_feelsLike");
          final int _cursorIndexOfDetailsFeelsLike = CursorUtil.getColumnIndexOrThrow(_cursor, "details_feelsLike");
          final int _cursorIndexOfDetailsHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "details_humidity");
          final int _cursorIndexOfDetailsPrecipitation = CursorUtil.getColumnIndexOrThrow(_cursor, "details_precipitation");
          final int _cursorIndexOfDetailsWind = CursorUtil.getColumnIndexOrThrow(_cursor, "details_wind");
          final int _cursorIndexOfDetailsPm10 = CursorUtil.getColumnIndexOrThrow(_cursor, "details_pm10");
          final int _cursorIndexOfDetailsPressure = CursorUtil.getColumnIndexOrThrow(_cursor, "details_pressure");
          final int _cursorIndexOfDetailsVisibility = CursorUtil.getColumnIndexOrThrow(_cursor, "details_visibility");
          final int _cursorIndexOfDetailsUvIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "details_uvIndex");
          final int _cursorIndexOfHourlyForecastJson = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyForecastJson");
          final int _cursorIndexOfWeeklyForecastJson = CursorUtil.getColumnIndexOrThrow(_cursor, "weeklyForecastJson");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final WeatherCacheEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpCurrent_iconUrl;
            _tmpCurrent_iconUrl = _cursor.getString(_cursorIndexOfCurrentIconUrl);
            final String _tmpCurrent_temperature;
            _tmpCurrent_temperature = _cursor.getString(_cursorIndexOfCurrentTemperature);
            final String _tmpCurrent_description;
            _tmpCurrent_description = _cursor.getString(_cursorIndexOfCurrentDescription);
            final String _tmpCurrent_maxTemp;
            _tmpCurrent_maxTemp = _cursor.getString(_cursorIndexOfCurrentMaxTemp);
            final String _tmpCurrent_minTemp;
            _tmpCurrent_minTemp = _cursor.getString(_cursorIndexOfCurrentMinTemp);
            final String _tmpCurrent_feelsLike;
            _tmpCurrent_feelsLike = _cursor.getString(_cursorIndexOfCurrentFeelsLike);
            final String _tmpDetails_feelsLike;
            _tmpDetails_feelsLike = _cursor.getString(_cursorIndexOfDetailsFeelsLike);
            final String _tmpDetails_humidity;
            _tmpDetails_humidity = _cursor.getString(_cursorIndexOfDetailsHumidity);
            final String _tmpDetails_precipitation;
            _tmpDetails_precipitation = _cursor.getString(_cursorIndexOfDetailsPrecipitation);
            final String _tmpDetails_wind;
            _tmpDetails_wind = _cursor.getString(_cursorIndexOfDetailsWind);
            final String _tmpDetails_pm10;
            _tmpDetails_pm10 = _cursor.getString(_cursorIndexOfDetailsPm10);
            final String _tmpDetails_pressure;
            _tmpDetails_pressure = _cursor.getString(_cursorIndexOfDetailsPressure);
            final String _tmpDetails_visibility;
            _tmpDetails_visibility = _cursor.getString(_cursorIndexOfDetailsVisibility);
            final String _tmpDetails_uvIndex;
            _tmpDetails_uvIndex = _cursor.getString(_cursorIndexOfDetailsUvIndex);
            final String _tmpHourlyForecastJson;
            _tmpHourlyForecastJson = _cursor.getString(_cursorIndexOfHourlyForecastJson);
            final String _tmpWeeklyForecastJson;
            _tmpWeeklyForecastJson = _cursor.getString(_cursorIndexOfWeeklyForecastJson);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final String _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getString(_cursorIndexOfLastUpdated);
            _result = new WeatherCacheEntity(_tmpId,_tmpCurrent_iconUrl,_tmpCurrent_temperature,_tmpCurrent_description,_tmpCurrent_maxTemp,_tmpCurrent_minTemp,_tmpCurrent_feelsLike,_tmpDetails_feelsLike,_tmpDetails_humidity,_tmpDetails_precipitation,_tmpDetails_wind,_tmpDetails_pm10,_tmpDetails_pressure,_tmpDetails_visibility,_tmpDetails_uvIndex,_tmpHourlyForecastJson,_tmpWeeklyForecastJson,_tmpLatitude,_tmpLongitude,_tmpAddress,_tmpLastUpdated);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
