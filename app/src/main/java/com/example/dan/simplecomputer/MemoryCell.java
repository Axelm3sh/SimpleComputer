package com.example.dan.simplecomputer;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by DANIEL on 10/21/2015.
 * <p/>
 * Class will handle creating fragment that holds cells as well as dynamically create new ones
 * Strictly handles GUI Memory Cells and their functionality
 */
public class MemoryCell extends Fragment
{
    private static final boolean DEBUG = false;
    private static final String VERBOSE = "DDP";

    //counter to keep track of max cells currently have
    private int numCellCounter = 0; //Ceiling of current available memory cells
    private LinearLayout scroll;    //Linear Layout within the ScrollView that will hold the cells to be arranged vertically
    private LayoutInflater inflaterInner; //Inflater to instantiate instance of our cell_layout2
    private View view; //Our current view we are handling stuff in

    private boolean flag_maxcells_hit = false;

    public MemoryCell()
    {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Output fragment has different functionality
        if (this.equals(getFragmentManager().findFragmentByTag("memory"))) {
            //Note: Use this.variable in CreateView to use everywhere else in Class universally
            this.view = inflater.inflate(R.layout.frag_cell_list, container, true);

            //Initialize layout and layout inflater, sets the views to the current view so no null pointers down the line
            this.scroll = (LinearLayout) view.findViewById(R.id.LinearLayoutMemCell);
            this.inflaterInner = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            Button button = (Button) view.findViewById(R.id.AddCellButton);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new DialogHandler(view, scroll, inflaterInner).invoke();
                }
            });
        }
        else {
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
                    AddCell();

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
        }

        if (DEBUG)
            Log.d(VERBOSE, String.format("This View of inflater is: %s", String.valueOf(view)));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //This view belongs to a fragment that has the tag output, call universal frag manager,
        // check to see if we are in the fragment with the tag output
        if (this.equals(getFragmentManager().findFragmentByTag("output")) ||
                this.equals(getFragmentManager().findFragmentByTag("input"))) {
            //Get IO button
            Button button = (Button) view.findViewById(R.id.AddCellIOButton);
            button.setText(R.string.addbtn_txt2); //Change button text to match input cards


            if (this.equals(getFragmentManager().findFragmentByTag("output"))) //No need for button for output view
            {
                button.setVisibility(View.INVISIBLE); //Remove button from view
                // Get Fragment IO Label
                TextView label = (TextView) view.findViewById(R.id.fragLabel);
                label.setText(R.string.IOLabel2);
            }

            AddCell();

            if (DEBUG)
                Log.d(VERBOSE, String.format("LL: %s", view.findViewById(R.id.LinearLayoutIOCell)));
        }
        else //memory fragment
        {
            //Initialize cell 00, view is passed in from superclass, get MemoryCell Button
            for (int i = 0; i < 100; i++) {
                AddCell();
            }
        }
    }


    public void AddCell()
    {
        final TextView textView;
        final EditText editText;
        View view;

        if (this.equals(getFragmentManager().findFragmentByTag("memory"))) {
            //Grab cell_layout to inflate + data fields
            view = inflaterInner.inflate(R.layout.cell_layout, null);
            textView = (TextView) view.findViewById(R.id.cellLabel);
            editText = (EditText) view.findViewById(R.id.cellData);
        }
        else {
            //Grab cell_layout2 to inflate + data fields
            view = inflaterInner.inflate(R.layout.cell_layout2, null);
            textView = (TextView) view.findViewById(R.id.cellIOLabel);
            editText = (EditText) view.findViewById(R.id.cellIOData);
            if (this.equals(getFragmentManager().findFragmentByTag("output"))) {
                editText.setHint("OUTPUT");
            }
        }

        if (DEBUG)
            Log.d(VERBOSE, String.format("View ID is: %s\nThe textView is: %s\nThe editText is: %s",
                    view, textView, editText));

        textView.setTag(returnNumCellsGenerated()); //Maybe not needed lol


        if (returnNumCellsGenerated() <= 99) {//Limits max cells to 99
            if (returnNumCellsGenerated() < 10) //Cells less than double digits will append with 0-prefix
            {
                textView.setText(String.format("%02d", returnNumCellsGenerated()));
                if (returnNumCellsGenerated() == 0 &&
                        this.equals(getFragmentManager().findFragmentByTag("memory"))) {//If first cell 00 and memory cell, disable typing and set data 001
                    editText.setText(String.format("%03d", returnNumCellsGenerated() + 1));
                    editText.setKeyListener(null);
                    editText.setFocusable(false);
                }
                else if (this.equals(getFragmentManager().findFragmentByTag("output"))) {
                    editText.setKeyListener(null);
                    editText.setFocusable(false);
                }
            }
            else//Cells 10 and above have double digits, format normally.
            {
                textView.setText(String.valueOf(returnNumCellsGenerated()));
            }

            scroll.addView(view); //no null pointers here, we got our views

            UpdateNumCellCounter(); //Call update Cell counter function to get latest values
        }
        else {

            if (!flag_maxcells_hit) {
                popToastL("Max Memory Cells Reached!");
                flag_maxcells_hit = !flag_maxcells_hit;
            }

        }
    }

    //Method Call to clear all Data pertaining to GUI cells and Internal Data
    //Pretty expensive to re-create all the cells, use sparingly
    public void ClearAllCells()
    {
        scroll.removeAllViews(); //Gets rid of all child views in scroll

        // Clear internal array of numbers stored
        //arrayData.clear(); //<- This gives so many out of index problems later on, screw this function
        resetNumCellCounter(); //Reset internal counter because we are rebuilding new list anyways

        if (this.equals(getFragmentManager().findFragmentByTag("memory"))) {
            for (int i = 0; i < 100; i++) {
                AddCell();
            }
        }
    }

    //ChangeCell, specify which location to change. if out of index bounds, create until available
    //Used for singular data change
    public void ChangeCell(int location, String data)
    {
        LinearLayout linearLayout = scroll;

        if (DEBUG) Log.d(VERBOSE, String.format("ChangeCell[Layout Children count] %s",
                String.valueOf(linearLayout.getChildCount())));

        //Check if its an LinearLayout cell_layout at this index location corresponding to cell number
        if (linearLayout.getChildAt(location) instanceof LinearLayout) {
            LinearLayout localLayout = (LinearLayout) linearLayout.getChildAt(location);

            if (localLayout.getChildAt(1) instanceof EditText) //should be child edittext
            {
                EditText text = (EditText) localLayout.getChildAt(1);
                    if (data.equals("")) { //Don't give null pointer error
                        text.setText("");
                    }
                    else {
                        text.setText(String.format("%03d", Integer.parseInt(data)));
                    }
                text.getParent().requestChildFocus(text, text);
            }//LL Child
        }//LL
        else {
            popToastS(String.format("Memory Cell Unavailable, Add Memory %02d", location));
        }

    }

    public String GetCellData(int location)
    {
        LinearLayout linearLayout = scroll;
        String data = "";

        if (DEBUG) Log.d(VERBOSE, String.format("ChangeCell[Layout Children count] %s",
                String.valueOf(linearLayout.getChildCount())));

        //Check if its an LinearLayout cell_layout at this index location corresponding to cell number
        if (linearLayout.getChildAt(location) instanceof LinearLayout) {
            LinearLayout localLayout = (LinearLayout) linearLayout.getChildAt(location);

            if (localLayout.getChildAt(1) instanceof EditText) //should be child edittext
            {
                EditText text = (EditText) localLayout.getChildAt(1);
                data = String.valueOf(text.getText());
                text.getParent().requestChildFocus(text, text);

            }//LL Child
        }//LL
        else {
            popToastS(String.format("Memory Cell %02d Invalid Data", location));
        }
        return data;
    }


    protected void UpdateNumCellCounter()
    {
        numCellCounter = scroll.getChildCount();
    }

    protected void resetNumCellCounter()
    {
        //Using .getChildCount returns true number of cells we have in the list
        numCellCounter = scroll.getChildCount();
        flag_maxcells_hit = false;
    }

    /*Return Methods for numCellCounter inside*/
    public int returnNumCellsGenerated()
    {
        if (DEBUG) Log.d(VERBOSE, String.format("Returning NumCells: %d", numCellCounter));
        return numCellCounter;
    }


    private void popToastS(String text)
    {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 70);
        toast.show();
    } //popToastS Advanced

    private void popToastL(String text)
    {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 70);
        toast.show();
    } //popToastL - long


    //INNER CLASS DIALOG HANDLER, CREATES POPUPS
    protected class DialogHandler
    {
        private final LinearLayout scroll;
        private final LayoutInflater inflaterInner;
        private View view;

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

//          Dynamic Number Picker Layout, can select numbers
            final NumberPicker np = new NumberPicker(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            np.setMaxValue(99);
            np.setMinValue(0);
            np.setWrapSelectorWheel(true);
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
                    .setTitle("What cell would you want to jump to?")
                    .setNegativeButton("Jump to Cell", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (extendCellsBy[0] < returnNumCellsGenerated() &&
                                    scroll.getChildAt(extendCellsBy[0]) instanceof LinearLayout)
                            //Check if its an LinearLayout cell_layout at this index location corresponding to cell number
                            {
                                LinearLayout localLayout = (LinearLayout) scroll.getChildAt(extendCellsBy[0]);
                                if (localLayout.getChildAt(1) instanceof EditText) //should be child edittext
                                {
                                    EditText text = (EditText) localLayout.getChildAt(1);
                                    text.getParent().requestChildFocus(text, text);

                                    popToastS("Found!");
                                }
                            }
                            else {
                                popToastS("Cell Not Found");
                            }
                        }
                    });

            if (view.equals(view.findViewById(R.id.LinearLayoutIOCell))) {
                builder.setPositiveButton("Add Cell(s)", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for (int i = 0; i < extendCellsBy[0]; i++) {
                            AddCell();//Remove and inplement multiadd
                        }
                    }
                });
            }

            // Toast.makeText settles the information needed for display, .show() actually pops it up
            builder.create();//Settles all settings into builder
            builder.show(); //Now we pop it up
        }
    }
}
