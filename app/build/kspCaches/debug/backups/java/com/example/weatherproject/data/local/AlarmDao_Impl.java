package com.example.weatherproject.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AlarmDao_Impl implements AlarmDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AlarmEntity> __insertionAdapterOfAlarmEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<AlarmEntity> __deletionAdapterOfAlarmEntity;

  private final EntityDeletionOrUpdateAdapter<AlarmEntity> __updateAdapterOfAlarmEntity;

  public AlarmDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlarmEntity = new EntityInsertionAdapter<AlarmEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `alarms` (`id`,`hour`,`minute`,`days`,`selectedDate`,`isEnabled`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AlarmEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getHour());
        stmt.bindLong(3, value.getMinute());
        final String _tmp = __converters.fromList(value.getDays());
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, _tmp);
        }
        if (value.getSelectedDate() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindLong(5, value.getSelectedDate());
        }
        final int _tmp_1 = value.isEnabled() ? 1 : 0;
        stmt.bindLong(6, _tmp_1);
      }
    };
    this.__deletionAdapterOfAlarmEntity = new EntityDeletionOrUpdateAdapter<AlarmEntity>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `alarms` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AlarmEntity value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__updateAdapterOfAlarmEntity = new EntityDeletionOrUpdateAdapter<AlarmEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `alarms` SET `id` = ?,`hour` = ?,`minute` = ?,`days` = ?,`selectedDate` = ?,`isEnabled` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AlarmEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getHour());
        stmt.bindLong(3, value.getMinute());
        final String _tmp = __converters.fromList(value.getDays());
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, _tmp);
        }
        if (value.getSelectedDate() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindLong(5, value.getSelectedDate());
        }
        final int _tmp_1 = value.isEnabled() ? 1 : 0;
        stmt.bindLong(6, _tmp_1);
        stmt.bindLong(7, value.getId());
      }
    };
  }

  @Override
  public Object insertAlarm(final AlarmEntity alarm,
      final Continuation<? super Long> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          long _result = __insertionAdapterOfAlarmEntity.insertAndReturnId(alarm);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteAlarm(final AlarmEntity alarm,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAlarmEntity.handle(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object updateAlarm(final AlarmEntity alarm,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAlarmEntity.handle(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<AlarmEntity>> getAllAlarms() {
    final String _sql = "SELECT * FROM alarms";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"alarms"}, new Callable<List<AlarmEntity>>() {
      @Override
      public List<AlarmEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfDays = CursorUtil.getColumnIndexOrThrow(_cursor, "days");
          final int _cursorIndexOfSelectedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedDate");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<AlarmEntity> _result = new ArrayList<AlarmEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AlarmEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final List<String> _tmpDays;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDays)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDays);
            }
            _tmpDays = __converters.fromString(_tmp);
            final Long _tmpSelectedDate;
            if (_cursor.isNull(_cursorIndexOfSelectedDate)) {
              _tmpSelectedDate = null;
            } else {
              _tmpSelectedDate = _cursor.getLong(_cursorIndexOfSelectedDate);
            }
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            _item = new AlarmEntity(_tmpId,_tmpHour,_tmpMinute,_tmpDays,_tmpSelectedDate,_tmpIsEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAlarmById(final int id, final Continuation<? super AlarmEntity> continuation) {
    final String _sql = "SELECT * FROM alarms WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AlarmEntity>() {
      @Override
      public AlarmEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfDays = CursorUtil.getColumnIndexOrThrow(_cursor, "days");
          final int _cursorIndexOfSelectedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedDate");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final AlarmEntity _result;
          if(_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final List<String> _tmpDays;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDays)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDays);
            }
            _tmpDays = __converters.fromString(_tmp);
            final Long _tmpSelectedDate;
            if (_cursor.isNull(_cursorIndexOfSelectedDate)) {
              _tmpSelectedDate = null;
            } else {
              _tmpSelectedDate = _cursor.getLong(_cursorIndexOfSelectedDate);
            }
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            _result = new AlarmEntity(_tmpId,_tmpHour,_tmpMinute,_tmpDays,_tmpSelectedDate,_tmpIsEnabled);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
