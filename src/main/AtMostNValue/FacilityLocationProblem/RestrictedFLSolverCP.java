package main.AtMostNValue.FacilityLocationProblem;

import main.AtMostNValue.Propagators.PropAtMostNValue;
import main.AtMostNValue.Propagators.PropDomination;
import main.AtMostNValue.Propagators.PropUseValues;
import main.AtMostNValue.SubGradient.GradientMethod;
import main.AtMostNValue.SubGradient.GradientObject;
import main.AtMostNValue.heuristics.CPFeasAndCheap;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.binary.element.PropElement;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.impl.FixedIntVarImpl;
import org.chocosolver.util.ESat;

/**
 * A more restricted facility CP location solver EXCLUDING:
 * - capacity constraints
 * - cardinality constraints
 */
public class RestrictedFLSolverCP extends TestSolver {

    protected boolean trace = false;

    public boolean applyDomination = true;

    protected CPModels model_mode = CPModels.STAND;
    //Solving parameters
    protected RelaxMode relax_mode;
    protected Heuristiques heur_mode;

    //Solver representation
    protected Model model;
    protected Solver solver;
    protected IntVar[] X;
    protected BoolVar[] Yj;
    protected IntVar N;
    protected IntVar Z;
    protected IntVar[] Wvj2;
    protected int[] concernedValues;
    protected GradientObject gradientObject;
    protected int numberOfStep;
    protected double threshold;


    public RestrictedFLSolverCP(FacilityLocationData data, RelaxMode mode, CPModels mmode, Heuristiques hmode, int timelimit, boolean domination, GradientObject gradientObject, int numberOfStep, double threshold) {
        super("CP" + ((mmode == CPModels.STAND) ? "" : "+" + mmode), data, timelimit);
        this.relax_mode = mode;
        this.model_mode = mmode;
        this.heur_mode = hmode;
        this.feasible = true;
        this.applyDomination = domination;
        this.gradientObject = gradientObject;
        this.numberOfStep = numberOfStep;
        this.threshold = threshold;
        if (mmode != CPModels.STAND) {useLR = true;}
        stateModel();
    }

    @Override
    public void showSolution() {
        this.solution.showSolution();
    }

    //***************************************************//
    //******************* State model *******************//
    //***************************************************//

    /**
     * State the CP Model
     */
    public void stateModel() {
        if (log)
            System.out.println("WarehouseLocation" + data + " : " + relax_mode + " - " + model_mode);
        this.model = new Model();
        this.solver = model.getSolver();
        createVariables();
        try {
            stateConstraints();
        } catch (ContradictionException e) {
            feasible = false;
            optCost = -1;
        }
    }

    protected void createVariables() {
        this.concernedValues = new int[data.getNbW()];
        for (int k = 0; k < data.getNbW(); k++) {
            concernedValues[k] = k;
        }
        X = new IntVar[data.getNbS()];
        for (int k = 0; k < data.getNbS(); k++) {
            X[k] = model.intVar("X[" + k + "]", concernedValues);
        }
        Yj = model.boolVarArray("Yj", concernedValues.length);
        N =  model.intVar("N", 0, data.getMaxNbFacility(), true);
        Z = model.intVar("Z", 0, data.getUb());

    }

    protected void XAdjustment() throws ContradictionException {
        for (int j = 0; j < data.getNbW(); j++) {
            for (int i = 0; i < data.getNbS(); i++) {
                if (data.getDemand(i, j) > data.getCapa(j)) {
                    X[i].removeValue(j, Cause.Null);
                }
            }
        }


    }

    protected void PostSomeConstraint(){
        for(int j=0;j<data.getNbW();j++){
            model.post(new Constraint("", new PropUseValues(X, Yj[j], j)));
        }
    }

