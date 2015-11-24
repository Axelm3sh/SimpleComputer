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
    private static final boolean DEBUG = true;
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
        this.CPUThread = new CPUHandler(memoryCell,inputCell,outputCell);

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
                CPUThread.setProgramCounter(Integer.parseInt(ProgramCounter.getText().toString()));

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

                CPUThread.CallStepTime();

                SetCPUDisplay(CPUThread);
            }
        });


    }

    /*************image button/menu click event*********************/
    public void onClick(View view)
    {
        startActivity(new Intent(this,HelpActivity.class));
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
                    CPUThread.setProgramCounter(Integer.parseInt(s.toString()));
                } catch (Exception e) {
                    CPUThread.setProgramCounter(Integer.parseInt(String.valueOf(ProgramCounter.getText())));
                }
            }
        });

    }

    //Change on screen display of CPU text, GUI-only function, does not change internal CPU
    private void SetCPUDisplay(CPUHandler cpuHandler)
    {
        Accumulator.setText(String.format("%03d",cpuHandler.getAccumulator()));
        AccumulatorCarry.setText(String.valueOf(cpuHandler.getAccumulatorCarry()));
        InstructionRegister.setText(String.format("%03d", cpuHandler.getInstructionRegister()));
        ProgramCounter.setText(String.format("%02d", cpuHandler.getProgramCounter()));
    }

    //CPU internal data and display clear method
    private void ClearCPUDisplay()
    {
        CPUThread.ClearCPU(); //Clears internal
        SetCPUDisplay(CPUThread); //Resets display GUI

        popToast(String.format("CPU has been Reset\n AC: %d%d IR: %d PC: %d",
                CPUThread.getAccumulatorCarry(), CPUThread.getAccumulator(),
                CPUThread.getAccumulator(),
                CPUThread.getProgramCounter()));
    }


    //INNER CLASS ComputeTask, Asynchronous Background task, Handles Calculations on CPUThread
    private class ComputeTask extends AsyncTask<CPUHandler, Integer, CPUHandler>
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

                            publishProgress(CPUThread.getProgramCounter(),
                                    CPUThread.getAccumulator(), CPUThread.getAccumulatorCarry(),
                                    CPUThread.getInstructionRegister());
                        }
                    });

                    if (DEBUG) Log.d(VERBOSE, String.format("External %d", CPUThread.getProgramCounter()));

                    Thread.sleep(500);//Deliberate delay before next loop iteration
                }
            } catch (Exception e) {
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
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            //Todo implement method for progress change, values 0 to 100 i suppose

            ProgramCounter.setText(String.format("%02d",values[0]));
            Accumulator.setText(String.format("%03d", values[1]));
            AccumulatorCarry.setText(String.valueOf(values[2]));
            InstructionRegister.setText(String.format("%03d", values[3]));

            if (DEBUG) Log.d(VERBOSE, String.format("Progress Update %d", values[0]));

        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(CPUHandler cpuHandler)
        {
            super.onCancelled(cpuHandler);
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

        CharSequence[] items = {" ", " ", " ", " "};
        //array list to keep the selected items
        boolean[] itemsChecked = new boolean[items.length];


        public DialogHandler(View view)
        {
            this.dialogView = view;
        }

        public void invokeClearCheckBox()
        {

            items[0] = " CPU ";
            items[1] = " Memory ";
            items[2] = " Input ";
            items[3] = " Output ";

            //Building dynamic AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());

            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Which section would you like to clear?")
                    .setMultiChoiceItems(items, itemsChecked, new DialogInterface.OnMultiChoiceClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked)
                        {
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
//                                            CPUThread.LoadMemoryArray(memoryCell.getArrayData());
                                        }
                                        break;
                                    case 2: //Input
                                        if (itemsChecked[index]) {
                                            inputCell.ClearAllCells();
//                                            CPUThread.LoadInputArray(inputCell.getArrayData());
                                        }
                                        break;
                                    case 3: //Output
                                        if (itemsChecked[index]) {
                                            outputCell.ClearAllCells();
//                                            CPUThread.LoadOutputArray(outputCell.getArrayData());
                                        }
                                        break;
                                    default:
                                        if (DEBUG)
                                            Log.d(VERBOSE, "Out of bounds: itemsChecked.length");
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


        /******Program preloader********/
        public void invokeProgramLoader()
        {

            items[0] = " Bootload ";
            items[1] = " Division ";
            items[2] = " Shifting Digits ";
            items[3] = " Absolute Value ";
            final int checked = -1;

            //Building dynamic AlertDialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());

            builder.setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("Which example would you like to try?")
                    .setSingleChoiceItems(items, checked, new DialogInterface.OnClickListener()
                    {

                    //todo // FIXME: 11/24/2015 create program loader
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Load data into internal CPU, which then gets called into the GUI for display

//                            switch(which) {
//                                case 0: CPUThread.LoadInputArray(PreloadData(which)); //boot load input
//                                        itemsChecked[0] = true;
//                                    break;
//                                case 1: CPUThread.LoadMemoryArray(PreloadData(which));
//                                        itemsChecked[1] = true;
//                                    break;
//                                default: CPUThread.LoadInputArray(PreloadData(0));
//                            }
                        }
                    })
                    .setPositiveButton("Load Selected", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
//                            if(itemsChecked[0]) {
//                                inputCell.UpdateCells(CPUThread.getCellInputList());
//                            }
//                            if(itemsChecked[1])
//                            {
//                                memoryCell.UpdateCells(CPUThread.getCellDataList());
//                            }
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
    /*
    private List<CellData> PreloadData(int which)
    {
        List<CellData> list = new ArrayList<>();


        switch (which) {
            case 0:
                String[] a = {"002", "600", "003", "200",
                        "004", "501", "005", "601", "002", "401"};
                for (int i = 0; i < a.length; i++) {
                    CellData instance = new CellData();
                    instance.setCellIDNumber(i);
                    instance.setCellData(a[i]);
                    list.add(instance);
                }
                break;
            case 1:
                String data = "001,,,,,,,,,,,,,,,,,,,,804,534,035,036,435,336,732,535,434,200,534,624,110,900";
                String[] dataSplit = data.split(",");

                for (int i = 0; i < dataSplit.length; i++) {
                    CellData instance = new CellData();
                    instance.setCellIDNumber(i);
                    instance.setCellData(dataSplit[i]);
                    list.add(instance);

                    if (DEBUG) Log.d(VERBOSE, String.format("division: %s", dataSplit[i]));
                }

                break;
            case 2:
                break;
            case 3:
                break;
        }

        if (DEBUG) Log.d(VERBOSE, String.format("%s", list));
        return list;
    }*/


    private void popToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }//popToast


}
