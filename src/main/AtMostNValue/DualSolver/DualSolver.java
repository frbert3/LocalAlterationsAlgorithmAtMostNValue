package main.AtMostNValue.DualSolver;


import main.AtMostNValue.DataStructure.BipartiteSet;
import main.AtMostNValue.SubGradient.*;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Locale;

public class DualSolver {

    IntVar[] X;

    BoolVar[] Y;

    IntVar N;

    protected final BipartiteSet[] dX;
    protected BipartiteSet[] dXIndex;
    protected final BipartiteSet[] YInX;

    protected BipartiteSet[] YInXIndex;

    protected GradientObject grad;


    protected int n;
    protected int m;

    protected int offSet;

    protected BipartiteSet[] lfValues;

    protected double[] lagrangeMultipliers;
    protected double[] optimalLagrangeMultipliers;
    public double[] qVal;
    protected double LB;
    protected double bestLB;
    protected BipartiteSet fixedYiTo1;
    public BipartiteSet notInstanciatedValues;
    public int[] y;
    protected int[] nbYSetToOneInDom;

    private double ZERO = 1e-4d;

    private boolean stop = false;

    public int iterTot;
    public int iter;
    public int bestIter;

    public double[] subgrad;

    public static double MAX = Double.MAX_VALUE/10;
    public static double MIN = -MAX;

    public static double MIN_CHANGE = 0.0001;
    private double[] lagrangeMultipliersLocal;
    private double[] qValLocal;
    private double LBLocal;
    public double[] gradLocal;
    private int[] yLocal;


    protected int[] nbYSetToOneInDomLocal;
    private int numberOfStep;
    private double threshold;


    double[] fGradJ;

    private HarmonicSubGradient harmonicSubGradient = null;
    private GeometricSubGradient geometricSubGradient =null;
    private NewtonSubGradient newtonSubGradient =null;

    private GradientObject gradientObject;

    private NewtonConstantSubGradient newtonConstantSubGradient = null;

    public DualSolver(IntVar[] X, BoolVar[] Y, int offSet, IntVar N, GradientObject grad, int n, int m, int numberOfStep, double threshold){
        this.X = X;
        this.Y = Y;
        this.N = N;
        this.n = n;
        this.m = m;
        this.offSet = offSet;
        this.dX = new BipartiteSet[n];
        this.dXIndex = new BipartiteSet[n];
        this.YInX = new BipartiteSet[m];
        this.YInXIndex = new BipartiteSet[m];
        this.lfValues = new BipartiteSet[m];
        this.fixedYiTo1 = new BipartiteSet(m);
        this.notInstanciatedValues = new BipartiteSet(m);
        this.lagrangeMultipliers = new double[n];
        Arrays.fill(lagrangeMultipliers, 0.0d);
        this.optimalLagrangeMultipliers = new double[n];
        this.qVal = new double[m];
        this.y = new int[m];
        this.LB = 0.0d;
        this.bestLB = 0.0d;
        this.iterTot = 0;
        this.subgrad = new double[n];
        this.grad = grad;
        this.iter = 0;
        this.bestIter = 0;
        initializeDataStructure();
        this.lagrangeMultipliersLocal = new double[n];
        this.qValLocal = new double[m];
        this.gradLocal = new double[n];
        this.nbYSetToOneInDomLocal = new int[n];
        this.yLocal = new int[m];
        this.LBLocal = LB;
        this.numberOfStep = numberOfStep;
        this.threshold = threshold;
        this.fGradJ = new double[n];
        this.nbYSetToOneInDom = new int[n];
        this.gradientObject = grad;
        switch (gradientObject.method) {
            case Newton -> newtonSubGradient = new NewtonSubGradient(grad);
            case Geometric -> geometricSubGradient = new GeometricSubGradient(grad);
            case Harmonic -> harmonicSubGradient = new HarmonicSubGradient(grad);
        }

        this.newtonConstantSubGradient = new NewtonConstantSubGradient(new GradientObject(GradientMethod.Newton, 1.0, 1.97));
    }

    public void initRelaxation() {
        resetDomain();
    }

