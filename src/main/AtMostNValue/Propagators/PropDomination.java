package main.AtMostNValue.Propagators;

import main.AtMostNValue.FacilityLocationProblem.FacilityLocationData;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

public class PropDomination extends Propagator<BoolVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    FacilityLocationData data;
    BoolVar[] Y;
    IntVar[] X;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropDomination(BoolVar[] yj, IntVar[] x, FacilityLocationData data) {
        super(yj, PropagatorPriority.LINEAR, true);
        Y = yj;
        X = x;
        this.data = data;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int j = 0; j < Y.length; j++) {
            if (Y[j].getLB() == 1) {
                propagate(j, 0);
            }
        }
    }

    @Override
    public void propagate(int idV, int evtmask) throws ContradictionException {
        if (Y[idV].getValue() == 1) { //idV est la valeur nouvellement instanciée.
            for (int j = 0; j < Y.length; j++) {
                if (j != idV && Y[j].getUB() == 1) { // j, une valeur non fermée
                    for (int i = 0; i < X.length; i++) {
                        if (X[i].contains(j) && X[i].contains(idV)) {
                            if (data.getSupplyCost(i, j) >= data.getSupplyCost(i, idV)) {
                                X[i].removeValue(j, this);
                            }// else if ((data.getSupplyCost(i, j) == data.getSupplyCost(i, idV))) {
                            //   X[i].removeValue(j, aCause);
                            //}
                        }
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant filtering
    }
}