    protected void stateConstraints() throws ContradictionException {
//        PropNValueGlobal.subproblem = PropNValueGlobal.SUBPROBLEMS.NVALUE_DEDICATED;//System.out.println("(RestrictedFLSolverCP 120) PropNValueGlobal.subproblem = " + PropNValueGlobal.subproblem);

        // 1) Take into account initial holes
        XAdjustment();//System.out.println("(RestrictedFLSolverCP 123) Ajustement des X"); // Remettre le bout de code au lieu de ça
//        System.out.println("net.add_nodes(");
//        System.out.print("[");
//        for(int i = 0; i< 201; i++) {
//            System.out.print(i+",");
//        }
//        System.out.println("],");
//        System.out.print("label = [");
//        for(int i = 0; i< 100; i++) {
//            System.out.print("\"C"+i+"\",");
//        }
//        for(int i = 0; i< 100; i++) {
//            System.out.print("\"S"+i+"\",");
//        }
//        System.out.println("],");
//        System.out.print("color = [");
        //"#eb4034" red Store i
        //"#3155a8" blue Client i
//        for(int i = 0; i< 100; i++) {
//            System.out.print("\"#3155a8\",");
//        }
//        for(int i = 0; i< 100; i++) {
//            System.out.print("\"#eb4034\",");
//        }
//        System.out.println("],\n)");



        // 2) Link Y and X:         X_i = j => Y_j = 1
        //    redondant constraint: Y_j = 1 => there exists an i s.t X_i = j
        PostSomeConstraint();
        //valid constraint for facility location benchmark
        model.sum(Yj, "=", N).post();
        // 3) State objective
        stateObjective();

        // 4) State the NValue constraint
        stateNValues();

        // 5) Add contraint on dominance
        if (applyDomination) {
            model.post(new Constraint("facilitylocation/domination", new PropDomination(Yj, X, data)));
        }
    }

    //***************************************************//
    //******************* Objective *********************//
    //***************************************************//

    public void stateObjective() {
        int max = Integer.MAX_VALUE / 10;
        //Assignment cost
        IntVar Zass = model.intVar("Zass", -max, max, true);
        if (data.isObjectiveConnection()) {//Assignment cost
            IntVar[] CostOfStore = new IntVar[data.getNbS()];
            int[][] supplycosts = data.getAllSupplyCosts();
            for (int i = 0; i < data.getNbS(); i++) {
                CostOfStore[i] = model.intVar("CostOfClient " + i, 0, data.getMaxCostStore(i));
                model.post(new Constraint("Element" + i, new PropElement(CostOfStore[i], supplycosts[i], X[i], 0)));
            }

            model.sum(CostOfStore, "=", Zass).post();
        } else {
            model.arithm(Zass,"=",0).post();
        }

        IntVar Zval = model.intVar("Zval", -max, max, true);
        if (data.isObjectiveFixed()) {//Opening cost
            model.scalar(Yj, data.getAllFixedCosts(),"=", Zval).post();
        } else {
            model.arithm(Zval,"=",0).post();
        }

        model.sum(new IntVar[]{Zass, Zval}, "=", Z).post();

    }


    //***************************************************//
    //*********** Redundant NValues *********************//
    //***************************************************//


    private void stateNValues() {
        if (model_mode == CPModels.STAND) { //Option 1 (standart nvalue)
            model.nValues(X,N);
        }
        if (model_mode == CPModels.LNVAL || model_mode == CPModels.LNWVAL) { //Option 2 (lagrangian nvalue)
            IntVar[] Wvj = new IntVar[Yj.length];
            for (int j = 0; j < Wvj.length; j++) {
//                Wvj[j] = VariableFactory.fixed(1, N.getSolver());
//                Wvj[j] = FixedIntVarImpl("Wj", 1, model);
                Wvj[j] = new FixedIntVarImpl("Wv1["+j+"]", 1, model);
            }
            model.post(new Constraint("Nvalue Global",new PropAtMostNValue(X, Yj, N, N, concernedValues, gradientObject, numberOfStep,  threshold)));


        }

        //Option 3 (lagrangian weighted nvalue): variable costs to derive a lower current_bound
        if (model_mode == CPModels.LWVAL || model_mode == CPModels.LNWVAL) {
            if (model_mode == CPModels.LWVAL)
                model.nValues(X,N);
            addLowerBoundWeightedNValue();
        }

        //Option 4 (lagrangian nvalue with costs): variable costs model with limit on N
        if (model_mode == CPModels.LCVAL) {
            addLowerBoundCostNValue();
        }
    }