    public void lagrangianRelaxation(){
        resetToBestValue();
        resetFixedAndUnfixed();
        this.iter = 0;
        this.stop = false;
        while(iter < 1000 && !stop) {
            Arrays.fill(nbYSetToOneInDom,0);
            prepareReducedCost();
            if(LB <= N.getUB()) {
                gatherFValues(LB, N.getUB());
                for(int j = 0; j <= notInstanciatedValues.last; j++) {
                    filteringJValueAlgo(notInstanciatedValues.list[j]);
                }
                for(int i = 0; i < dXIndex.length; i++){
                    if(dXIndex[i].size() == 1) {
                        y[dX[i].list[dXIndex[i].list[0]]] = 1;
                    }
                }
                computeSubGrad();
                saveOptimalLambda(LB > bestLB);
                iter++;
                iterTot++;

                stop = !lagrangeMultipliersOptimization(iter) || (LB > N.getUB());
            }else{
                computeSubGrad();
                iter++;
                iterTot++;
                stop=!lagrangeMultipliersOptimization(iter) || (LB > N.getUB());}

        }

    }

    protected void computeSubGrad(){
        Arrays.fill(subgrad, 1.0d);
        for(int i = 0; i < n; i++) {
            subgrad[i] -= nbYSetToOneInDom[i];
        }
    }

    public void addValInS(int val) {
        //System.out.println("(SubPbNValue 171) dom.notNullXij["+val+"] = " + dom.notNullXij[val].pretty());
        for (int k = 0; k <= YInX[val].last; k++) {
            int var = YInX[val].list[k];
            nbYSetToOneInDom[var]++;
            //<hca> something very subtle is going on here...
            //the following code would build a dualsolver by ignoring variables i with a null contribution to value val
            //this seems to converge much faster...
//            if (dualizedCstrs[var].getLambda() > 0) nbYSetToOneInDom[var]++;
//            if (lagrangeMultipliers[var] > 0) nbYSetToOneInDom[var]++;
        }
    }

    private boolean lagrangeMultipliersOptimization(final int iter){
        boolean valid = true;
        boolean change = false;
        boolean gradBool = true;
        if((gradientObject.method == GradientMethod.Newton)) {
            newtonSubGradient.reset(N.getUB(), LB, lagrangeMultipliers, nbYSetToOneInDom);
        }
        switch (gradientObject.method) {
            case Newton -> gradBool = newtonSubGradient.coefficient(iter);
            case Geometric -> gradBool = geometricSubGradient.coefficient(iter);
            case Harmonic -> gradBool = harmonicSubGradient.coefficient(iter);
        }
        double muk = 0.0d;

        switch (gradientObject.method) {
            case Newton -> muk = newtonSubGradient.getMuk();
            case Geometric -> muk = geometricSubGradient.getMuk();
            case Harmonic -> muk = harmonicSubGradient.getMuk();
        }
        for(int i=0; i<n;i++){
            if((1.0 - nbYSetToOneInDom[i]) != 0){
                if(gradBool){
                    change |= addLambda(i, muk * (1.0 - nbYSetToOneInDom[i]));
                    valid = false;
                }
                else{
                    // such has division by 0 in  Newton.
                    return false;
                }
            }
        }
        return (!valid && change);
    }

    protected boolean addLambda(int idx, double v){
        double valueZeroH = 0.0001;
        double prev = lagrangeMultipliers[idx];
        lagrangeMultipliers[idx] = lagrangeMultipliers[idx] + v;
        lagrangeMultipliers[idx] = Math.min(MAX,lagrangeMultipliers[idx]);
        lagrangeMultipliers[idx] = Math.max(MIN,lagrangeMultipliers[idx]);
        lagrangeMultipliers[idx] = Math.max(0, lagrangeMultipliers[idx]);
        if(Math.abs(lagrangeMultipliers[idx] - prev) < valueZeroH){ //changes below some thresholds (MIN_CHANGE) are not considered (to avoid big slowdown convergence issues...)
            lagrangeMultipliers[idx] = prev;
            return false;
        }
        return true;
    }

    protected void filteringJValueAlgo(int j) {
        if((y[j] == 0) && ((LB + qVal[j]) - N.getUB() > 0)) {
            if(!lfValues[j].contain(0) && lfValues[j].last !=0) {
                lfValues[j].add(0);
            }
        } else if (y[j] == 1) {
            if(LB - qVal[j]-N.getUB()>0 ){
                if(!lfValues[j].contain(1) && lfValues[j].last !=0) {
                    lfValues[j].add(1);
                }
            }
        }
    }

    protected void saveOptimalLambda(boolean save) {
        if(save) {
            bestLB=LB;
            bestIter = iter;
            for(int i=0;i<n;i++){
                optimalLagrangeMultipliers[i]=lagrangeMultipliers[i];
//            optimalSubGrad[i]=subGrad[i];
            }
        }
    }

    protected void resetToBestValue() {
//        LB = bestLB;
        LB = 0.0d;
        iter = 0;
        for(int i=0;i<n;i++){
            lagrangeMultipliers[i]=optimalLagrangeMultipliers[i];
//            optimalSubGrad[i]=subGrad[i];
        }
    }

