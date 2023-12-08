package main.AtMostNValue.heuristics;


import main.AtMostNValue.FacilityLocationProblem.FacilityLocationData;

/**
 * Generic class for branching in facility locations problems
 */
public abstract class DFLBranching {

    protected FacilityLocationData data;

    protected DFLBranching(FacilityLocationData data) {
        this.data = data;
    }

    protected int chooseFacilityToOpen() {
        int bestIdx = -1;
        double bestEval = Double.MAX_VALUE;
        double secBestEval = Double.MAX_VALUE;
        for (int idx = 0; idx < data.getNbW(); idx++) {
            if (isFacilityUnknown(idx)) {
                int evalSmallestDom = evalValueIOnFeas(idx);
                if (evalSmallestDom < bestEval) {
                    bestIdx = idx;
                    bestEval = evalSmallestDom;
                    secBestEval = evalValueIOnCost(idx);
                } else if (evalSmallestDom == bestEval) {
                    double secEval = evalValueIOnCost(idx);
                    if (secEval < secBestEval) {
                        bestIdx = idx;
                        secBestEval = secEval;
                    }
                }
            }
        }
        return bestIdx;
    }

    protected abstract boolean isFacilityUnknown(int idx);

    /**
     * @param val_idx
     * @return the size of the smallest domain that contains value idx
     */
    protected abstract int evalValueIOnFeas(int val_idx);

    /**
     * @return the number of variables of the given domain size for which value idx is the best (cheapest) one
     */
    protected abstract double evalValueIOnCost(int val_idx);


}

