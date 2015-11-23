package com.example.dan.simplecomputer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create SimpleCPU
        this.CPUThread = new CPUHandler();

        FragmentManager fragmentManager = getFragmentManager();
        //Great, now to deal with instances...

        //MemoryCell GUI Instance
        this.memoryCell = (MemoryCell) fragmentManager.findFragmentById(R.id.listView_MemCells);
        this.inputCell = (MemoryCell) fragmentManager.findFragmentById(R.id.listView_InputCard);
        this.outputCell = (MemoryCell) fragmentManager.findFragmentById(R.id.listView_OutputCard);


        //Initialize the text boxes
        Accumulator = (EditText) this.findViewById(R.id.CPU_editTx_AC);
        AccumulatorCarry = (EditText) this.findViewById(R.id.CPU_editTx_AC_carry);
        InstructionRegister = (EditText) this.findViewById(R.id.CPU_editTx_IR);
        ProgramCounter = (EditText) this.findViewById(R.id.CPU_editTx_PC);


        //Clear Button onClickEvent
        final Button clearButton = (Button) findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                //todo should cancel Async Task maybe??

                return true;
            }
        });

        //Run-time Button
        Button runButton = (Button) findViewById(R.id.buttonRun);
        runButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (DEBUG) Log.d(VERBOSE, "Running Program - RUN_HIT_FLAG");

                /*todo Create an async task which calls StepTime function, async progress update will continually reevaluate CPU Display*/
                if (DEBUG) Log.d(VERBOSE, String.format("CPUThread before Async is %s", CPUThread));

                newAsyncTask = new ComputeTask();
                newAsyncTask.execute(CPUThread);

                if (DEBUG) Log.d(VERBOSE, String.format("CPUThread after Async is %s", CPUThread));

            }
        });


        //Step-time Button
        Button stepButton = (Button) findViewById(R.id.buttonStep);
        stepButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                CPUThread.LoadMemoryArray(memoryCell.getArrayData());
                CPUThread.LoadInputArray(inputCell.getArrayData());

                CPUThread.CallStepTime();

                memoryCell.UpdateCells(CPUThread.getCellDataList());
                outputCell.UpdateCells(CPUThread.getCellOutputList());

                ProgramCounter.setText(String.valueOf(CPUThread.getProgramCounter()));
                Accumulator.setText(String.valueOf(CPUThread.getAccumulator()));
                AccumulatorCarry.setText(String.valueOf(CPUThread.getAccumulatorCarry()));
                InstructionRegister.setText(String.valueOf(CPUThread.getInstructionRegister()));
            }
        });

        ImageButton imgButton = (ImageButton) findViewById(R.id.OptionsButton);
        imgButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //todo help file, save file, load file
            }
        });

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
        ProgramCounter.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {//empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {//empty
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                try {
                    CPUThread.setProgramCounter(Integer.getInteger(s.toString()));
                }catch (Exception e)
                {
                    CPUThread.setProgramCounter(Integer.parseInt(String.valueOf(ProgramCounter.getText())));
                }
            }
        });

    }

    //Change on screen display of CPU text, GUI-only function, does not change internal CPU
    private void SetCPUDisplay(CPUHandler cpuHandler) {
        Accumulator.setText(String.valueOf(cpuHandler.getAccumulator()));
        AccumulatorCarry.setText(String.valueOf(cpuHandler.getAccumulatorCarry()));
        InstructionRegister.setText(String.valueOf(cpuHandler.getInstructionRegister()));
        ProgramCounter.setText(String.valueOf(cpuHandler.getProgramCounter()));
    }

    //CPU internal data and display clear method
    private void ClearCPUDisplay() {
        CPUThread.ClearCPU(); //Clears internal
        SetCPUDisplay(CPUThread); //Resets display GUI

        popToast(String.format("CPU has been Reset\n AC: %d%d IR: %d PC: %d",
                CPUThread.getAccumulatorCarry(),CPUThread.getAccumulator(),
                CPUThread.getAccumulator(),
                CPUThread.getProgramCounter()));
    }


    //INNER CLASS ComputeTask, Asynchronous Background task, Handles Calculations on CPUThread
    private class ComputeTask extends AsyncTask<CPUHandler, Integer, CPUHandler> {

        @Override
        protected CPUHandler doInBackground(CPUHandler... params) {
            if (DEBUG) Log.d(VERBOSE, "params: " + params[0]);

            try {
                while (!CPUThread.CheckError()) {
                    CPUThread.CallStepTime();

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                        //stuff that updates ui
                            memoryCell.UpdateCells(CPUThread.getCellDataList());
//                    inputCell.UpdateCells(CPUThread.getCellInputList());
                            outputCell.UpdateCells(CPUThread.getCellOutputList());
                        }
                    });


                    publishProgress(CPUThread.getProgramCounter(),
                            CPUThread.getAccumulator(), CPUThread.getAccumulatorCarry(),
                            CPUThread.getInstructionRegister());

                    Thread.sleep(500);//Deliberate delay before next loop iteration
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //todo Call step-time and once step is done, update directly with findbyViewid
            return params[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Grab all current data available on press into String List, let CPUThread handle calculations
            //Load CPUThread so we don't get an internal null data array
            CPUThread.LoadMemoryArray(memoryCell.getArrayData());
            CPUThread.LoadInputArray(inputCell.getArrayData());
//            CPUThread.LoadOutputArray(outputCell.getArrayData());


        }

        @Override
        protected void onPostExecute(CPUHandler s) {
            super.onPostExecute(s);
            if (DEBUG) Log.d(VERBOSE, String.format("PostExecuteAsync <ASYNC_TASK_DONE> class return: %s", s));

            SetCPUDisplay(s);

            //Update ALL UI CELLS with current values in CPUThread.
            memoryCell.UpdateCells(CPUThread.getCellDataList());
//            inputCell.UpdateCells(CPUThread.getCellInputList());
            outputCell.UpdateCells(CPUThread.getCellOutputList());

            popToast(CPUThread.getErrStr());

            if (DEBUG) popToast("Async Task Finished!");

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //Todo implement method for progress change, values 0 to 100 i suppose

            ProgramCounter.setText(String.valueOf(values[0]));
            Accumulator.setText(String.valueOf(values[1]));
            AccumulatorCarry.setText(String.valueOf(values[2]));
            InstructionRegister.setText(String.valueOf(values[3]));

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(CPUHandler cpuHandler) {
            super.onCancelled(cpuHandler);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //INNER CLASS DIALOG HANDLER, CREATES POPUPS
    protected class DialogHandler {
        private View dialogView;

        CharSequence[] items = {" ", " ", " ", " "};
        //array list to keep the selected items
        boolean[] itemsChecked = new boolean[items.length];


        public DialogHandler(View view) {
            this.dialogView = view;
        }

        public void invokeClearCheckBox() {

            items[0] = " CPU ";
            items[1] = " Memory ";
            items[2] = " Input ";
            items[3] = " Output ";

            //Building dynamic AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());

            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Which section would you like to clear?")
                    .setMultiChoiceItems(items, itemsChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            itemsChecked[which] = isChecked;
                        }
                    })
                    .setPositiveButton("Clear Selected", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            for (int index = 0; index < itemsChecked.length; index++) {
                                switch (index) {
                                    case 0: //CPU
                                        if (itemsChecked[index]) {
                                            ClearCPUDisplay();
                                        }
                                        break;
                                    case 1: //Memory
                                        if (itemsChecked[index]) {
                                            memoryCell.ClearAllCells();
                                            CPUThread.LoadMemoryArray(memoryCell.getArrayData());
                                        }
                                        break;
                                    case 2: //Input
                                        if (itemsChecked[index]) {
                                            inputCell.ClearAllCells();
                                            CPUThread.LoadInputArray(inputCell.getArrayData());
                                        }
                                        break;
                                    case 3: //Output
                                        if (itemsChecked[index]) {
                                            outputCell.ClearAllCells();
//                                            CPUThread.LoadOutputArray(outputCell.getArrayData());
                                        }
                                        break;
                                    default:
                                        if (DEBUG) Log.d(VERBOSE, "Out of bounds: itemsChecked.length");
                                }
                            }
                            outputCell.UpdateCells(CPUThread.getCellOutputList());
//                            inputCell.UpdateCells(CPUThread.getCellInputList());
                            memoryCell.UpdateCells(CPUThread.getCellDataList());
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

        public void invokeProgramLoader() {

            items[0] = " BootLoad ";
            items[1] = " Division ";
            items[2] = " Shifting Digits ";
            items[3] = " Absolute Value ";

            //Building dynamic AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());

            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Which example would you like to try?")
                    .setMultiChoiceItems(items, itemsChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            itemsChecked[which] = isChecked;
                        }
                    })
                    .setPositiveButton("Load Selected", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            for (int index = 0; index < itemsChecked.length; index++) {
                                switch (index) {
                                    case 0: //CPU
                                        if (itemsChecked[index]) {

                                            List<CellData> boot = new ArrayList<CellData>(99);
                                            boot.add(new CellData());

                                            memoryCell.UpdateCells(boot);

                                        }
                                        break;
                                    case 1: //Memory
                                        if (itemsChecked[index]) {
                                            memoryCell.ClearAllCells();
                                            CPUThread.LoadMemoryArray(memoryCell.getArrayData());
                                        }
                                        break;
                                    case 2: //Input
                                        if (itemsChecked[index]) {
                                            inputCell.ClearAllCells();
                                            CPUThread.LoadInputArray(inputCell.getArrayData());
                                        }
                                        break;
                                    case 3: //Output
                                        if (itemsChecked[index]) {
                                            outputCell.ClearAllCells();
//                                            CPUThread.LoadOutputArray(outputCell.getArrayData());
                                        }
                                        break;
                                    default:
                                        if (DEBUG) Log.d(VERBOSE, "Out of bounds: itemsChecked.length");
                                }
                            }
                            outputCell.UpdateCells(CPUThread.getCellOutputList());
//                            inputCell.UpdateCells(CPUThread.getCellInputList());
                            memoryCell.UpdateCells(CPUThread.getCellDataList());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            popToast("No Program Loaded");
                        }
                    });

            // Toast.makeText settles the information needed for display, .show() actually pops it up
            builder.create();//Settles all settings into builder
            builder.show(); //Now we pop it up
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    private List<CellData> PreloadData(int which)
    {
        List<CellData> example;
        CellData instance = new CellData();

        switch(which)
        {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
        return null;
    }


    private void popToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }//popToast


}