    protected void resetFixedAndUnfixed() {
        fixedYiTo1.clear();
        notInstanciatedValues.full();
        for(int j = 0; j < m ; j++) {
            if(Y[j].isInstantiated()){
                if(Y[j].isInstantiatedTo(1) && !fixedYiTo1.contain(j)) {
                    fixedYiTo1.add(j);
                }
                if(notInstanciatedValues.contain(j)) {
                    notInstanciatedValues.remove(j);
                }
            }
        }
    }

    protected void initializeDataStructure(){
        initYInX();
        initdXAndDXIndexAndAdjustYInX();
        initYInXIndexAndLFValues();

        fixedYiTo1.clear();
        notInstanciatedValues.full();

    }
    protected void initYInX() {
        for(int j = 0; j < m; j++){
            YInX[j] = new BipartiteSet(n);
            YInX[j].clear();
        }
    }
    protected void initdXAndDXIndexAndAdjustYInX() {
        for(int i = 0; i < n; i++){
            dX[i] = new BipartiteSet(m);
            dX[i].clear();
            dXIndex[i] = new BipartiteSet(X[i].getDomainSize());
            int lb = X[i].getLB();
            int ub = X[i].getUB();
            for(int val = lb; val <= ub; val = X[i].nextValue(val)){
                int j = val - offSet;
                dX[i].add(j);
                if(!YInX[j].contain(i)){
                    YInX[j].add(i);
                }
            }

        }
    }

    protected void initYInXIndexAndLFValues() {
        for(int j = 0; j < m; j++) {
            if(YInX[j].last >= 0){
                YInXIndex[j] = new BipartiteSet( YInX[j].size());
            }else {
                YInXIndex[j] = new BipartiteSet(1);
                YInXIndex[j].clear();
                notInstanciatedValues.remove(j);
            }
            lfValues[j] = new BipartiteSet(2);
            lfValues[j].clear();
        }
    }

    protected void resetDomain() {
        for(int i = 0; i < n; i++) {
            dXIndex[i].clear();
            for(int idxValue = 0; idxValue <= dX[i].last; idxValue++){
                if(X[i].contains(dX[i].list[idxValue] + offSet)) {
                    dXIndex[i].add(idxValue);
                }
            }
            if(X[i].isInstantiated()){
                if(!fixedYiTo1.contain(X[i].getValue() - offSet)){
                    fixedYiTo1.add(X[i].getValue() - offSet);
                    if(notInstanciatedValues.contain(X[i].getValue() - offSet)) {
                        notInstanciatedValues.remove(X[i].getValue() - offSet);
                    }
                }
            }
        }
        for(int j = 0; j < m; j++) {
            lfValues[j].clear();
            YInXIndex[j].clear();
            for(int idxValue = 0; idxValue <= YInX[j].last; idxValue++){
                if(X[YInX[j].list[idxValue]].contains(j + offSet)) {
                    YInXIndex[j].add(idxValue);
                }
            }
        }

        for(int j = 0; j < m; j++) {
            if(Y[j].isInstantiatedTo(0)) {
                for(int i=0; i<=YInXIndex[j].last;i++){
//                assert(YInXIndex[j].last!=-1);
                    int idx = YInX[j].list[YInXIndex[j].list[i]];
                    int idxToRemove = dX[idx].position[j];
                    if(dXIndex[idx].contain(idxToRemove)) {
                        dXIndex[idx].remove(idxToRemove);
                    }
                }
                YInXIndex[j].clear();
                if(notInstanciatedValues.contain(j)) {
                    notInstanciatedValues.remove(j);
                }
            } else {
                if(Y[j].isInstantiatedTo(1)) {
                    y[j] = 1;
                    if(!fixedYiTo1.contain(j)) {
                        fixedYiTo1.add(j);
                        if(notInstanciatedValues.contain(j)) {
                            notInstanciatedValues.remove(j);
                        }
                    }
                }
            }
        }
    }

    protected void prepareReducedCost() {
        LB = 0.0d;
        Arrays.fill(qVal, 1.0);
        Arrays.fill(y, 0);
        Arrays.fill(nbYSetToOneInDom,0);
        double cste = 0.0d;
        double LHScoef = 1.0;
        double RHS = 1.0;
        double obj = 0.0d;
        double objFixed = 0.0d;
        cste = computeCsteAndUpdateCostFunction(RHS,LHScoef);

        double objUnfixed = 0.0d;

        objFixed = computeBenefitOfFixedValue();
        objUnfixed = computeBenefitOfUnfixedValues();

        obj = objFixed + objUnfixed;

        LB = obj + cste - ZERO;

    }