    private void buildVariableCosts() {
        Wvj2 = new IntVar[Yj.length];
        for (int j = 0; j < Wvj2.length; j++) {
            Wvj2[j] = new FixedIntVarImpl("Wv["+j+"]", data.getFixedCost(j), model);

        }
    }

    /**
     * Redundant model
     * - For each value j:
     * w_j + sum_i c_{ij}x_{ij} = Wv2_j
     * - WeightedNValue(X,Wv2) <= Z
     */
    private void addLowerBoundWeightedNValue() {
//        IntVar Z2 = VF.bounded("Z2", 0, data.getUb(), solver);
        IntVar Z2 = model.intVar("Z2", 0, data.getUb(), true);
        if (Wvj2 == null) buildVariableCosts();
//        solver.post(new Constraint("Nvalue Global", new PropNValueGlobal(X, Yj, Wvj2, N, Z2, true, relax_mode, GlobalConstraintFactory.warmStartLR)));
        model.post(new Constraint("Nvalue Global",new PropAtMostNValue(X, Yj, N, N, concernedValues, gradientObject, numberOfStep, threshold)));
//        solver.post(ICF.arithm(Z2, "<=", Z));
        model.arithm(Z2, "<=", Z);
    }


    /**
     * Redundant model
     * - For each value j:
     * w_j + sum_i c_{ij}x_{ij} = Wv2_j
     * - WeightedNValue(X,Wv2) <= Z AND NValue(X) <= N
     */
    private void addLowerBoundCostNValue() {
//        IntVar Z3 = VF.bounded("Z3", 0, data.getUb(), solver);
        IntVar Z3 = model.intVar("Z3", 0, data.getUb(), true);
        if (Wvj2 == null) buildVariableCosts();
//        solver.post(new Constraint("Nvalue Global", new PropNValueGlobal(X, Yj, Wvj2, N, Z3, false, relax_mode, GlobalConstraintFactory.warmStartLR)));
        model.post(new Constraint("Nvalue Global",new PropAtMostNValue(X, Yj, N, N, concernedValues, gradientObject, numberOfStep, threshold)));
//        solver.post(ICF.arithm(Z3, "<=", Z));
        model.arithm(Z3, "<=", Z);
    }

    //***************************************************//
    //***************** solveRelaxation *****************************//
    //***************************************************//

