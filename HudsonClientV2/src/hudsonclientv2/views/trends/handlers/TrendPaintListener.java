package hudsonclientv2.views.trends.handlers;

import hudsonclientv2.bo.ResultsTests;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.Chart;
import org.swtchart.IAxisSet;
import org.swtchart.IGrid;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

public class TrendPaintListener implements PaintListener {

    private Chart chart;

    private List<ResultsTests> resultsToDraw;

    public TrendPaintListener() {
    }

    public void paintControl(PaintEvent e) {
	Collections.reverse(resultsToDraw);
	Shell shell = e.display.getActiveShell();
	shell.setLayout(new FillLayout());
	chart = new Chart(shell, SWT.NONE);
	chart.getTitle().setText("Test Results Trend");
	double[] errorsSeries = new double[resultsToDraw.size()];
	double[] ignoreSeries = new double[resultsToDraw.size()];
	double[] passedSeries = new double[resultsToDraw.size()];
	double[] buildsSeries = new double[resultsToDraw.size()];
	String[] buildsSeriesCat = new String[resultsToDraw.size()];
	int i = 0;
	for (final ResultsTests resultsTests : resultsToDraw) {
	    buildsSeries[i] = resultsTests.getBuildNumber();
	    buildsSeriesCat[i] = String.valueOf(resultsTests.getBuildNumber());
	    errorsSeries[i] = resultsTests.getErrorsCount();
	    ignoreSeries[i] = resultsTests.getIgnoreCount();
	    passedSeries[i] = resultsTests.getTotalCount() - ignoreSeries[i] - errorsSeries[i];
	    i++;
	}

	// create line series
	//TODO extract some charts utilities overlays --> See alpha
	ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "errors");
	lineSeries.setYSeries(errorsSeries);
	lineSeries.setXSeries(buildsSeries);
	lineSeries.setLineColor(new Color(e.display, 255, 0, 0));
	lineSeries.enableArea(true);
	ILineSeries lineSeries1 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "ignore");
	lineSeries1.setYSeries(ignoreSeries);
	lineSeries1.setXSeries(buildsSeries);
	lineSeries1.setLineColor(new Color(e.display, 0, 0, 0));
	lineSeries1.enableArea(true);
	ILineSeries lineSeries2 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "ok");
	lineSeries2.setYSeries(passedSeries);
	lineSeries2.setXSeries(buildsSeries);
	lineSeries2.setLineColor(new Color(e.display, 0, 255, 0));
	lineSeries2.enableArea(true);
	
	chart.getSeriesSet().bringForward("ok");
	chart.getSeriesSet().bringForward("errors");
	chart.getSeriesSet().bringForward("ignore");

	IAxisSet axisSet = chart.getAxisSet();
	axisSet.getXAxis(0).getTitle().setText("Build Number");
	axisSet.getXAxis(0).setCategorySeries(buildsSeriesCat);
	axisSet.getXAxis(0).enableCategory(true);
	
	axisSet.getYAxis(0).getTitle().setText("Tests");
	// adjust the axis range
	axisSet.adjustRange();

	IGrid xGrid = axisSet.getXAxis(0).getGrid();
	xGrid.setStyle(LineStyle.NONE);
	Color color = new Color(Display.getDefault(), 255, 0, 0);
	xGrid.setForeground(color);
    }

    public List<ResultsTests> getResultsToDraw() {
	return resultsToDraw;
    }

    public void setResultsToDraw(List<ResultsTests> resultsToDraw) {
	this.resultsToDraw = resultsToDraw;
    }

}