    private void updateCost(int var_index, double LHScoef){
        for(int idx = 0; idx <= dXIndex[var_index].last; idx++) {
            int val = dX[var_index].list[dXIndex[var_index].list[idx]];
            qVal[val] -= lagrangeMultipliers[var_index] ;
        }
    }

    public double computeCsteAndUpdateCostFunction(double RHS, double LHScoef) {
        double cste = 0;
        for(int i = 0; i < n; i++){
            cste += lagrangeMultipliers[i];
            updateCost(i, LHScoef);
        }
        return cste;
    }

    public double computeBenefitOfFixedValue(){
        double objFixed = 0;
        for(int j = 0; j <= fixedYiTo1.last; j ++) {
            objFixed += qVal[fixedYiTo1.list[j]];
            y[fixedYiTo1.list[j]] = 1;
            addValInS(fixedYiTo1.list[j]);
        }
        return objFixed;
    }

    public double computeBenefitOfUnfixedValues() {
        double objUnfixed = 0.0d;
        for(int j = 0; j <= notInstanciatedValues.last; j++){
            if(qVal[notInstanciatedValues.list[j]] <= 0) {
                objUnfixed += qVal[notInstanciatedValues.list[j]];
                addValInS(notInstanciatedValues.list[j]);
                if(qVal[notInstanciatedValues.list[j]] < 0){
                    y[notInstanciatedValues.list[j]] = 1;
                }
            } else {
                y[notInstanciatedValues.list[j]] = 0;
            }
        }
        return objUnfixed;
    }

    public void gatherFValues(double lb, double bestUB) {
        for (int jIdx = 0; jIdx <= notInstanciatedValues.last; jIdx++) {
            int j = notInstanciatedValues.list[jIdx];
            for (int val = 0; val <= 1; val++) { //in this case we restrict the filtering to the Y variables
                if (!lfValues[j].contain(val)) {
                    if ((lb + getRC(j,val)) > bestUB) {
                        lfValues[j].add(val);
                    }
                }
            }
        }
    }

    public void localAlterationAlgorithm(){

        saveState();
        if (numberOfStep > 0){
            for (int j1 = 0; j1 <= notInstanciatedValues.last; j1++) {
                int j = notInstanciatedValues.list[j1];
                double DistanceFromFiltering = N.getUB() - (LB + Math.abs(qVal[j]));
                boolean isOptimizationWorth = (DistanceFromFiltering <= threshold) && (DistanceFromFiltering >=0);
                if (isOptimizationWorth) {
                    int k = 0;
                    boolean goOn = true;
                    while (k <= numberOfStep && goOn ) {
                        if(k == 0) {
                            computeFGrad(j);
                        }
                        goOn = lagrangeMultipliersOptimizationLocalAlterations(k, 2.0, 1.0, N.getUB(), LB + Math.abs(qVal[j]), fGradJ, lagrangeMultipliers, GradientMethod.Newton);
                        if (goOn) {
                            prepareReducedCost();
                            if(LB <= N.getUB()) {
                                gatherFValues(LB, N.getUB());
                                filteringJValueAlgo(j);
                            }
                            computeSubGrad();
                            if((LB + Math.abs(qVal[j]) == N.getUB())) {//Avoid the local maximum
                                System.arraycopy(subgrad, 0, fGradJ, 0, n);
                            } else{
                                computeFGrad(j);
                            }
                        }
                        goOn = goOn && !(Math.abs(qVal[j]) + LB - N.getUB() > ZERO);
                        k++;
                    }
                    RestoreState();

                }
            }
        }
    }
    private void RestoreState() {
        System.arraycopy(lagrangeMultipliersLocal, 0, lagrangeMultipliers, 0, n);
        System.arraycopy(gradLocal, 0, subgrad , 0, n);
        System.arraycopy(nbYSetToOneInDomLocal, 0, nbYSetToOneInDom , 0, n);
        System.arraycopy(qValLocal, 0, qVal, 0, m);
        System.arraycopy(yLocal, 0, y, 0, m);
        LB = LBLocal;
    }