    public void run() {//System.out.println("(RestrictedFLSolverCP 267) On est dans le constructeur de le \"run\" @ "+X[0].getSolver().getMeasures().getTimeCount());
//        System.out.println("Here");
        if (feasible) {//System.out.println("(RestrictedFLSolverCP 269) On est \"feasible\"");
            try {//System.out.println("(RestrictedFLSolverCP 270) try{");
                solver.propagate();
                this.rootNodeLb = Z.getLB();

                TimeCounter counter = new TimeCounter(solver, (timelimitInS * 1000) * 1000000L);
                solver.addStopCriterion(counter);
                solver.plugMonitor(new IMonitorSolution() {
                    @Override
                    public void onSolution() {
                        if (trace)
                            System.out.println("solution found : " + Z + " " + N + " Fail: " + solver.getMeasures().getFailCount() + " Time: " + solver.getMeasures().getTimeCount());
                    }
                });
                setHeuristic();
//                solver.findOptimalSolution(Z, false);

                model.setObjective(Model.MINIMIZE, Z);
                Solution solution2 = new Solution(model);
                while (model.getSolver().solve()) {
                    solution2.record();
                }
                solution2.restore();


                if (solver.isFeasible() == ESat.TRUE) {//System.out.println("Feli was here @ RestrictedFLSolverCP 305");
                    storeSolution();//System.out.println("Feli was here @ RestrictedFLSolverCP 307 @ time = "+solver.getMeasures().getTimeCount());
                } else {
                    optCost = -1;
                    feasible = false;
                }
                IMeasures m = solver.getMeasures();
                if (log) System.out.println("Back  : " + m.getFailCount());
                this.searchSpace = m.getFailCount();
//                System.out.println(solver.getMeasures().getBestSolutionValue());


            } catch (ContradictionException e) {
                this.feasible = false;
                optCost = -1;
                //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        IMeasures m = solver.getMeasures();
        this.time = m.getTimeCount();
        this.searchSpace = m.getFailCount();
        this.optCost = m.getBestSolutionValue().intValue();
//        System.out.println(optCost);
    }


    public void storeSolution() {
        this.feasible = false;
        if (solver.isFeasible() != ESat.UNDEFINED) {
            if (solver.isFeasible() == ESat.TRUE) {//System.out.println("Feli was here @ RestrictedFLSolverCP 331");
                optCost = solver.getObjectiveManager().getBestSolutionValue().intValue();
                this.feasible = true;
                for (int j = 0; j < data.getNbW(); j++) {
                    if (Yj[j].isInstantiatedTo(1) && !solution.containsY(j+1)) {//!solution.y_sol.contains(j + 1)) {
//                        System.out.println("Feli was here @ RestrictedFLSolverCP 335 for j = "+ j);
                        solution.addY(j+1);//y_sol.add(j + 1);
                        for (int i = 0; i < data.getNbS(); i++) {
                            if (X[i].isInstantiatedTo(j)) {
                                solution.addXij(j, i+1);
                                //solution.x_sol[j].add(i + 1);
                            }
                        }
                    }
                }
                solution.setTotalCost(optCost);//solution.total_cost = optCost;
                if (log) {
                    showSolution();
                }
            } else {
                if (log) System.out.println("No Solution");
            }
        }
        if (log) {
            System.out.println(solver.getMeasures().toOneLineString());
        }
    }

    //***************************************************//
    //***************** search **************************//
    //***************************************************//

    public void setHeuristic() {
        long seed = 0;
//		IntValueSelector valueSelector = ISF.random_value_selector(seed);
//solver.set(ISF.domOverWDeg(X, seed, valueSelector));


        if (data.isObjectiveConnection()) { //System.out.println("Feli was here @ RestrictedFLSolverCP 369"); // 1) heuristique basique quand on veut ouvrir le plus de valeur possible
            //solver.post(ICF.arithm(N,"=",N.getUB()));//not needed
            if (heur_mode == Heuristiques.STAND) {//System.out.println("Feli was here @ RestrictedFLSolverCP 371");
                //solver.set(ISF.lexico_UB(N),
//                solver.set(ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(), Yj),
//                        ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(), X));
                solver.setSearch(Search.minDomLBSearch(Yj), Search.minDomLBSearch(X));
            } else {
//                System.out.println("Ici");
                //solver.set(ISF.lexico_UB(N),
                solver.setSearch(
                        Search.intVarSearch(new CPFeasAndCheap(data, Yj, X), new IntDomainMax(), Yj),
                        Search.intVarSearch(new FirstFail(model), new IntDomainMin(), X));
            }
        } else if (data.isObjectiveFixed()) { // 2) Heuristique basique quand on veut ouvrir le moins de valeur possible
            if (heur_mode == Heuristiques.STAND) {
//                solver.set(ISF.lexico_LB(N),
//                        ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(), Yj),
//                        ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(), X));
                solver.setSearch(Search.minDomLBSearch(N),Search.minDomLBSearch(Yj), Search.minDomLBSearch(X));
            } else {
                solver.setSearch(Search.minDomLBSearch(N),
                        Search.intVarSearch(new CPFeasAndCheap(data, Yj, X), new IntDomainMax(), Yj),
                        Search.intVarSearch(new FirstFail(model), new IntDomainMin(), X));
//                solver.set(ISF.lexico_LB(N),
//                        ISF.custom(new CPFeasAndCheap(data, Yj, X), ISF.max_value_selector(), Yj),
//                        ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(), X));
            }
        } else { // 3) TODO
            throw new Error("no mixed cost facilit pb yet");
        }
    }

    //***************************************************//
    //***************** Main ****************************//
    //***************************************************//


    //***************************************************//
    //***************** Main ****************************//
    //***************************************************//
    //-agentlib:hprof=cpu=samples,depth=25
    public static void main(String[] args) {
        minitestPMEDIAN();
//        minitestDFL();
    }

    public static void minitestPMEDIAN() {
        // NEWTON //
//        GlobalConstraintFactory.setNewtonGradient(5, 10, 1000, 1000);
        GradientObject gradientObject1 = new GradientObject(GradientMethod.Newton, 1.0, 5);

        /*FacilityLocationData wld = new FacilityLocationData(80, 80, "randPMED_" + 0);
        wld.dlfGene(0, 0.1);
        FLSolverCP solver = new FLSolverCP(wld, RelaxMode.LAG, CPModels.LCVAL, 300);
        solver.solveRelaxation();
        solver.showSolution();
        System.out.println("RLB: " + solver.getRootNodeLb() + " T: " + solver.getTime() + " F: " + solver.getSearchSpace());
        */
        String fileName             = "data/pmed_dfl/732PM_GapA.txt"; //1731
        FacilityLocationData wld    = FacilityParseur.loadPMedianLib(fileName);
        RestrictedFLSolverCP solver = new RestrictedFLSolverCP(wld, RelaxMode.LAG, CPModels.LNVAL, Heuristiques.COST, 30, true, gradientObject1, 80, 0.45);
        solver.trace = true;
        solver.run();
        if (solver.isFeasible()) {
            System.out.println("check");
            solver.getSolution().check();
        }
        System.out.println("RLB: " + solver.getRootNodeLb() + " T: " + solver.getTime() + " F: " + solver.getSearchSpace() + " OPT: " + solver.getOptCost());

        //solution found : Z = 145 N = 15
        //solution found : Z = 140 N = 15
        //FL[331PM_GapB, 100, 100][SEARCH:143408][TIME:1500.01][FEAS:1][R:0][OPT:140]
        //MIP:FL[331PM_GapB, 100, 100][SEARCH:5030][TIME:30.022][FEAS:1][R:0][OPT:135]
    }

    //1031PM_GapB.txt
    //solution found : Z = 165 N = 14 Fail: 5735 Time: 20.470253
    //RLB: 15 T: 56.10141 F: 16634 OPT: 165
    //1131PM_GapB
    //solution found : Z = 162 N = 14 Fail: 9263 Time: 29.271255
    //RLB: 16 T: 63.20767 F: 20292 OPT: 162
    //732PM_GapB
    //solution found : Z = 157 N = 11 Fail: 782 Time: 2.95661
    //RLB: 14 T: 19.743969 F: 5878 OPT: 157
    //1833PM_GapC
    //solution found : Z = 175 N = 14 Fail: 67131 Time: 233.69255
    //RLB: 12 T: 300.00302 F: 85618 OPT: 175

    public static void minitestDFL() {
//        GlobalConstraintFactory.setNewtonGradient(5, 10, 1000, 1000);
        GradientObject gradientObject1 = new GradientObject(GradientMethod.Newton, 1.0, 5);
        FacilityLocationData wld = new FacilityLocationData(50, 50, "randDFL_" + 0);
        wld.dlfGene(0, 0.1);
        RestrictedFLSolverCP solver = new RestrictedFLSolverCP(wld, RelaxMode.LAG, CPModels.STAND, Heuristiques.STAND, 300, false, gradientObject1, 80, 0.45);
        solver.trace = true;
        solver.run();
        if (solver.isFeasible()) {
            System.out.println("check");
            solver.getSolution().check();
        }
        System.out.println("RLB: " + solver.getRootNodeLb() + " T: " + solver.getTime() + " F: " + solver.getSearchSpace() + " OPT: " + solver.getOptCost());
    }
}
