package com.example.dan.simplecomputer;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dan on 10/15/2015.
 */
public class CPUHandler extends Activity
{

    private static final boolean DEBUG = false;
    private static final String VERBOSE = "DDP";

    private List<CellData> cellDataList; //Array for memory cell data
    private List<CellData> cellInputList; //Array for memory cell Inputs
    private List<CellData> cellOutputList; //Array for memory cell Outputs


    private int Accumulator, AccumulatorCarry, InstructionRegister, ProgramCounter;

    /* -1 - unexpected error, 1 - No more input cards, 2 - instruction card empty, 3 - infinite loop*/
    boolean ErrorEncountered = false;
    int ErrFlagCode = 0;
    String ErrStr = "";


    //Constructor
    public CPUHandler()
    {
        if (DEBUG) Log.d("DDP", "Inside Constructor CPU CLASS");

        this.ProgramCounter = 0;
        this.AccumulatorCarry = 0;
        this.Accumulator = 0;
        this.InstructionRegister = 0;
        this.cellDataList = new ArrayList<>(); //Empty Mem List, will be overwritten by later func
        this.cellInputList = new ArrayList<>();//Empty Input list, will be overwritten by later func
        this.cellOutputList = new ArrayList<>(); //Output cells don't have to be instanced.
        // Data will be generated after calling Step-time
    }

    //ClearCPU Function: Sets internal values to 0
    public void ClearCPU()
    {
        setAccumulator(0);
        setAccumulatorCarry(0);
        setInstructionRegister(0);
        setProgramCounter(0);

    }

    public boolean CheckError()
    {
        if (ErrorEncountered) {
            this.ErrStr = ErrorCode(ErrFlagCode);
        }
        return ErrorEncountered;
    }

    /* -1 - unexpected error,
        1 - No more input cards,
        2 - instruction card empty,
        3 - infinite loop*/
    public String ErrorCode(int e)
    {
        switch (e) {
            case 0:
                return "No Error, Program Terminated Normally";
            case 1:
                return "Error: No more Input Cards!";
            case 2:
                return "Error: Expected Instruction in Memory!";
            case 3:
                return "Error: Infinite Loop Detected!";
            default:
                return "Error: Unexpected Case!"; //-1 code
        }
    }

    public String getErrStr()
    {
        String temp = ErrStr;
        ErrStr = "";
        return temp;
    }

    public void CallStepTime()
    {
        /*Fetch Cycle
        *   - look at address in program counter
        *   - get copy of instruction in memory cell @address
        *   - replace contents of IR with new instructions
        *   - increment Program Counter*/
        int pc = getProgramCounter();

        if (DEBUG) Log.d(VERBOSE, String.format("DATA ARRAY: %s", cellDataList));
        String step = getSingleCellData(pc, cellDataList);


        //Check for null string/empty string
        if (step == null) {
            ErrorEncountered = true;
            ErrFlagCode = 2;
        }
        else {
            if (!step.equals("")) {
                setInstructionRegister(Integer.parseInt(step));
                setProgramCounter(getProgramCounter() + 1); //Increment PC by 1
            }

        /*Decode Cycle
        *   - decode the op-code part of the instruction in the IR*/
            InterpretCurrentInstruction(step);
        }


    }