    private boolean lagrangeMultipliersOptimizationLocalAlterations(final int k, double mu0, double ratio, double UB, double LB, double[] subGrad, double[] lagrangeMultipliers, GradientMethod Grad){
        boolean valid = true;
        boolean change = false;


        newtonConstantSubGradient.reset(UB, LB, lagrangeMultipliers, nbYSetToOneInDom);
        boolean boolSubGrad = newtonConstantSubGradient.coefficient(k);

        for(int i=0; i<n;i++){
            if((1-nbYSetToOneInDom[i])!=0){
                if(boolSubGrad){
                    change |= addLambda(i, newtonConstantSubGradient.getMuk()*(1-nbYSetToOneInDom[i]));
                    valid = false;
                }
                else{
                    // such has division by 0 in  Newton.
                    return false;
                }
            }
        }
        return (!valid && change);
    }

    private void computeFGrad(int j){

        for (int i = 0; i < n; i++) {
            fGradJ[i] = computeFJGradIthComponent(j, i);
//            fGradJ[i] = 0.67 * computeFJGradIthComponent(j, i) + 0.33 * subgrad[i];
//            if(fGradJ[i]<-1*ZERO && lagrangeMultipliers[i]==0){
//                fGradJ[i]=0;
//                nbYSetToOneInDom[i] = 1;
//            }
        }

    }
    private double computeFJGradIthComponent(int jIndex, int iIndex){
        if(dX[iIndex].contain(jIndex)){//X[iIndex].contains(concernedValues[jIndex])
            if(qVal[jIndex] < -1*ZERO) {
                nbYSetToOneInDom[iIndex] -=1;
                return subgrad[iIndex] + 1;
            } else {
                nbYSetToOneInDom[iIndex] +=1;
                return subgrad[iIndex] - 1;
            }
        }
        return subgrad[iIndex];
    }
    private void saveState(){
        System.arraycopy(lagrangeMultipliers, 0, lagrangeMultipliersLocal, 0, n);
        System.arraycopy(subgrad, 0, gradLocal, 0, n);
        System.arraycopy(nbYSetToOneInDom, 0, nbYSetToOneInDomLocal, 0, n);
        System.arraycopy(qVal, 0, qValLocal, 0, m);
        System.arraycopy(y, 0, yLocal, 0, m);
        LBLocal = LB;
    }

    public double getRC(int j, int targetValue) {
        if (targetValue==0){
            return -qVal[j];
        } else{
            assert targetValue==1;
            return qVal[j];
        }
    }

    public double getLB() {
        return LB;
    }

    public BipartiteSet getLFValues(int idx) {
        if (idx < Y.length)
            return lfValues[idx];
        else return null;
    }

    public BipartiteSet[] getYInX(){
        return YInX;
    }

    public BipartiteSet[] getYInXIndex(){
        return YInXIndex;
    }

    public BipartiteSet[] getdX(){
        return dX;
    }

    public BipartiteSet[] getdXIndex(){
        return dXIndex;
    }



    protected void printDataStructure(BipartiteSet[] dataStructure, String name){
        System.out.println(name +"[");
        for(int i = 0; i < dataStructure.length; i++){
            System.out.println(dataStructure[i].pretty());
        }
        System.out.println("]");
    }

    protected void printDomainFromIdx(BipartiteSet[] VarDomain, BipartiteSet[] DomainIdx, String name) {
        System.out.println(name +"[");
        for(int i = 0; i < VarDomain.length; i++) {
            if(DomainIdx[i].last!=-1){
                System.out.print("[");
                for(int idx = 0; idx < DomainIdx[i].last; idx++) {
                    System.out.print(VarDomain[i].list[DomainIdx[i].list[idx]]+", ");
                }

                System.out.print(VarDomain[i].list[DomainIdx[i].list[DomainIdx[i].last]]);

                System.out.println( "]");}
            else {
                System.out.println( "[]");
            }
        }
        System.out.println("]");
    }

    protected void printADouble(double[] vector,int number ,String name) {
        System.out.println(name + "");
        for (int i = 0; i < vector.length; i++) {
            System.out.print(vector[i] < -1 * ZERO ? "" : " ");
            System.out.print(String.format(Locale.US, "%.5f", vector[i]) + "");
            if (i % number < (number - 1)) {
                System.out.print(", ");
            } else {
                if (i != vector.length - 1) {
                    System.out.println(", ");
                } else {
                    System.out.println("");
                }
            }
        }
    }

    protected void printAnInt(int[] vector,int number ,String name){
//        System.out.println(name + " = [");
        System.out.print("[");
        System.out.print("[");
        for(int i = 0; i < vector.length; i++) {
            System.out.print(vector[i]);
            if ((i % number < (number - 1)) && (i != vector.length -1)) {
                System.out.print(", ");
            } else {
                if (i != m - 1) {
                    System.out.println("],");
                    System.out.print("[");
                } else {
                    System.out.println("]],");
                }
            }
        }
    }
}
