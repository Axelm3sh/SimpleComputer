package com.example.dan.simplecomputer;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.util.List;

/**
 * CPU Emulator
 * The core of the processing unit of emulator
 */
public class CPUHandler extends Activity
{

    private static final boolean DEBUG = true;
    private static final String VERBOSE = "DDP";

    MemoryCell memoryCell, inputCell, outputCell;

    private int Accumulator, AccumulatorCarry, InstructionRegister, ProgramCounter, InputIndex;

    /* -1 - unexpected error, 1 - No more input cards, 2 - instruction card empty, 3 - infinite loop*/
    boolean ErrorEncountered = false;
    int ErrFlagCode = 0;
    String ErrStr = "";


    //Constructor
    public CPUHandler(MemoryCell memory, MemoryCell input, MemoryCell output)
    {
        if (DEBUG) Log.d("DDP", "Inside Constructor CPU CLASS");

        this.ProgramCounter = 0;
        this.AccumulatorCarry = 0;
        this.Accumulator = 0;
        this.InstructionRegister = 0;
        this.InputIndex = 0;

        this.memoryCell = memory;
        this.inputCell = input;
        this.outputCell = output;

        // Data will be generated after calling Step-time
    }

    //ClearCPU Function: Sets internal values to 0
    public void ClearCPU()
    {
        setAccumulator(0);
        setAccumulatorCarry(0);
        setInstructionRegister(0);
        setProgramCounter("0");
        InputIndex = 0;
        ErrorEncountered = false;
        ErrFlagCode = 0;

//        cellOutputList.clear();
    }

    public boolean CheckError()
    {
        if (ErrorEncountered) {
            this.ErrStr = ErrorCode(ErrFlagCode);
            InputIndex = 0; //Reset input cards if we encounter any stop
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
                return "Error: No Input Card!";
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

        String step = memoryCell.GetCellData(pc);

        if (DEBUG) Log.d(VERBOSE, String.format("Step is %s", step));

        //Check for null string/empty string
        if (!step.equals("")) {
            setInstructionRegister(Integer.parseInt(step));
        }
        else {
            ErrorEncountered = true;
            ErrFlagCode = 2;
        }
        setProgramCounter(String.valueOf(getProgramCounter() + 1)); //Increment PC by 1

        if (DEBUG) Log.d(VERBOSE, String.format("Internal %d", getProgramCounter()));

        /*Decode Cycle
        *   - decode the op-code part of the instruction in the IR*/
        InterpretCurrentInstruction(step);
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
        String temp = inputCell.GetCellData(InputIndex);

            //Handles if we hit empty input card
            if (temp.equals("")) {
                //Blank Input, Advance input device, set PC to 00, halt CPU
                ErrorEncountered = true;
                ErrFlagCode = 1;
                setProgramCounter("0");
                InputIndex++;
            }
            else {
                memoryCell.ChangeCell(location, temp);
                InputIndex++; //Move the inputcards up by 1 for next.
            }

    }

    //Logic for OPCODE 1
    private void SendToOutputCard(int location)
    {
        String instance = memoryCell.GetCellData(location);

        outputCell.AddCell();
        outputCell.ChangeCell(outputCell.returnNumCellsGenerated()-1,instance);


    }

    //Logic for OPCODE 2
    private void AddAccumulator(int location)
    {

        int numAdd;

        if(!memoryCell.GetCellData(location).equals("")) {
            numAdd = Integer.parseInt(memoryCell.GetCellData(location));
        }
        else
        {
            numAdd = 0;
            ErrFlagCode = 2;
            ErrorEncountered = true;
        }

        Accumulator = Accumulator + numAdd;
        if (Accumulator <= 1000) {
            AccumulatorCarry++;
            Accumulator = -1000 + numAdd;
        }
    }

    //Logic for OPCODE 3
    private void SubtractAccumulator(int location)
    {

        int numSub;

        if(!memoryCell.GetCellData(location).equals("")) {
            numSub = Integer.parseInt(memoryCell.GetCellData(location));
        }
        else
        {
            numSub = 0;
            ErrFlagCode = 2;
            ErrorEncountered = true;
        }

        Accumulator = Accumulator - numSub;
        if (Accumulator >= -1000) {
            AccumulatorCarry++;
            Accumulator = 1000 - numSub;
        }
    }


    //OPCODE 4
    public void LoadAccumulator(int location) //TODO load accumulator
    {
        int number;

        if(!memoryCell.GetCellData(location).equals("")) {
            number = Integer.parseInt(memoryCell.GetCellData(location));
        }
        else
        {
            number = -1;
            ErrFlagCode = 2;
            ErrorEncountered = true;
        }

        setAccumulator(number);
    }

    //OPCODE 5
    public void StoreAccumulator(int location) //TODO store accumulator
    {
        //Grab Data at location, set current accumulator value into it's data
        if (location != 0) { //Dont edit 00
            memoryCell.ChangeCell(location, String.valueOf(getAccumulator()));
        }
    }

    public void JumpTo(int location)
    {
        //Cell 99 is at true index 98 in list
        memoryCell.ChangeCell(99, String.valueOf(getProgramCounter()));

        setProgramCounter(String.valueOf(location));

    }

    public void TestAccumulator(int location)
    {
        if (getAccumulator() < 0) {
            setProgramCounter(String.valueOf(location));
        }
    }

    public void ShiftAccumulator(int location)
    {
        //000000000xxx000000000
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

        setAccumulator(Integer.parseInt(cellection.substring(9,11)));

    }

    public void HaltProgram(int location)
    {
        setProgramCounter(String.valueOf(location));
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

    public void setProgramCounter(String programCounter)
    {
        if (programCounter.equals(""))
        {
            ProgramCounter = 0;
        }
        else {
            ProgramCounter = Integer.parseInt(programCounter);
        }
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

