package com.example.mapstest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
public class LoadActivity extends Activity implements RepositoryChangedListener
{
    final int MENU_RENAME = 0;
    final int MENU_DELETE = 1;
    final int MENU_DELETE_ALL = 2;

    ListView lvLoad;
    String[] names;
    TracksRepository tracksRepository;
    TextView tvLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);
        getActionBar().hide();

        tvLoad = (TextView) findViewById(R.id.tvLoad);
        lvLoad = (ListView) findViewById(R.id.lvLoad);
        registerForContextMenu(lvLoad);
        tracksRepository = (TracksRepository) getApplication();
        setListeners();
        updList();
    }

    private void updList()
    {
        names = tracksRepository.getNames();
        if (names.length != 0)
        {
            tvLoad.setText(R.string.saved_runs);
        }
        else
        {
            tvLoad.setText(R.string.nothing_saved);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.load_list_item, names);
        lvLoad.setAdapter(adapter);
    }

    protected void setListeners() {
        lvLoad.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                tracksRepository.setCurRunIndex(position);
                Intent intent = new Intent(LoadActivity.this, RunShowActivity.class);
                startActivity(intent);
            }
        });

        tracksRepository.addListener(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        tracksRepository.removeListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
        menu.setHeaderTitle(names[info.position]);
        String[] menuItems = getResources().getStringArray(R.array.load_menu);
        for (int i = 0; i < menuItems.length; ++i)
        {
            menu.add(0, i, i, menuItems[i]);
        }
    }

    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        tracksRepository.setCurRunIndex(info.position);

        switch (item.getItemId())
        {
            case MENU_RENAME:
                Dialogs.showRenameDialog(this);
                break;
            case MENU_DELETE:
                Dialogs.showDeleteDialog(this);
                break;
            case MENU_DELETE_ALL:
                Dialogs.showDeleteAllDialog(this);
                break;
            default:
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void repositoryChanged(int actionCode)
    {
        updList();
    }
}
