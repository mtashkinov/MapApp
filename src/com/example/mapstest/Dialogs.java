package com.example.mapstest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Dialogs
{
    public static void showRenameDialog(final Activity activity)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_dialog, null);
        final EditText etName = (EditText) view.findViewById(R.id.etName);

        dialog.setView(view);
        dialog.setTitle(R.string.rename);

        dialog.setNeutralButton(
                R.string.cancel,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                    }
                }
        );

        dialog.setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg2)
                    {
                        String newName = etName.getText().toString();
                        TracksRepository tracksRepository = (TracksRepository)activity.getApplication();
                        if (!tracksRepository.rename(newName))
                        {
                            Toast.makeText(activity, activity.getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
                            showRenameDialog(activity);
                        }
                    }
                }
        );
        dialog.show();
    }

    public static void showDeleteDialog(final Activity activity)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.delete_run_title);
        dialog.setMessage(R.string.delete_run);

        dialog.setPositiveButton (
                R.string.yes,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                        TracksRepository tracksRepository = (TracksRepository)activity.getApplication();
                        tracksRepository.delete();
                        Toast.makeText(activity, activity.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        dialog.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg2)
                    {
                    }
                }
        );

        dialog.show();
    }

    public static void showDeleteAllDialog(final Activity activity)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.delete_all_title);
        dialog.setMessage(R.string.delete_all);

        dialog.setPositiveButton (
                R.string.yes,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                        TracksRepository tracksRepository = (TracksRepository)activity.getApplication();
                        tracksRepository.deleteAll();
                        Toast.makeText(activity, activity.getString(R.string.all_deleted), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        dialog.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg2)
                    {
                    }
                }
        );

        dialog.show();
    }

    public static void showSaveDialog(final Activity activity, final Run run, String title, final DialogClickListener listener)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(title);
        dialog.setMessage(R.string.save_data);

        dialog.setPositiveButton (
                R.string.yes,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                        listener.onDialogNotCanceled(true);
                        TracksRepository tracksRepository = (TracksRepository)activity.getApplication();
                        tracksRepository.save(run);
                    }
                }
        );

        dialog.setNeutralButton(
                R.string.cancel,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg2)
                    {
                    }
                }
        );

        dialog.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg3)
                    {
                        listener.onDialogNotCanceled(false);
                    }
                }
        );
        dialog.show();
    }
}
