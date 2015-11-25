package com.example.dan.simplecomputer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

/**
 * a Dialog Frag
 */
public class AlertDialogFrag extends DialogFragment
{
    String title = "";

    public void setTitle(String title)
    {
        this.title = title;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        doPositiveClick();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        doNegativeClick();
                    }
                });

        return builder.create();
    }


    public void doPositiveClick()
    {
        popToast("You clicked OK");
    }

    public void doNegativeClick()
    {
        popToast("You clicked Cancel");
    }

    private void popToast(String text)
    {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    } //popToast Advanced

}
