package main.AtMostNValue.heuristics;


import main.AtMostNValue.FacilityLocationProblem.FacilityLocationData;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

/**
 * Simple heuristic for facility location problems.
 *
 * A heuristic to choose the y_j during search depending on:
 * -- the size of the minimum domain (among the X) that can take value j
 * -- the average cost of value j if all variable that can still connect to j do so
 */
public class CPFeasAndCheap extends DFLBranching implements VariableSelector<IntVar> {

    protected IntVar[] X;
    protected IntVar[] Y;

    /**
     * @param y: facility variables
     * @param x: assignment variables
     */
    public CPFeasAndCheap(FacilityLocationData data, IntVar[] y, IntVar[] x) {
        super(data);
        X = x;
        Y = y;
    }

    @Override
    public IntVar getVariable(IntVar[] y) {
        int bestIdx = this.chooseFacilityToOpen();
        return bestIdx > -1 ? y[bestIdx] : null;
    }


    protected boolean isFacilityUnknown(int idx) {
        return !Y[idx].isInstantiated();
    }

    /**
     * @param val_idx
     * @return the size of the smallest domain that contains value idx
     */
    protected int evalValueIOnFeas(int val_idx) {
        int domSizeOfTheSmallestDomainWithIDX = Integer.MAX_VALUE;
        for (int i = 0; i < X.length; i++) {
            if (X[i].contains(val_idx) && X[i].getDomainSize() < domSizeOfTheSmallestDomainWithIDX)
                domSizeOfTheSmallestDomainWithIDX = X[i].getDomainSize();
        }
        return domSizeOfTheSmallestDomainWithIDX;
    }


    /**
     * @return the number of variables of the given domain size for which value idx is the best (cheapest) one
     */
    protected double evalValueIOnCost(int val_idx) {
        double costDomain = data.getFixedCost(val_idx);
        int nbVarToIdx = 0;
        for (int i = 0; i < X.length; i++) {
            if (X[i].contains(val_idx)) {
                costDomain += data.getSupplyCost(i,val_idx);
                nbVarToIdx++;
            }
        }
        return costDomain/nbVarToIdx;
    }
}