    public void InterpretCurrentInstruction(String value)
    {


        int instructCase;
        int memoryActor;
        //Take string, turn into int, mod by 100 to get first value number
        if (!value.equals("")) {
            instructCase = Integer.parseInt(value) / 100; //Should get first digit
            memoryActor = Integer.parseInt(value) % 100; //Should get memory to act on
        }
        else {
            instructCase = 10;
            memoryActor = 99;
        }
        if (DEBUG) Log.d(VERBOSE, String.format("Interpret OPCODE: [%d][%d] PC@[%d]",
                instructCase, memoryActor, getProgramCounter()));

        /*Execution Cycle
        *   - perform execution required by op-code, using address field of the instruction
        *   in the Instruction Register*/

        try {

            switch (instructCase) {
                case 0:
                    GetInputFromCell(memoryActor);
                    break;
                case 1:
                    SendToOutputCard(memoryActor);
                    break;
                case 3:
                    AddAccumulator(memoryActor);
                    break;
                case 2:
                    SubtractAccumulator(memoryActor);
                    break;
                case 4:
                    LoadAccumulator(memoryActor);
                    break;
                case 5:
                    StoreAccumulator(memoryActor);
                    break;
                case 6:
                    JumpTo(memoryActor);
                    break;
                case 7:
                    TestAccumulator(memoryActor);
                    break;
                case 8:
                    ShiftAccumulator(memoryActor);
                    break;
                case 9:
                    HaltProgram(memoryActor);
                    break;
                default:
                    if (DEBUG)
                        Log.d("DDP", String.format("Error: Code %d in Switch", instructCase));

                    ErrorEncountered = true;
                    ErrFlagCode = 2;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            ErrorEncountered = true;
            ErrFlagCode = -1;
            //unexpected error
        }
    }

    //Note to self, watch out when you copy and paste too much...

    //Load the data into list using maxCellsGenerated as number of cells.
    public void LoadMemoryArray(List<CellData> cellDatas)
    {

        cellDataList = cellDatas;

        if (DEBUG) Log.d("DDP", "Successful list Add <Memory>");
    }

    //Load the data into list using maxCellsGenerated as number of cells.
    //NOTE: Algorithm uses reverse stack
    // (ie: input 00 will be at last index xx, input xx will be at index 00)
    //Later functions will remove first data when done and shift whole stack up keeping current
    //data at first input slot to read.
    public void LoadInputArray(List<CellData> cellDatas)
    {

        //If cellDataList is not empty, we start to clear it and prep for new data to be loaded
        if (cellInputList.isEmpty()) {
            cellInputList = new ArrayList<>(99);
        }

        //From max.size-1
        for (int i = cellDatas.size() - 1; i >= 0; i--) {
            CellData overwriteCell = cellDatas.get(i);

            cellInputList.add(overwriteCell);
        }
        if (DEBUG) Log.d("DDP", "Successful list Add <Input>");

    }

    //Load the data into list using maxCellsGenerated as number of cells.
    //fixme: Should this really be called at all? Not like you can edit outputs anyways...Redundant?
//    public void LoadOutputArray(List<CellData> cellDatas) {
//
//        //If cellDataList is not empty, we start to clear it and prep for new data to be loaded
//        if (!cellOutputList.isEmpty()) {
//            cellOutputList.clear();
//            if(DEBUG) Log.d("DDP", "Clear list Initialize");
//        }
//
//        for (int i = 0; i < cellDatas.size(); i++) {
//            CellData overwriteCell = cellDatas.get(i);
//
//            cellOutputList.add(overwriteCell);
//        }
//        if (DEBUG) Log.d("DDP", "Successful list Add <Output>");
//    }


    public List<CellData> getCellDataList()
    {
        if (DEBUG) Log.d("DDP", "Getting cellDataList from CPU internal");
        return cellDataList;
    }

    //Should be called when you want to load in an input list from a preloaded program
    public List<CellData> getCellInputList() {
        if (DEBUG) Log.d("DDP", "Getting cellInputList from CPU internal");
        return cellInputList;
    }

    public List<CellData> getCellOutputList()
    {
        if (DEBUG) Log.d("DDP", "Getting cellOutputList from CPU internal");
        return cellOutputList;
    }

    //getSingleCellData - returns String data from <Type>List at index <location>
    @Nullable
    public String getSingleCellData(int location, List<CellData> list)
    {
        if (DEBUG) Log.d("DDP", String.format("cellDataList: %s", list));

        if (location < list.size()) {
            return list.get(location).getCellData();
        }
        else {
            return "";
        }
    }

    //Compare Arrays before we do anything, other functions will handle the size problem
    private int TestArrayInequality(List firstArray, List secondArray)
    {
        int code;

        if (firstArray.size() < secondArray.size()) {
            code = 1; //first array is smaller

            if (firstArray.isEmpty()) //Test if first array empty
            {
                code = 2;
            }

        }
        else if (firstArray.size() > secondArray.size()) {
            code = -1; //second array is smaller

            if (secondArray.isEmpty()) //Test if second array empty
            {
                code = -2;
            }
        }
        else {
            code = 0; //Equal array sizes
        }

        return code;
    }


/* OP CODE LOGIC FUNCTIONS
*       SECTION CONTAINS OP CODE LOGIC FOR INTERPRET STEP*/

    //Todo Add array checks to all
    //Logic for OPCODE 0
    //method - location @index of: data
    private void GetInputFromCell(int location)
    {

        //Reverse stack. Last card is first input
        CellData overwriteCell = null;

        //Check Inputlist if it's not empty
        if (cellInputList.size() != 0) {
            overwriteCell = cellInputList.get(cellInputList.size() - 1);
            //Set new data to memory cell at location
            //First check to see if location is greater than our cells that we currently have
            if (cellDataList.size() > location) {
                cellDataList.set(location, overwriteCell);
            }
            else //Instantiate empty cells till we reach our location
            {
                for (int i = cellDataList.size() - 1; i < location; i++) {
                    cellDataList.add(new CellData());
                }
                cellDataList.set(location, overwriteCell); //now we can add it.
            }

            cellInputList.remove(cellInputList.size() - 1);
        }
        else {
            ErrorEncountered = true;
            ErrFlagCode = 1;
            //No input cards left
        }

    }

    //Logic for OPCODE 1
    private void SendToOutputCard(int location)
    {
        CellData instance = new CellData();

        instance.setCellData(cellDataList.get(location).getCellData()); //Set cell data

        if (cellOutputList.size() == 0) {
            instance.setCellIDNumber(0); //Label will be 0 if size is 0
        }
        else {
            //Label will be size - 1 to represent true index value
            instance.setCellIDNumber(cellOutputList.size() - 1);
        }

        cellOutputList.add(instance); //Add instance to list
    }

    //Logic for OPCODE 2
    private void AddAccumulator(int location)
    {

        int numAdd;


        numAdd = Integer.parseInt(cellDataList.get(location).getCellData());

        Accumulator = this.Accumulator + numAdd;
        if (Accumulator <= 1000) {
            AccumulatorCarry++;
            Accumulator = -1000 + numAdd;
        }
    }

    //Logic for OPCODE 3
    private void SubtractAccumulator(int location)
    {

        int numSub;

        numSub = Integer.parseInt(cellDataList.get(location).getCellData());

        Accumulator = this.Accumulator - numSub;
        if (Accumulator >= -1000) {
            AccumulatorCarry++;
            Accumulator = 1000 - numSub;
        }
    }


    //OPCODE 4
    public void LoadAccumulator(int location) //TODO load accumulator
    {
        int number;

        number = Integer.parseInt(cellDataList.get(location).getCellData());

        setAccumulator(number);
    }

    //OPCODE 5
    public void StoreAccumulator(int location) //TODO store accumulator
    {
        //Grab CellData at location, set current accumulator value into it's celldata
        cellDataList.get(location).setCellData(String.valueOf(getAccumulator()));
    }

    public void JumpTo(int location)
    {
        //Cell 99 is at true index 98 in list
        cellDataList.get(cellDataList.size() - 1).setCellData(String.valueOf(getProgramCounter()));

        setProgramCounter(location);

    }

    public void TestAccumulator(int location)
    {
        if (getAccumulator() < 0) {
            setProgramCounter(location);
        }
    }

    public void ShiftAccumulator(int location)
    {
        String cellection = String.format("%012d%09d", getAccumulator(), 0);
        //Calculate x,y shift values (8xy): x = left int values, y = right int values
        int left, right;

        left = location / 10;
        right = location % 10;

        for (int i = 0; i < left; i++) {
            cellection = cellection.charAt(cellection.length() - 1) + cellection.substring(0, cellection.length() - 1);
        }

        for (int i = 0; i < right; i++) {
            cellection = cellection.charAt(0) + cellection.substring(1, cellection.length());
        }

    }

    public void HaltProgram(int location)
    {
        setProgramCounter(location);
        ErrorEncountered = true;
        ErrFlagCode = 0;
    }

    //Default Getter and Setter Functions;
    public int getAccumulator()
    {
        return Accumulator;
    }

    public void setAccumulator(int accumulator)
    {
        Accumulator = accumulator;
    }

    public int getAccumulatorCarry()
    {
        return AccumulatorCarry;
    }

    public void setAccumulatorCarry(int accumulatorCarry)
    {
        AccumulatorCarry = accumulatorCarry;
    }

    public int getInstructionRegister()
    {
        return InstructionRegister;
    }

    public void setInstructionRegister(int instructionRegister)
    {
        InstructionRegister = instructionRegister;
    }

    public int getProgramCounter()
    {
        return ProgramCounter;
    }

    public void setProgramCounter(int programCounter)
    {
        ProgramCounter = programCounter;
    }


    private void popToastS(String text)
    {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 70);
        toast.show();
    } //popToastS Advanced


    private void popToastL(String text)
    {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 90);
        toast.show();
    } //popToastL - long

}
