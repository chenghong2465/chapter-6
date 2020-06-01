package com.byted.camp.todolist.db;

import android.provider.BaseColumns;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量

    private TodoContract() {
    }

    public static class TodoEntry implements BaseColumns {
        public static final String TABLE_NAME = "TodoList";
        public static final String COLUMN_NAME_STATE = "state";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_CONTENT = "content";
    }

    static final String SQL_CREATE_ENTRIES =
            "create table " + TodoEntry.TABLE_NAME + " (" + TodoEntry._ID
                    + " integer primary key, " + TodoEntry.COLUMN_NAME_DATE + " long,"
                    + TodoEntry.COLUMN_NAME_CONTENT + " text," + TodoEntry.COLUMN_NAME_STATE
                    + " integer)";
    private static final String SQL_DELETE_ENTRIES = "drop table if exists "
            +TodoEntry.TABLE_NAME;
}
