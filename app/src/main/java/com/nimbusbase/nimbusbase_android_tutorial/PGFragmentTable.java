package com.nimbusbase.nimbusbase_android_tutorial;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

/**
 * Created by Will on 11/10/14.
 */
public class PGFragmentTable extends PreferenceFragment {

    private static String
        sTableNameKey = "table_name";

    protected String
            mTableName;
    protected SQLiteOpenHelper
            mSQLiteOpenHelper;
    protected List<MDLUser>
            mRecords;

    protected ListView
            mListView;

    public static PGFragmentTable newInstance(String tableName) {
        final PGFragmentTable
                fragment = new PGFragmentTable();
        final Bundle
                bundle = new Bundle();
        bundle.putString(sTableNameKey, tableName);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        final Bundle
                bundle = getArguments();
        this.mTableName = bundle.getString(sTableNameKey);

        this.mSQLiteOpenHelper = new MDLDatabaseManager(getActivity());

        final PreferenceScreen
                preferenceScreen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(preferenceScreen);
        preferenceScreen.setOrderingAsAdded(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View
                view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView
                listView = (ListView) view.findViewById(android.R.id.list);
        this.mListView = listView;

        registerForContextMenu(listView);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ActionBar
            actionBar = getActivity().getActionBar();
        if (actionBar != null)
            actionBar.setTitle("Table " + mTableName);

        final SQLiteDatabase
                database = mSQLiteOpenHelper.getReadableDatabase();
        final List<MDLUser>
                records = MDLUser.fetchAll(database);
        database.close();

        if (!records.equals(mRecords)) {
            this.mRecords = records;
            reload(records);
            final PreferenceScreen
                    preferenceScreen = getPreferenceScreen();
            preferenceScreen.removeAll();
            for (final MDLUser user : records) {
                final PGListItemRecord
                        item = new PGListItemRecord(getActivity(), user);
                item.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return onRecordPressed((PGListItemRecord) preference);
                    }
                });
                preferenceScreen.addPreference(item);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pg_menu_table, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_insert_new_record == item.getItemId()) {
            onInsertButtonClick(item);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (mListView != null) {
            final AdapterView.AdapterContextMenuInfo
                    info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            final PGListItemRecord
                    item = (PGListItemRecord) getPreferenceScreen().getPreference(info.position);
            menu.setHeaderTitle(item.getTitle());
            menu.add(Menu.NONE, 0, 0, R.string.action_delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final PGListItemRecord
                recordItem = (PGListItemRecord) getPreferenceScreen().getPreference(info.position);
        onRecordDelete(recordItem);
        return true;
    }

    protected boolean onRecordPressed(PGListItemRecord item) {
        final MDLUser
                user = (MDLUser) item.getRecord();
        final PGFragmentRecordExist
                fragment = PGFragmentRecordExist.newInstance(mTableName, user.id, MDLUser.ATTRIBUTE_TYPE_BY_NAME);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    protected boolean onRecordDelete(PGListItemRecord item) {
        final MDLUser
                user = (MDLUser) item.getRecord();
        final SQLiteDatabase
                database = mSQLiteOpenHelper.getWritableDatabase();
        final boolean
                deleted = user.delete(database);
        database.close();

        if (deleted) {
            mRecords.remove(user);
            getPreferenceScreen().removePreference(item);
        }

        return true;
    }

    protected void onInsertButtonClick(MenuItem menuItem) {
        final PGFragmentRecordNew
                fragment = PGFragmentRecordNew.newInstance(mTableName, MDLUser.ATTRIBUTE_TYPE_BY_NAME);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    protected void reload(List<? extends PGRecord> records) {
        final PreferenceScreen
                preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        for (final PGRecord user : records) {
            final PGListItemRecord
                    item = new PGListItemRecord(getActivity(), user);
            item.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return onRecordPressed((PGListItemRecord) preference);
                }
            });
            preferenceScreen.addPreference(item);
        }
    }
}
