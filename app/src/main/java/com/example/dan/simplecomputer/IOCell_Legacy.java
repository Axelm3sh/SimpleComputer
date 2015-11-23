package com.example.dan.simplecomputer;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by DANIEL on 11/12/2015.
 *
 * Class will handle creating fragment that holds cells as well as dynamically create new ones
 * Strictly handles GUI IO Cells and their functionality, internal data will be handled
 * by CellData Class
 */
public class IOCell_Legacy extends Fragment
{
    //Declare the variables to be used in GUI interface
    private int numCellCounter = 0; //Counter for cells
    private LinearLayout scroll;    //Linear Layout within the ScrollView that will hold the cells to be arranged vertically
    private LayoutInflater inflaterInner; //Inflater to instantiate instance of our cell_layout2
    private View view; //Our current view we are handling stuff in

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Activity_main is outermost view, take and inflate frag_cell_list2 layout within activity_main layout and set current view to it.
        this.view = inflater.inflate(R.layout.frag_cell_list2, container, true);

        //Initialize layout and layout inflater, sets the views to the current view so no null pointers down the line
        //scroll will look inside our view -> frag_cell_list2 and find a LinLayout with id LLIOCell, set itself to it
        this.scroll = (LinearLayout) view.findViewById(R.id.LinearLayoutIOCell);
        //Basically gets System's universal layout inflater service to use down the line
        this.inflaterInner = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        //find in our current view a button etc...
        Button button = (Button) view.findViewById(R.id.AddCellIOButton);
        //Handles this specific button functionality such as click and longClick
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Dynamic cell generate function call, pass in our view to add to,
                // pass in inflater to show actual layout on screen
                AddCell(scroll, inflaterInner);
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                //Invokes the Dialog Number Picker
                new DialogHandler(view, scroll, inflaterInner).invoke();

                return true;
            }
        });

        //This view is returned
        return view;
    }

    //OnViewCreated: After whole View is created, buttons, functionality and all
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Initialize cell 00, view is passed in
        Button button = (Button) view.findViewById(R.id.AddCellIOButton);
        if(button.hasOnClickListeners())
        {
            AddCell(scroll, inflaterInner);
        }

        //This view belongs to a fragment that has the tag output, call universal frag manager,
        // check to see if we are in the fragment with the tag output
        if (this.equals(getFragmentManager().findFragmentByTag("output")))
        {
            button.setVisibility(View.GONE);
        }

    }



    public void AddCell(LinearLayout scroll, LayoutInflater inflater)
    {
        //make a new View, Grab layout we want to inflate
        View view = inflater.inflate(R.layout.cell_layout2, null);

        //Do stuff by adding functionality to text boxes and labels
        TextView textView = (TextView) view.findViewById(R.id.cellIOLabel);
//        EditText editText = (EditText) view.findViewById(R.id.cellIOData);
        textView.setTag(numCellCounter);
        if(numCellCounter < 10) //Cells less than double digits will append with 0-prefix
        {
            textView.setText(String.format("0%d", numCellCounter));
        }
        else//Cells 10 and above have double digits, format normally.
        {
            textView.setText(String.valueOf(numCellCounter));
        }
        //Use Scroll as our reference and add our new view to it.
        scroll.addView(view); //no null pointers here, we got our views from superclass

        numCellCounter++;

    }

    //INNER CLASS DIALOG HANDLER, CREATES POPUPS
    private class DialogHandler
    {
        //InnerVariables, could delete use directly from superclass
        private final LinearLayout scroll;
        private final LayoutInflater inflaterInner;
        private View view;

        //Constructor
        public DialogHandler(View view, LinearLayout scroll, LayoutInflater inflaterInner)
        {
            this.view = view;
            this.scroll = scroll;
            this.inflaterInner = inflaterInner;
        }

        public void invoke()
        {
            final int[] extendCellsBy = {0};

            //Building dynamic AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

//              Dynamic Number Picker Layout, can select numbers
            final NumberPicker np = new NumberPicker(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            np.setMaxValue(50);
            np.setMinValue(0);
            np.setWrapSelectorWheel(false);
            np.setLayoutParams(params);
            np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
            {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal)
                {
                    extendCellsBy[0] = newVal;
                }
            });
            builder.setView(np);


            builder.setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("How many cells would you like to add")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            for (int i = 0; i < extendCellsBy[0]; i++) {
                                AddCell(scroll, inflaterInner);//Remove and inplement multiadd
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Toast.makeText(getActivity(), "Action Cancelled", Toast.LENGTH_SHORT).show();
                            // Toast.makeText settles the information needed for display, .show() actually pops it up
                        }
                    });

            builder.create();//Settles all settings into builder
            builder.show(); //Now we pop it up
        }
    }
}
