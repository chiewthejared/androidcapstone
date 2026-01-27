package com.example.test_v2.fileAndDatabase;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.test_v2.HelperUserAccount;
import com.example.test_v2.HelperUserDao;
import com.example.test_v2.calendar.HelperEvent;
import com.example.test_v2.calendar.HelperEventDao;
import com.example.test_v2.notes.HelperNote;
import com.example.test_v2.notes.HelperNoteDao;
import com.example.test_v2.tags.Tag;
import com.example.test_v2.tags.TagDao;
import com.example.test_v2.timer.HelperTimerEvent;
import com.example.test_v2.timer.HelperTimerEventDao;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.List;


@Database(
        entities = {
                HelperUserAccount.class,
                HelperEvent.class,
                HelperNote.class,
                Tag.class,  // Ensure Tag is included
                HelperTimerEvent.class
        },
        version = 6,
        exportSchema = true
)
public abstract class HelperAppDatabase extends RoomDatabase {

    public abstract HelperUserDao userDao();
    public abstract HelperEventDao eventDao();
    public abstract HelperNoteDao noteDao();
    public abstract TagDao tagDao();

    public abstract HelperTimerEventDao timerEventDao();

    private static volatile HelperAppDatabase INSTANCE;
    private static final Executor databaseWriteExecutor = Executors.newFixedThreadPool(4);

    // Migration from v2 → v3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE HelperUserAccount ADD COLUMN phoneNumber TEXT DEFAULT NULL");
            android.util.Log.d("DatabaseMigration", "Migration 2 → 3 Applied");
        }
    };

    // Migration from v3 → v4
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Check if the 'notes' table exists before recreating it
            database.execSQL("CREATE TABLE IF NOT EXISTS `notes` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`user_id` TEXT NOT NULL, " +
                    "`title` TEXT, " +
                    "`content` TEXT, " +
                    "`file_path` TEXT, " +
                    "`created_at` TEXT)");
            android.util.Log.d("DatabaseMigration", "Migration 3 → 4 Applied");
        }
    };

    // Migration from v4 → v5: create the tags table
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))");
            android.util.Log.d("DatabaseMigration", "Migration 4 → 5 Applied: Tags table created");
        }
    };

    //TIMER DB
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create timer_events table if not exists
            database.execSQL("CREATE TABLE IF NOT EXISTS `timer_events` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`user_id` TEXT, " +
                    "`start_timestamp` INTEGER NOT NULL, " +
                    "`total_time_ms` INTEGER NOT NULL, " +
                    "`intervals_json` TEXT, " +
                    "`notes` TEXT" +
                    ")"
            );
            android.util.Log.d("DatabaseMigration", "Migration 5 → 6: timer_events table created.");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add the new column for actionLog if it doesn't exist
            database.execSQL("ALTER TABLE timer_events ADD COLUMN `action_log` TEXT");
        }
    };


    public static HelperAppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (HelperAppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HelperAppDatabase.class,
                                    "user-database"
                            )
                            .addMigrations(
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_5_6,
                                    MIGRATION_6_7
                            )
                            .build();
                    insertDefaultTags();
                }
            }
        }
        return INSTANCE;
    }

    private static void insertDefaultTags() {
        databaseWriteExecutor.execute(() -> {
            TagDao tagDao = INSTANCE.tagDao();

            // Get current tags
            List<Tag> existingTags = tagDao.getAll();
            List<String> tagNames = new ArrayList<>();
            for (Tag t : existingTags) {
                tagNames.add(t.name.toLowerCase());
            }

            // Insert "Work", "Personal", "Urgent" if they don't exist yet
            if (!tagNames.contains("work")) {
                tagDao.insert(new Tag("Work"));
            }
            if (!tagNames.contains("personal")) {
                tagDao.insert(new Tag("Personal"));
            }
            if (!tagNames.contains("urgent")) {
                tagDao.insert(new Tag("Urgent"));
            }
        });
    }
    public static Executor getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }
}
