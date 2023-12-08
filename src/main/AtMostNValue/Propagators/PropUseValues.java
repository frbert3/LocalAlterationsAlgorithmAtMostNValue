package main.AtMostNValue.Propagators;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

public class PropUseValues extends Propagator<IntVar> {

    protected IntVar[] X;
    protected BoolVar Y;
    protected int n, value;
    protected IStateInt[] watch;

    public PropUseValues(IntVar[] X, BoolVar Y, int value) {
        super(ArrayUtils.append(X, new IntVar[]{Y}), PropagatorPriority.TERNARY, true);
        this.X = X;
        this.Y = Y;
        this.value = value;
        this.n = X.length;
        this.watch = new IStateInt[2];
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {

        for(int i = 0; i < n; i++) {
            if(X[i].isInstantiatedTo(value))  {
                Y.setToTrue(this);
//                Y.instantiateTo(1, this);
                setPassive();
                return;
            }
        }

        watch[0] = model.getSolver().getEnvironment().makeInt(-1);
        watch[1] = model.getSolver().getEnvironment().makeInt(-1);
        int cpt = 0;
        for (int i = 0; i < n && cpt < 2; i++) {
            if (X[i].contains(value)) {
                watch[cpt++].set(i);
                if(X[i].isInstantiated()){
                    Y.setToTrue(this);
                    setPassive();
                    return;
                }
            }
        }
        if (watch[0].get() == -1){
            Y.setToFalse(this);
            setPassive();
        }
        else if (Y.isInstantiated()){
            Yfixed();
        }

    }

    @Override
    public void propagate(int vIdx, int evtmask) throws ContradictionException {
        if(vIdx == n){
            Yfixed();
        }else if(X[vIdx].isInstantiatedTo(value)){
            Y.setToTrue(this);
            setPassive();
        }else if(vIdx == watch[0].get() || vIdx == watch[1].get()){
            watchHasChange(vIdx);
        }
    }

    @Override
    public ESat isEntailed() {
        boolean may = false;
        boolean must = false;
        for(int i=0;i<n;i++){
            if(X[i].contains(value)){
                may = true;
                if(X[i].isInstantiated()){
                    must = true;
                    break;
                }
            }
        }
        if((Y.isInstantiatedTo(1) && !may) || (Y.isInstantiatedTo(0) && must)){
            return ESat.FALSE;
        }
        if((Y.isInstantiatedTo(1) && must) || (Y.isInstantiatedTo(0) && !may)){
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }


    public void Yfixed() throws ContradictionException {
        if (Y.isInstantiatedTo(0)){
            for(IntVar x:X){
                x.removeValue(value, this);
            }
            setPassive();
        }else if (Y.isInstantiatedTo(1)){
            if(watch[0].get() == -1){
//                contradiction(Y,"");
                ContradictionException contradiction = new ContradictionException();
                throw  contradiction.set(this, Y, "");
            }else if(watch[1].get() == -1){
                X[watch[0].get()].instantiateTo(value, this);
                setPassive();
            }
        }
    }

    public void watchHasChange(int w) throws ContradictionException {
        if(X[w].contains(value)) {
            if (X[w].isInstantiated()) {
                Y.instantiateTo(1, this);
                setPassive();
            }
        }else{
            if (w == watch[0].get()) {
                watch[0].set(watch[1].get());
            }
            watch[1].set(-1);
            if (watch[0].get() == -1) {
                Y.instantiateTo(0, this);
                setPassive();
            } else {
                for (int i=0; i<n; i++){
                    if(i != watch[0].get() && X[i].contains(value)){
                        watch[1].set(i);
                        if (X[i].isInstantiated()) {
                            Y.instantiateTo(1, this);
                            setPassive();
                        }
                        break;
                    }
                }
                if (watch[1].get() == -1 && Y.isInstantiatedTo(1)){
                    X[watch[0].get()].instantiateTo(value, this);
                    setPassive();
                }
            }
        }
    }
}