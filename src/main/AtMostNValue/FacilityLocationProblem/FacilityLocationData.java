package main.AtMostNValue.FacilityLocationProblem;


import java.util.Arrays;
import java.util.Random;

/**
 * Benchmark on Warehouse location
 */
public class FacilityLocationData {

    /**
     * Number of warehouses
     */
    protected int nbW;

    /**
     * Number of stores
     */
    protected int nbS;

    /**
     * Fixed cost
     */
    protected int[] fixedCost;

    /**
     * supplycosts[i][j]: supply cost for supplying client i with warehouse j
     */
    protected int[][] supplycosts;

    /**
     * capa[j]: capacity of warehouse j
     */
    protected int[] capa;

    /**
     * demand[i][j]: demand of store/client i for facility j
     */
    protected int[][] demand;


    /**
     * Max number of facilities
     */
    protected int N;

    protected String name;

    /**
     * Indicate which variant of the problem we are facing
     */
    protected boolean demandConstraint;     //non unit demand for each store
    protected boolean cardinalityConstraint;//constraint on the number of warehouses that can open


    public FacilityLocationData(int nbW, int nbS) {
        this(nbW, nbS, "?no_name?");
    }

    public FacilityLocationData(int nbW, int nbS, String name) {
        this.nbS         = nbS;
        this.nbW         = nbW;
        this.name        = name;
        this.N           = nbW;
        this.supplycosts = new int[nbS][nbW];
        this.capa        = new int[nbW];
        this.demand      = new int[nbS][nbW];
        this.fixedCost   = new int[nbW];
        this.demandConstraint = true;
        this.cardinalityConstraint = true;
        for (int i = 0; i < nbS; i++)
            Arrays.fill(supplycosts[i], 10000);
    }


    //***************************************************//
    //****************** SET ****************************//
    //***************************************************//

    public void setFixedCost(int fixedCost, int j) {
        this.fixedCost[j] = fixedCost;
    }

    public void setSupplycosts(int costs, int i, int j) {
        this.supplycosts[i][j] = costs;
    }

    public void setCapa(int capa, int j) {
        this.capa[j] = capa;
    }

    public void setDemand(int dem, int i) {
        Arrays.fill(this.demand[i],dem);
    }

    public void setDemand(int dem, int i, int j) {
        this.demand[i][j] = dem;
    }

    public void setN(int n) {
        N = n;
    }

    public void setDemandConstraint(boolean demandConstraint) {
        this.demandConstraint = demandConstraint;
    }

    public void setCardinalityConstraint(boolean cardinalityConstraint) {
        this.cardinalityConstraint = cardinalityConstraint;
    }

    //***************************************************//
    //****************** GET ****************************//
    //***************************************************//

    /**
     * @param j: facility/warehouse
     * @return the capacity (maximum demand) of facility j
     */
    public int getCapa(int j) {
        return capa[j];
    }

    /**
     * @param j: facility/warehouse
     * @return the cost for opening facility j
     */
    public final int getFixedCost(int j) {
        return fixedCost[j];
    }

    /**
     * @param i: client/store
     * @param j: facility/warehouse
     * @return the cost for supplying client i from facility j
     */
    public final int getSupplyCost(int i, int j) {
        return supplycosts[i][j];
    }

    /**
     * @param i: client
     * @return the demand of client i
     */
    public int getDemand(int i) {
        return demand[i][0]; //we assume that the demand is identical for all facilities
    }

    /**
     * @param i: client
     * @return the demand of client i for facility j (demand now varies from one facility to another)
     */
    public int getDemand(int i, int j) {
        return demand[i][j];
    }

    /**
     * @return the maximum number of facilities that can be opened
     */
    public int getMaxNbFacility() {
        return N;
    }

    public String getName() {
        return name;
    }

    public int getNbW() {
        return nbW;
    }

    public int getNbS() {
        return nbS;
    }


    public int[][] getAllSupplyCosts() {
        int[][] copySCosts = new int[supplycosts.length][];
        for (int i = 0; i < copySCosts.length; i++) {
            copySCosts[i] = new int[supplycosts[i].length];
            System.arraycopy(supplycosts[i],0, copySCosts[i], 0, copySCosts[i].length);
        }
        return copySCosts;
    }

    public int[] getAllFixedCosts() {
        int[] copyFcost = new int[fixedCost.length];
        System.arraycopy(fixedCost,0, copyFcost, 0, copyFcost.length);
        return copyFcost;
    }

    public int[][] getAllDemand() {
        int[][] copyDemand = new int[demand.length][];
        for (int i = 0; i < copyDemand.length; i++) {
            copyDemand[i] = new int[demand[i].length];
            System.arraycopy(demand[i],0, copyDemand[i], 0, copyDemand[i].length);
        }
        return copyDemand;
    }

    //***************************************************//
    //****************** SERVICES ***********************//
    //***************************************************//

    public int getSumDemand() {
        int[] maxd = new int[nbS];
        for (int i = 0; i < nbS; i++) {
            maxd[i] = max(demand[i]);
        }
        return sum(maxd);
    }


    public int getSumCapa() {
        return sum(capa);
    }

    /**
     * @return an upper current_bound of the cost
     */
    public int getUb() {
        int sum = sum(fixedCost);
        for (int i = 0; i < nbS; i++) {
            sum += max(supplycosts[i]);
        }
        return sum;
    }

