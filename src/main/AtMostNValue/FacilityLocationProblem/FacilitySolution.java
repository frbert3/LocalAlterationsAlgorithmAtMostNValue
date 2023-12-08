package main.AtMostNValue.FacilityLocationProblem;


import java.util.LinkedList;
import java.util.List;

/**
 * Representation of a solution to facility location
 */
public class FacilitySolution {

    protected FacilityLocationData data;

    protected int n; //number of stores
    protected int m; //number of warehouses
    protected List<Integer> y_sol;
    protected List<Integer>[] x_sol;

    protected double total_cost;

    public FacilitySolution(FacilityLocationData data) {
        this.n = data.nbS;
        this.m = data.nbW;
        this.data = data;
        this.y_sol = new LinkedList<Integer>();
        this.x_sol = new LinkedList[m];
        for (int i = 0; i < m; i++) {
            x_sol[i] = new LinkedList<Integer>();
        }
    }

    public void clearY() {
        y_sol.clear();
    }

    public void addY(int y) {
        assert(!y_sol.contains(y));
        y_sol.add(y);
    }

    public void addXij(int j, int i) {
        assert(!x_sol[j].contains(i));
        x_sol[j].add(i);
    }

    public void setTotalCost(double tcost) {
        this.total_cost = tcost;
    }

    public boolean containsY(int y) {
        return y_sol.contains(y);
    }

    public void showSolution() {
        int fixedcost = 0;
        for (int j : y_sol) {
            fixedcost += data.getFixedCost(j-1);
        }
        System.out.println("Facilities: " + y_sol + "[fc:" +fixedcost+"]");
        for (int j = 0; j < n; j++) {
            int connectionCost = 0;
            int connectedDemand= 0;
            for (int i : x_sol[j]) {
                connectionCost  += data.getSupplyCost(i-1,j);
                connectedDemand += data.getDemand(i-1,j);
            }
            if (!x_sol[j].isEmpty())
                System.out.println("F_" + (j+1) + ":" + x_sol[j] + "[cc:" + connectionCost + "][load:" + connectedDemand +"/" + data.getCapa(j) + "]");
        }
    }

    /**
     * Simple checker
     * @return
     */
    public boolean check() {
        if (data.demandConstraint) throw new Error("Checker does not handle demand constraints");

        //1) check nvalue
        if (y_sol.size() > data.getMaxNbFacility()) {
            System.out.println("too many facilities opened " + y_sol.size() + " > " + data.getMaxNbFacility());
            return false;
        }

        //2) check connection validity
        for (int i = 1; i <= data.getNbS(); i++) {
            int warehouseconnected = -1;
            for (int j : y_sol) {
                for (int clienti : x_sol[j-1]) {
                    if (clienti == i) {
                        if (warehouseconnected != -1)
                            throw new Error("one client/store is connected to more than 1 facility");
                        warehouseconnected = j;
                    }
                }
            }

            if (warehouseconnected == -1)
                throw new Error("client/store" + i + " is un-connected");
        }

        //3) recompute cost
        double recomputed_cost = 0;
        for (int j : y_sol) {
            recomputed_cost += data.getFixedCost(j-1);
        }
        for (int j = 0; j < data.getNbW(); j++) {
            for (int i : x_sol[j]) {
                recomputed_cost  += data.getSupplyCost(i-1,j);
            }
        }
        if (Math.abs(total_cost - recomputed_cost) > 1e-5) {
            throw new Error("cost do not match " + total_cost + " versus " + recomputed_cost);
        }
        return true;

    }

}