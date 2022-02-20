package org.capstoneresearch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

public class RunnableSimulator implements Runnable
{
    private double producerDiscount;
    private double batteryCycles;
    private double tripDistance;
    private double truckPrice;
    private List<Double> maxProfit= new ArrayList<>();
    private static AtomicInteger counter = new AtomicInteger(1);
    private static CellStyle style = MainSimulator.getWorkbook().createCellStyle();

    public RunnableSimulator(double producerDiscount, double batteryCycles, double tripDistance, double truckPrice)
    {
        this.producerDiscount = producerDiscount;
        this.batteryCycles = batteryCycles;
        this.tripDistance = tripDistance;
        this.truckPrice = truckPrice;
        maxProfit.add(0.0);
        maxProfit.add(0.0);
    }

    public void run()
    {
        for (int numBatteries = 1; numBatteries < 2001; numBatteries++)
            {
                System.out.println(numBatteries);
                List<Double> statsList = MainSimulator.simulateSupplyChain(tripDistance, 0.185, 38000, 1.273148148, 475 * 3600, producerDiscount, numBatteries, 100, batteryCycles, truckPrice);
                if (statsList.get(0) > maxProfit.get(0))
                {
                    maxProfit = statsList;
                }
                else
                {
                    break;
                }
            }
            System.out.println(maxProfit);
            Row row = MainSimulator.getSheet().createRow(RunnableSimulator.counter.get());

            Cell cell = row.createCell(0);
            cell.setCellValue(tripDistance);
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue(maxProfit.get(0));
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue(maxProfit.get(1));
            cell.setCellStyle(style);
            RunnableSimulator.counter.set(RunnableSimulator.counter.get() + 1);
            maxProfit.set(0, 0.0);
            maxProfit.set(1, 0.0);
    }
}