    public int getMaxCostStore(int store) {
        return max(supplycosts[store]);
    }

    public void fillMissingDemandByMax() {
        int maxCapa = max(capa);
        for (int i = 0; i < nbS; i++) {
            for (int j = 0; j < nbW; j++) {
                if (demand[i][j] == 0)
                    demand[i][j] = maxCapa+1;
            }
        }
    }

    /**
     * @param j
     * @return the maximum number of clients that can be connected to facility j
     */
    public int getMaxNumberOfClientForFacility(int j) {
        int[] demForJ = new int[nbS];
        for (int i = 0; i < nbW; i++) {
            demForJ[i] = demand[i][j];
        }
        Arrays.sort(demForJ);
        int nbMax = 0;
        int sum = 0;
        for (int i = 0; i < nbS && (sum < capa[j]); i++) {
            sum += demForJ[i];
            if (sum < capa[j]) nbMax++;
        }
        return nbMax;
    }

    public boolean isObjectiveConnection() {
        for (int i = 0; i < nbW; i++) {
            if (fixedCost[i] != 0) return false;
        }
        return true;
    }

    public boolean isObjectiveFixed() {
        for (int i = 0; i < nbW; i++) {
            for (int j = 0; j < nbS; j++) {
                if (supplycosts[i][j] != 0) return false;
            }
        }
        return true;
    }
    //***************************************************//
    //****************** TOOLS **************************//
    //***************************************************//

    protected static int sum(int[] tab) {
        int sum = 0;
        for (int i = 0; i < tab.length; i++) {
            sum += tab[i];
        }
        return sum;
    }

    protected static int max(int[] tab) {
        int max = tab[0];
        for (int j = 1; j < tab.length; j++) {
            if (tab[j] > max) {
                max = tab[j];
            }
        }
        return max;
    }

    public static int[] getColumn(final int[][] array, final int column) {
        int[] col = new int[array.length];
        for (int i = 0; i < col.length; i++) {
            col[i] = array[i][column];
        }
        return col;
    }


    @Override
    public String toString() {
        return "FL[" + name + ", " + nbW + ", " + nbS + "]";
    }


    //***************************************************//
    //****************** Generation *********************//
    //***************************************************//

    /**
     * Random generation of small PMedian problem
     * @param seed
     */
    public void dlfGene(int seed, double holes) {
        Random rand = new Random(seed);
        //int connectionCostMax = 10;
        int fixedCostVar      = rand.nextInt(1000);
        for (int j = 0; j < nbW; j++) {  //no capacity
            setCapa(nbS, j);
            setFixedCost(fixedCostVar+rand.nextInt(300),j);
        }
        for (int i = 0; i < nbS; i++) {  //unit demand
            for (int j = 0; j < nbW; j++) {
                boolean valueInDomain = (rand.nextDouble() < holes);
                if (valueInDomain) {
                    setDemand(1,i,j);
                    setSupplycosts(0, i, j); //rand.nextInt(connectionCostMax)
                } else {
                    setDemand(nbS+1,i,j);
                }
                setSupplycosts(0, i, j);
            }
        }
        this.N = nbS/4;
        cardinalityConstraint = true;
        demandConstraint = false;
    }

    /**
     * Random generation of small PMedian problem
     * @param seed
     */
    public void capaPmedianGene(int seed) {
        Random rand = new Random(seed);
        int totaldem = 0;
        int[] demands = new int[nbS];
        for (int i = 0; i < nbS; i++) {  //unit demand
            int dem = rand.nextInt(10)+1;
            demands[i] = dem;
            totaldem += dem;
        }
        for (int j = 0; j < nbW; j++) {  //no capacity
            setCapa(totaldem/N + (5)*totaldem/nbS, j);
            setFixedCost(0,j);
        }
        for (int i = 0; i < nbS; i++) {  //unit demand
            for (int j = 0; j < nbW; j++) {
                boolean valueInDomain = (rand.nextDouble() < 0.2);
                if (valueInDomain) {
                    setDemand(demands[i],i,j);
                    setSupplycosts(rand.nextInt(6)+1, i, j);
                } else {
                    setDemand(totaldem,i,j);
                }
            }
        }

        this.N = 5;
        cardinalityConstraint = true;
        demandConstraint = false;
    }


    /**
     * Random generation of small PMedian problem
     * @param seed
     */
    public void capaDFLGene(int seed) {
        Random rand = new Random(seed);
        int demMoy = 10;
        for (int j = 0; j < nbW; j++) {  //no capacity
            setCapa((demMoy*nbS)/N + (5)*(demMoy*nbS)/nbS, j);
            setFixedCost(100,j);
        }
        for (int i = 0; i < nbS; i++) {  //unit demand
            for (int j = 0; j < nbW; j++) {
                boolean valueInDomain = (rand.nextDouble() < 0.2);
                if (valueInDomain) {
                    setDemand(rand.nextInt(2*demMoy-1)+1,i,j);
                    setSupplycosts(rand.nextInt(100)+1, i, j);
                } else {
                    setDemand(2*demMoy*nbS,i,j);
                }
            }
        }

        this.N = nbW;
        cardinalityConstraint = false;
        demandConstraint = true;
    }

}
