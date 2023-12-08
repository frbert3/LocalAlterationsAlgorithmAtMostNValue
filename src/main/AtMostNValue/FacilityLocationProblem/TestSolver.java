package main.AtMostNValue.FacilityLocationProblem;


/**
 * Generic test solver to store statistics of all tests
 */
public abstract class TestSolver {

    public enum CPModels {
        STAND,      // Decomposition + NValue
        LNVAL,         // Decomposition + Lagrangian propagator for NValue
        LWVAL,   // Decomposition + Lagrangian propagator for Weighted NValue
        LNWVAL,  // Decomposition + Lagrangian propagator for Weighted NValue
        LCVAL;  // Decomposition + Lagrangian propagator for Weighted NValue
    }

    public enum Heuristiques {
        STAND, //STANDARD
        COST   //COST ORIENTED
    }

    protected boolean log = false;

    /**
     * Timelimit in seconds
     */
    protected int timelimitInS = -1;

    /**
     * Encoding of the data
     */
    protected FacilityLocationData data;

    /**
     * Solution to the problem
     */
    protected FacilitySolution solution;

    /**
     * number of fails (for choco), number of nodes (for cplex)
     **/
    protected long searchSpace;

    /**
     * Iteration of LR / Iteration of cplex
     */
    protected int numberOtIterations;

    /**
     * Number of calls to the dual solver
     */
    protected int numberOfCallDualSolver;

    /**
     * time in seconds
     */
    protected float time;
    /**
     * true if a feasible y_sol has been found
     */
    protected boolean feasible;

    /**
     * Store optimal solution
     */
    protected int optCost;

    /**
     * Stored lower current_bound at root node
     */
    protected int rootNodeLb;

    /**
     * Name of the algorithm
     */
    protected String name;

    /**
     * true if the algorithm is using LR
     */
    protected boolean useLR = false;

    public TestSolver(int timelimitInS) {
        this(null,timelimitInS);
    }

    public TestSolver(String name, FacilityLocationData data, int timelimitInS) {
        this.name = name;
        this.data         = data;
        this.searchSpace  = 0;
        this.time         = 0;
        this.rootNodeLb   = 0;
        this.feasible     = false;
        this.useLR        = false;
        this.timelimitInS = timelimitInS;
        if (data != null)
            this.solution = new FacilitySolution(data);
    }


    public TestSolver(FacilityLocationData data, int timelimitInS) {
        this("NONE",data,timelimitInS);
    }

    public abstract void run();

    public boolean timeLimitReached() {
        return (time >= timelimitInS);
    }

    public boolean isUseLR() {
        return useLR;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public int getTimelimitInS() {
        return timelimitInS;
    }

    public long getSearchSpace() {
        return searchSpace;
    }

    public float getTime() {
        return time;
    }

    public FacilitySolution getSolution() {
        return solution;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public int getOptCost() {
        return optCost;
    }

    public int getRootNodeLb() {
        return rootNodeLb;
    }

    public int getNumberOtIterations() {
        return numberOtIterations;
    }

    public void setNumberOtIterations(int numberOtIterations) {
        this.numberOtIterations = numberOtIterations;
    }

    public String summary() {
        return "" + data + "[SEARCH:" + searchSpace + "][TIME:" + (Math.round(time*1000)/1000d) + "][FEAS:"+ (feasible ? 1: 0)+"][R:"+(timeLimitReached() ? 0 : 1)+"][OPT:"  + optCost + "]";
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TestSolver{" +
                "log=" + log +
                ", timelimitInS=" + timelimitInS +
                ", data=" + data +
                ", solution=" + solution +
                ", searchSpace=" + searchSpace +
                ", numberOtIterations=" + numberOtIterations +
                ", numberOfCallsDualS=" + numberOfCallDualSolver +
                ", time=" + time +
                ", feasible=" + feasible +
                ", optCost=" + optCost +
                ", rootNodeLb=" + rootNodeLb +
                '}';
    }

    public abstract void showSolution();

}
