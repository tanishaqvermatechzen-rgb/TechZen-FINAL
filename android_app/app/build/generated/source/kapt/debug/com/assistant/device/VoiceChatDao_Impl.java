package com.assistant.device;

import android.database.Cursor;
import androidx.annotation.NonNull;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class VoiceChatDao_Impl implements VoiceChatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VoiceChatEntity> __insertionAdapterOfVoiceChatEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldVoiceChats;

  public VoiceChatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVoiceChatEntity = new EntityInsertionAdapter<VoiceChatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `voice_chats` (`id`,`timestamp`,`user_query`,`assistant_response`,`provider`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VoiceChatEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        if (entity.getUserQuery() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getUserQuery());
        }
        if (entity.getAssistantResponse() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAssistantResponse());
        }
        if (entity.getProvider() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getProvider());
        }
      }
    };
    this.__preparedStmtOfDeleteOldVoiceChats = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM voice_chats WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final VoiceChatEntity chat, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfVoiceChatEntity.insert(chat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldVoiceChats(final long sinceTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldVoiceChats.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sinceTime);
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
          __preparedStmtOfDeleteOldVoiceChats.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VoiceChatEntity>> getRecentVoiceChatsFlow() {
    final String _sql = "SELECT * FROM voice_chats ORDER BY timestamp DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"voice_chats"}, new Callable<List<VoiceChatEntity>>() {
      @Override
      @NonNull
      public List<VoiceChatEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUserQuery = CursorUtil.getColumnIndexOrThrow(_cursor, "user_query");
          final int _cursorIndexOfAssistantResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "assistant_response");
          final int _cursorIndexOfProvider = CursorUtil.getColumnIndexOrThrow(_cursor, "provider");
          final List<VoiceChatEntity> _result = new ArrayList<VoiceChatEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VoiceChatEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpUserQuery;
            if (_cursor.isNull(_cursorIndexOfUserQuery)) {
              _tmpUserQuery = null;
            } else {
              _tmpUserQuery = _cursor.getString(_cursorIndexOfUserQuery);
            }
            final String _tmpAssistantResponse;
            if (_cursor.isNull(_cursorIndexOfAssistantResponse)) {
              _tmpAssistantResponse = null;
            } else {
              _tmpAssistantResponse = _cursor.getString(_cursorIndexOfAssistantResponse);
            }
            final String _tmpProvider;
            if (_cursor.isNull(_cursorIndexOfProvider)) {
              _tmpProvider = null;
            } else {
              _tmpProvider = _cursor.getString(_cursorIndexOfProvider);
            }
            _item = new VoiceChatEntity(_tmpId,_tmpTimestamp,_tmpUserQuery,_tmpAssistantResponse,_tmpProvider);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
