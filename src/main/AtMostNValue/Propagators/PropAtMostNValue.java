package main.AtMostNValue.Propagators;

import main.AtMostNValue.DataStructure.BipartiteSet;
import main.AtMostNValue.DualSolver.DualSolver;
import main.AtMostNValue.SubGradient.GradientObject;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;

public class PropAtMostNValue extends Propagator<IntVar> {


    /**
     * X[i] is a primal variable of the problem.
     * */
    private IntVar[] X;

    /**
     * All the values in all the domains of Xs variables
     * */
    private int[] concernedValues;

    /**
     * Y[j] is a decision variable of the problem.
     * There is one Y[j] for each value in concernedValues.
     * */
    private BoolVar[] Y;

    /**
     * N and Z variables are the heterogeneity variables.
     * */
    private IntVar N;
    private IntVar Z;

    public GradientObject Grad;

    protected int numberOfStep;
    protected double threshold;

    public int n;
    public int m;

    public DualSolver dualsolver;

    protected int offSet;

    public static double MAX = Double.MAX_VALUE/10;
    public static double MIN = -MAX;





    public PropAtMostNValue(IntVar[] X, BoolVar[] Y, IntVar N, IntVar Z, int[] concernedValues, GradientObject Grad, int numberOfStep, double threshold){
        super(ArrayUtils.append(Y, X, new IntVar[]{N, Z}), PropagatorPriority.VERY_SLOW, false);
        this.X = X;
        this.Y = Y;
        this.N = N;
        this.Z = Z;
        this.concernedValues = concernedValues;
        this.Grad = Grad;
        this.numberOfStep = numberOfStep;
        this.threshold = threshold;

        this.n = X.length;
        this.m = Y.length;

        int minConcernedValues = (int) MAX;

        for(int i=0;i<m;i++){
            if(concernedValues[i]<minConcernedValues){
                minConcernedValues = concernedValues[i];
            }
        }
        this.offSet = minConcernedValues;
        this.dualsolver = new DualSolver(X, Y, offSet, N, Grad, n, m, numberOfStep, threshold);
        this.sureValues = new BitSet(m + 1);
        this.possibleValues = new BipartiteSet(m + 1);
    }



    @Override
    public void propagate(int evtmask) throws ContradictionException {
        initiateNeededValues();
        propagSumYLessEqualThanN();
        dualsolver.initRelaxation();
        dualsolver.lagrangianRelaxation();


        if (numberOfStep > 0) {
            dualsolver.localAlterationAlgorithm();
        }
        for (int j = 0; j < Y.length; j++) {
            if (Y[j].isInstantiatedTo(0)) {
                for(int i=0; i <= dualsolver.getYInXIndex()[j].last;i++){
//                assert(YInXIndex[j].last!=-1);
                    int idx = dualsolver.getYInX()[j].list[dualsolver.getYInXIndex()[j].list[i]];
//                    int idxToRemove = dualsolver.getdX()[idx].position[j];
                    if(X[idx].contains(j + offSet)) {
                        X[idx].removeValue(j + offSet, this);
                    }
                }
            }
        }
        filterFromEndSupport();
    }

    @Override
    public ESat isEntailed() {
        if(nbYtoOne==N.getUB()){
            return ESat.TRUE;
        }
        if(nbYtoOne>N.getUB()){
            return ESat.FALSE;
        }
//
////        return ESat.FALSE;
        return ESat.UNDEFINED;
//        return ESat.TRUE;
    }

    //*****************************************************//
    //*********** Process domains *************************//
    //*****************************************************//


    protected BitSet sureValues;
    protected BipartiteSet possibleValues;
    protected int nbYtoOne;
    protected int maxNbYtoOne;
    protected int lbSumOfWeights;

    public void initiateNeededValues() {
        sureValues.clear();
        possibleValues.clear();
        lbSumOfWeights = 0;
        for (int j = 0; j < Y.length; j++) {
            if (Y[j].isInstantiatedTo(1)) {
                sureValues.set(j);
                lbSumOfWeights += 1.0;
                updatePossibleFromVal(j);
            } else if (!Y[j].isInstantiated()) {
                updatePossibleFromVal(j);
            }
        }
        nbYtoOne = sureValues.cardinality();
        maxNbYtoOne = possibleValues.size();
    }

    public void filterFromEndSupport() throws ContradictionException {
        double lb = dualsolver.getLB();
        Z.updateLowerBound((int) Math.ceil(lb), this);
        for (int idx = 0; idx < Y.length; idx++) {
            BipartiteSet fvalues = dualsolver.getLFValues(idx);
            if (fvalues != null) {
                for (int i = 0; i < fvalues.size(); i++) {
                    int val = fvalues.list[i];
                    if (!Y[idx].isInstantiated()) {
                        Y[idx].removeValue(val, this);
                        updateFromRemoval(idx, val, false);
                    }
                }
            }
        }
        propagSumYLessEqualThanN();
    }

    private void updateFromRemoval(int idxVal, int val, boolean triggerPropag) throws ContradictionException {//System.out.println("(PropNValueGlobal 268)");
        if (val == 1) {
            maxNbYtoOne--;
            possibleValues.remove(idxVal);
        } else if (val == 0) {
            nbYtoOne++;
            lbSumOfWeights += 1.0;
            sureValues.set(idxVal);
            updatePossibleFromVal(idxVal);
        }
        if (triggerPropag) propagSumYLessEqualThanN();
    }
    private final void updatePossibleFromVal(int val) {
        if (!possibleValues.contain(val))
            possibleValues.add(val);
    }

    private void propagSumYLessEqualThanN() throws ContradictionException {
        N.updateLowerBound(nbYtoOne, this);
        N.updateUpperBound(maxNbYtoOne, this);
        if (nbYtoOne == N.getUB()) {
            for (int j = 0; j < Y.length; j++) {
                if (!Y[j].isInstantiated()) {
                    Y[j].removeValue(1, this);
                    for (int i = 0; i < X.length; i++) {
                        X[i].removeValue(j, this);
                    }
                    updateFromRemoval(j, 1, false);
                }
            }
        }
    }

    private void printX3(int number){
        for(int i=0; i<n;i++){
            if(X[i].getLB() < 10){
                System.out.print(X[i].getLB()+" \t");
            } else {
                System.out.print(X[i].getLB()+"\t");
            }

            if(i%(number)==(number - 1)){
                System.out.println();
            }
        }
        System.out.println();
    }
    private void printX(){
        for(int i=0; i<n;i++){
            System.out.println(X[i]);

        }
    }

}
