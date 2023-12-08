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
//        System.out.println(this.offSet);
        this.dualsolver = new DualSolver(X, Y, offSet, N, Grad, n, m, numberOfStep, threshold);



        this.sureValues = new BitSet(m + 1);//System.out.println("(PropNValueGlobal 134) sureValues : "+sureValues);
        this.possibleValues = new BipartiteSet(m + 1);
//        for(int i = 0; i < n; i++) {
//            System.out.println("X["+i+"], "+X[i].getDomainSize());
//        }

    }



    @Override
    public void propagate(int evtmask) throws ContradictionException {
//        System.out.println("(97 propagate "+ dualsolver.iterTot +" ) "+model.getSolver().getTimeCount());
        initiateNeededValues();
        propagSumYLessEqualThanN();
//        if (lbSumOfWeights > Z.getUB()) {
////            contradiction(Z, "");
//            Z.updateLowerBound((int)Math.ceil(lbSumOfWeights), this);
////            Z.getModel().getSolver().getEngine().flush();
//        }
//        X[0].removeValue(offSet + 1, this);
//        Y[2].setToFalse(this);
//        Y[10].setToTrue(this);
//        X[1].instantiateTo(offSet + 29, this);
//        if(X[6].isInstantiated()){
//            System.out.println("ici");
//        }
        dualsolver.initRelaxation();
        dualsolver.lagrangianRelaxation();


        if (numberOfStep > 0) {
//            System.out.println("Hello");
            dualsolver.localAlterationAlgorithm();
        }
//        printX();
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
//        if (numberOfStep > 0) {
////            System.out.println("Hello");
//            dualsolver.AlgoFred();
//        }
        filterFromEndSupport();
//        printX();

//        System.out.println("(140 propagate"+ dualsolver.iterTot +") "+model.getSolver().getTimeCount());
//        System.out.println("----------------------------------------------------------------");
    }

    @Override
    public ESat isEntailed() {
        if(nbYtoOne==N.getUB()){
//            System.out.println(fixedYiTo1.prettyFred());
//            System.out.println(fixedYiTo0.prettyFred());
//            printX3(7);
//            printY2(10);
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

    public void filterFromEndSupport() throws ContradictionException {//System.out.println("(PropNValueGlobal 451)");
        double lb = dualsolver.getLB();
        Z.updateLowerBound((int) Math.ceil(lb), this);
        for (int idx = 0; idx < Y.length; idx++) {
            BipartiteSet fvalues = dualsolver.getLFValues(idx);
            if (fvalues != null) {//System.out.println("(PropNValueGlobal 456)");
                for (int i = 0; i < fvalues.size(); i++) {
                    int val = fvalues.list[i];
                    if (!Y[idx].isInstantiated()) {//System.out.println("(PropNValueGlobal 459)");
                        //System.out.println("remove value " + val + " from "+Yj[idx]);
                        Y[idx].removeValue(val, this);
                        updateFromRemoval(idx, val, false);
//                        System.out.println("HereXYZZZ");
                    }
                }
            }
        }
//        if (!weighted) {//filter the lower current_bound of N weighted est à vrai donc on ne rentre pas
//            N.updateLowerBound(relax.getLowerBoundN(), this);
//        }
//        N.updateLowerBound(relax.getLowerBoundN(), this);
        propagSumYLessEqualThanN();
        //System.out.println("NB Values: " + valuesPruned.size() + " " + valuesPruned.toString());
    }

    private void updateFromRemoval(int idxVal, int val, boolean triggerPropag) throws ContradictionException {//System.out.println("(PropNValueGlobal 268)");
        if (val == 1) {//System.out.println("(PropNValueGlobal 269)");
            maxNbYtoOne--;
            possibleValues.remove(idxVal);
//            System.out.println("HereXYZ2 " + idxVal);
        } else if (val == 0) {//System.out.println("(PropNValueGlobal 273)");
            nbYtoOne++;
            lbSumOfWeights += 1.0;
            sureValues.set(idxVal);
//            System.out.println("HereXYZ3 " + idxVal);
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
        /*if (maxNbYtoOne == N.getLB() && maxNbYtoOne != nbYtoOne) {  //<hca> this is propagating Sum_i Y_i = N ...
            for (int j = 0; j < Yj.length; j++) {
                if (!Yj[j].isInstantiated()) {
                    Yj[j].removeValue(0, aCause);
                    updateFromRemoval(j, 0, false);
                }
            }
        } else */
        if (nbYtoOne == N.getUB()) {//System.out.println("(PropNValueGlobal 255)");
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
//        System.out.print("X = [");
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
//        System.out.println("]");
    }
    private void printX(){
//        System.out.print("X = [");
        for(int i=0; i<n;i++){
            System.out.println(X[i]);
//        System.out.println("]");
        }
    }

}
