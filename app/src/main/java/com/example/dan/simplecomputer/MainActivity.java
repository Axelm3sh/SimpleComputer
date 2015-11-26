package com.example.dan.simplecomputer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity
{

    //CONSTANTS
    private static final boolean DEBUG = false;
    private static final String VERBOSE = "DDP";

    //Classes
    ComputeTask newAsyncTask;
    //EditText boxes
    EditText Accumulator, AccumulatorCarry, InstructionRegister, ProgramCounter;
    //CPU Emulator
    CPUHandler CPUThread;
    //CellFragments
    MemoryCell memoryCell, inputCell, outputCell;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        //Great, now to deal with instances...

        //MemoryCell GUI Instance
        this.memoryCell = (MemoryCell) fragmentManager.findFragmentById(R.id.listView_MemCells);
        this.inputCell = (MemoryCell) fragmentManager.findFragmentById(R.id.listView_InputCard);
        this.outputCell = (MemoryCell) fragmentManager.findFragmentById(R.id.listView_OutputCard);

        //Create SimpleCPU
        this.CPUThread = new CPUHandler(memoryCell, inputCell, outputCell);

        //Initialize the text boxes
        this.Accumulator = (EditText) this.findViewById(R.id.CPU_editTx_AC);
        this.AccumulatorCarry = (EditText) this.findViewById(R.id.CPU_editTx_AC_carry);
        this.InstructionRegister = (EditText) this.findViewById(R.id.CPU_editTx_IR);
        this.ProgramCounter = (EditText) this.findViewById(R.id.CPU_editTx_PC);


        /**************Clear Button Click events*****************/
        final Button clearButton = (Button) findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogHandler clearPopup = new DialogHandler(clearButton.getRootView());
                clearPopup.invokeClearCheckBox();
                //todo should cancel Async Task
            }
        });
        clearButton.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {

                //Clears Only CPU memory
                ClearCPUDisplay();

                //Clears GUI Cells
                memoryCell.ClearAllCells();
                inputCell.ClearAllCells();
                outputCell.ClearAllCells();

                return true;
            }
        });

        /**************Run-time Button click events****************/
        final Button runButton = (Button) findViewById(R.id.buttonRun);
        runButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (DEBUG) Log.d(VERBOSE, "Running Program - RUN_HIT_FLAG");
                if (DEBUG) Log.d(VERBOSE, String.format("CPUThread before Async is %s", CPUThread));

                //Get current Value of PC counter before we start operation
                CPUThread.setProgramCounter(ProgramCounter.getText().toString());

                newAsyncTask = new ComputeTask();
                newAsyncTask.execute(CPUThread);

                if (DEBUG) Log.d(VERBOSE, String.format("CPUThread after Async is %s", CPUThread));
            }
        });
        runButton.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                CPUThread.ClearCPU(); //Clear CPU

                DialogHandler loader = new DialogHandler(runButton.getRootView());
                loader.invokeProgramLoader();
                return true;
            }
        });

        /****************Step Button click events****************/
        Button stepButton = (Button) findViewById(R.id.buttonStep);
        stepButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!CPUThread.CheckError()) {
                    CPUThread.CallStepTime();
                    SetCPUDisplay(CPUThread);
                }
            }
        });


    }

    /*************
     * image button/menu click event
     *********************/
    public void onClick(View view)
    {
        startActivity(new Intent(this, HelpActivity.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        //disable Accumulator Text Change
        Accumulator.setFocusable(false);
        Accumulator.setKeyListener(null);
        //disable AccumulatorCarry Text Change
        AccumulatorCarry.setFocusable(false);
        AccumulatorCarry.setKeyListener(null);
        //disable Instruction Register Text Change
        InstructionRegister.setFocusable(false);
        InstructionRegister.setKeyListener(null);

        SetCPUDisplay(CPUThread); //Initialize CPU GUI Text

        //Program Counter Listener - listen for changes to the value
        ProgramCounter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {//empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {//empty
            }

            @Override
            public void afterTextChanged(Editable s) {
                String temp;

                temp = s.toString();
                CPUThread.setProgramCounter(temp);
            }
        });

    }

    //Change on screen display of CPU text, GUI-only function, does not change internal CPU
    private void SetCPUDisplay(CPUHandler cpuHandler)
    {
        Accumulator.setText(String.format("%03d", cpuHandler.getAccumulator()));
        AccumulatorCarry.setText(String.valueOf(cpuHandler.getAccumulatorCarry()));
        InstructionRegister.setText(String.format("%03d", cpuHandler.getInstructionRegister()));
        ProgramCounter.setText(String.format("%02d", cpuHandler.getProgramCounter()));
    }

    //CPU internal data and display clear method
    private void ClearCPUDisplay()
    {

        if(newAsyncTask != null) {
            newAsyncTask.cancel(false);
        }

        CPUThread.ClearCPU(); //Clears internal
        SetCPUDisplay(CPUThread); //Resets display GUI

        popToast(String.format("CPU has been Reset\n AC: %d%d IR: %d PC: %d",
                CPUThread.getAccumulatorCarry(), CPUThread.getAccumulator(),
                CPUThread.getAccumulator(),
                CPUThread.getProgramCounter()));
    }

    public void DataLoader(int choice)
    {
        String appendedString = "";

        switch (choice) {
            case 1: //Saving File

                //Turn current Memory cells into Single String data
                for (int i = 0; i < memoryCell.returnNumCellsGenerated(); i++) {
                    appendedString = appendedString + String.format("%s-", memoryCell.GetCellData(i));
                }
                int lastDash = appendedString.lastIndexOf("-");
                if (lastDash != -1) {
                    appendedString = appendedString.substring(0, lastDash - 1);
                }

                //New intent > new Bundle > put stuff into bundle > put bundle inside intent > ship
                Intent intent = new Intent(this.getBaseContext(), FileEditorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("dataString", appendedString);
                bundle.putBoolean("save", true);
                intent.putExtras(bundle);

                startActivityForResult(intent, 1);
                break;

            case 2: //Loading File
                Intent intent2 = new Intent(this.getBaseContext(), FileEditorActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("dataString", appendedString);
                bundle2.putBoolean("save", false);
                intent2.putExtras(bundle2);

                startActivityForResult(intent2, 2);

            case 3: //BootLoad
                String bootLoadProg = "002-600-003-200-004-501-005-601-002-401-";
                String[] bootLoadSplit = bootLoadProg.split("-");

                inputCell.ClearAllCells();

                for (String aBootLoadSplit : bootLoadSplit) {
                    inputCell.AddCell();
                }

                for (int i = 0; i < bootLoadSplit.length; i++) {
                    inputCell.ChangeCell(i, bootLoadSplit[i]);
                }
                break;
            case 4:
                String divisionProg = "001--------------------804-534-035-036-435-336-732-535-434-200-534-624-134-900-";
                String[] divisionSplit = divisionProg.split("-");

                memoryCell.ClearAllCells();

                for (int i = 0; i < divisionSplit.length; i++) {
                    memoryCell.ChangeCell(i, divisionSplit[i]);
                }

                break;
            case 5:
                String shiftProg = "001--------------------035-435-813-536-435-823-810-236-536-435-831-236-536-136-900-";
                String[] shiftProgSplit = shiftProg.split("-");

                memoryCell.ClearAllCells();

                for (int i = 0; i < shiftProgSplit.length; i++) {
                    memoryCell.ChangeCell(i, shiftProgSplit[i]);
                }

                break;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                popToast("File has been saved to " + data.getDataString());
            }
            else if (resultCode == RESULT_CANCELED) {
                popToast("File Save Cancelled");
            }
        }
        else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                popToast("File has been loaded");

                //Get back data of single String file
                String temp = data.getDataString();

                //Split the string into individual data
                String[] tempAr = temp.split("-");

                //Load Cells with stuff
                for (int i = 0; i < tempAr.length; i++) {
                    memoryCell.ChangeCell(i, tempAr[i]);
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                popToast("File Load Cancelled");
            }
        }
    }

    //INNER CLASS ComputeTask, Asynchronous Background task, Handles Calculations on CPUThread
    private class ComputeTask extends AsyncTask<CPUHandler, CPUHandler, CPUHandler>
    {

        @Override
        protected CPUHandler doInBackground(CPUHandler... params)
        {
            if (DEBUG) Log.d(VERBOSE, "params: " + params[0]);

            try {
                while (!CPUThread.CheckError()) {

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //stuff that updates ui
                            CPUThread.CallStepTime();
                            publishProgress(CPUThread);

                        }
                    });

                    if (DEBUG)
                        Log.d(VERBOSE, String.format("External %d", CPUThread.getProgramCounter()));

                    Thread.sleep(500);//Deliberate delay before next loop iteration
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            //todo Call step-time and once step is done, update directly with findbyViewid
            return params[0];
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(CPUHandler s)
        {
            super.onPostExecute(s);
            if (DEBUG)
                Log.d(VERBOSE, String.format("PostExecuteAsync <ASYNC_TASK_DONE> class return: %s", s));

            SetCPUDisplay(s);


            popToast(CPUThread.getErrStr());

            if (DEBUG) popToast("Async Task Finished!");

        }

        @Override
        protected void onProgressUpdate(CPUHandler... values)
        {
            super.onProgressUpdate(values);
            //Todo implement method for progress change, values 0 to 100 i suppose

            SetCPUDisplay(values[0]);

        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();

            popToast("FORCING CPU HALT");
        }

        @Override
        protected void onCancelled(CPUHandler cpuHandler)
        {
            super.onCancelled(cpuHandler);

            popToast("FORCING CPU HALT");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //INNER CLASS DIALOG HANDLER, CREATES POPUPS
    protected class DialogHandler
    {
        private View dialogView;

        //array list to keep the selected items, used for multi-list
        CharSequence[] clearItems = {" CPU ", " Memory ", " Input ", " Output "};
        boolean[] clearItemsChecked = new boolean[clearItems.length];

        //array to keep selected items, used for singleItemlist
        CharSequence[] executeItems = {" Bootload ", " Division ", " Shifting Digits "};
        int executeItemsWhich = -1;


        public DialogHandler(View view)
        {
            this.dialogView = view;
        }

        public void invokeClearCheckBox()
        {
            //Building dynamic AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());

            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Which section would you like to clear?")
                    .setMultiChoiceItems(clearItems, clearItemsChecked, new DialogInterface.OnMultiChoiceClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked)
                        {
                            clearItemsChecked[which] = isChecked;
                        }
                    })
                    .setPositiveButton("Clear Selected", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            for (int index = 0; index < clearItemsChecked.length; index++) {
                                switch (index) {
                                    case 0: //CPU
                                        if (clearItemsChecked[index]) {
                                            ClearCPUDisplay();
                                        }
                                        break;
                                    case 1: //Memory
                                        if (clearItemsChecked[index]) {
                                            memoryCell.ClearAllCells();
                                        }
                                        break;
                                    case 2: //Input
                                        if (clearItemsChecked[index]) {
                                            inputCell.ClearAllCells();
                                        }
                                        break;
                                    case 3: //Output
                                        if (clearItemsChecked[index]) {
                                            outputCell.ClearAllCells();
                                        }
                                        break;
                                    default:
                                        if (DEBUG)
                                            Log.d(VERBOSE, "Out of bounds: clearItemsChecked.length");
                                }
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            popToast("Action Cancelled");
                        }
                    });

            // Toast.makeText settles the information needed for display, .show() actually pops it up
            builder.create();//Settles all settings into builder
            builder.show(); //Now we pop it up
        }


        /******
         * Program preloader
         ********/
        public void invokeProgramLoader()
        {

            //Building dynamic AlertDialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());

            builder.setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("Which example would you like to try?")
                    .setSingleChoiceItems(executeItems, executeItemsWhich, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //todo load defaults 1 boot, 2 div, 3 shift dig using which
                            executeItemsWhich = which;
                        }
                    })
                    .setPositiveButton("Select", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            popToast("You selected " + executeItems[executeItemsWhich]);

                            switch (executeItemsWhich) {
                                case 0:
                                    DataLoader(3);
                                    break;
                                case 1:
                                    DataLoader(4);
                                    break;
                                case 2:
                                    DataLoader(5);
                                    break;
                                default:
                                    break;
                            }
                        }

                    })
                    .setNegativeButton("Load User Defined", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            DataLoader(2);
                        }
                    })
                    .setNeutralButton("Save Current", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            DataLoader(1);
                        }
                    });

            // Toast.makeText settles the information needed for display, .show() actually pops it up
            builder.create();//Settles all settings into builder
            builder.show(); //Now we pop it up
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Todo create a File Read in, can use this as template too


    private void popToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }//popToast


}
