package com.assistant.device;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class SmartDeviceDao_Impl implements SmartDeviceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SmartDeviceEntity> __insertionAdapterOfSmartDeviceEntity;

  public SmartDeviceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSmartDeviceEntity = new EntityInsertionAdapter<SmartDeviceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `smart_devices` (`id`,`name`,`type`,`is_on`,`value`,`status_text`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SmartDeviceEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getType());
        }
        final int _tmp = entity.isOn() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getValue());
        if (entity.getStatusText() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStatusText());
        }
      }
    };
  }

  @Override
  public Object insertOrUpdate(final SmartDeviceEntity device,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSmartDeviceEntity.insert(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SmartDeviceEntity>> getAllDevicesFlow() {
    final String _sql = "SELECT * FROM smart_devices";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"smart_devices"}, new Callable<List<SmartDeviceEntity>>() {
      @Override
      @NonNull
      public List<SmartDeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsOn = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfStatusText = CursorUtil.getColumnIndexOrThrow(_cursor, "status_text");
          final List<SmartDeviceEntity> _result = new ArrayList<SmartDeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SmartDeviceEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final boolean _tmpIsOn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOn);
            _tmpIsOn = _tmp != 0;
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final String _tmpStatusText;
            if (_cursor.isNull(_cursorIndexOfStatusText)) {
              _tmpStatusText = null;
            } else {
              _tmpStatusText = _cursor.getString(_cursorIndexOfStatusText);
            }
            _item = new SmartDeviceEntity(_tmpId,_tmpName,_tmpType,_tmpIsOn,_tmpValue,_tmpStatusText);
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
  public Object getAllDevicesDirect(
      final Continuation<? super List<SmartDeviceEntity>> $completion) {
    final String _sql = "SELECT * FROM smart_devices";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SmartDeviceEntity>>() {
      @Override
      @NonNull
      public List<SmartDeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsOn = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfStatusText = CursorUtil.getColumnIndexOrThrow(_cursor, "status_text");
          final List<SmartDeviceEntity> _result = new ArrayList<SmartDeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SmartDeviceEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final boolean _tmpIsOn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOn);
            _tmpIsOn = _tmp != 0;
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final String _tmpStatusText;
            if (_cursor.isNull(_cursorIndexOfStatusText)) {
              _tmpStatusText = null;
            } else {
              _tmpStatusText = _cursor.getString(_cursorIndexOfStatusText);
            }
            _item = new SmartDeviceEntity(_tmpId,_tmpName,_tmpType,_tmpIsOn,_tmpValue,_tmpStatusText);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDeviceById(final String id,
      final Continuation<? super SmartDeviceEntity> $completion) {
    final String _sql = "SELECT * FROM smart_devices WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SmartDeviceEntity>() {
      @Override
      @Nullable
      public SmartDeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsOn = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfStatusText = CursorUtil.getColumnIndexOrThrow(_cursor, "status_text");
          final SmartDeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final boolean _tmpIsOn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOn);
            _tmpIsOn = _tmp != 0;
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final String _tmpStatusText;
            if (_cursor.isNull(_cursorIndexOfStatusText)) {
              _tmpStatusText = null;
            } else {
              _tmpStatusText = _cursor.getString(_cursorIndexOfStatusText);
            }
            _result = new SmartDeviceEntity(_tmpId,_tmpName,_tmpType,_tmpIsOn,_tmpValue,_tmpStatusText);
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

  @Override
  public Flow<SmartDeviceEntity> getDeviceFlowById(final String id) {
    final String _sql = "SELECT * FROM smart_devices WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"smart_devices"}, new Callable<SmartDeviceEntity>() {
      @Override
      @Nullable
      public SmartDeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfIsOn = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfStatusText = CursorUtil.getColumnIndexOrThrow(_cursor, "status_text");
          final SmartDeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final boolean _tmpIsOn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOn);
            _tmpIsOn = _tmp != 0;
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final String _tmpStatusText;
            if (_cursor.isNull(_cursorIndexOfStatusText)) {
              _tmpStatusText = null;
            } else {
              _tmpStatusText = _cursor.getString(_cursorIndexOfStatusText);
            }
            _result = new SmartDeviceEntity(_tmpId,_tmpName,_tmpType,_tmpIsOn,_tmpValue,_tmpStatusText);
          } else {
            _result = null;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
