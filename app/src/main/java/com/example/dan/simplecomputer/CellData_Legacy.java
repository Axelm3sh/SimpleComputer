package com.example.dan.simplecomputer;

/**
 * Created by DANIEL on 10/28/2015.
 * Simple Class object that stores data from cells into data
 *
 */
@Deprecated
public class CellData_Legacy
{
    private int cellIDNumber;
    private String cellData;

    public CellData_Legacy()
    {
        this.cellIDNumber = 0;
        this.cellData = "";
    }

    /*---------------DATA SETTERS-----------------------------*/

    public void setCellData(String cellData)
    {
        this.cellData = cellData;
    }
    public void setCellIDNumber(Integer cellIDNumber)
    {
        this.cellIDNumber = cellIDNumber;
    }

/*---------------DATA GETTERS-----------------------------*/

    public String getCellData()
    {
        return cellData;
    }

    public Integer getCellIDNumber()
    {
        return cellIDNumber;
    }
}
