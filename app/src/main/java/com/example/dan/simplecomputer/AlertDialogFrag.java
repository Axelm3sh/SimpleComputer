package com.example.dan.simplecomputer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by Dan on 3/11/2015.
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

        //Dynamic Edittext input snippet, can extract values ie password box
//        final EditText input = new EditText(getActivity());
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
//        input.setLayoutParams(params);
//        builder.setView(input); // uncomment this line

        //Dynamic Number Picker Layout, can select numbers
//        final NumberPicker np = new NumberPicker(getActivity());
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        np.setMaxValue(100);
//        np.setMinValue(0);
//        np.setWrapSelectorWheel(false);
//        np.setLayoutParams(params);
//        builder.setView(np);

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

//    public void onClick(View view) //Code to instantiate this dialog frag
//    {
//        AlertDialogFrag frag = new AlertDialogFrag();
//        frag.setTitle("Time to go home?");
//        frag.show(getFragmentManager(), "dialog");
//    }

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
