package main.AtMostNValue.Experiments;

import main.AtMostNValue.DataStructure.BipartiteSet;
import main.AtMostNValue.InstanceGenerator.DominatingQueen;
import main.AtMostNValue.Propagators.PropAtMostNValue;
import main.AtMostNValue.Propagators.PropUseValues;
import main.AtMostNValue.SubGradient.GradientMethod;
import main.AtMostNValue.SubGradient.GradientObject;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.io.FileNotFoundException;

public class DominatingQueensProblem {
    public static void main(String[] args) throws FileNotFoundException {


        System.out.print("n" + ", ");
        System.out.print("m" + ", ");
        System.out.print("v" + ", ");
        System.out.print("Feasable" + ", ");
        System.out.print("grad.method" + ", ");
        System.out.print("grad.ratio" + ", ");
        System.out.print("grad.mu0" + ", ");
        System.out.print("nStep" + ", ");
        System.out.print("threshold"+ ", ");
        System.out.print("NodeCount" + ", ");
//        System.out.print("BackTrackCount "+ ", ");
        System.out.print("FailCount" + ", ");
        System.out.print("iterTot" + ", ");
        System.out.print("TimeCount" + ", ");
        System.out.println("N");


        int[] q = new int[]{6, 7, 8, 8, 9};
        int[] p = new int[]{6, 7, 8, 8, 9};
        int[] v = new int[]{3, 4, 5, 4, 5};
        int offSet = 0;
        boolean viewSolution = true;

        GradientObject[] grads = new GradientObject[]{new GradientObject(GradientMethod.Harmonic,1.0,1.0), new GradientObject(GradientMethod.Geometric,0.95,1000.0), new GradientObject(GradientMethod.Newton,1.0,5.0)};
//        GradientObject[] grads = new GradientObject[]{new GradientObject(GradientMethod.Harmonic,1.0,1.0)};
//        GradientObject[] grads = new GradientObject[]{new GradientObject(GradientMethod.Geometric,0.95,1000.0)};
//        GradientObject[] grads = new GradientObject[]{new GradientObject(GradientMethod.Newton,1.0,5.0)};

        int[] numberOfStep = new int[]{0, 40};
        double[] threshold = new double[]{0.4};
        for(int problem = 0; problem < q.length; problem++){
            int n = q[problem];
            int m = p[problem];
            int[] concernedValues = new int[n * m];
            for (int i = 0; i < n * m; i++) {
                concernedValues[i] = i + offSet;
            }
            BipartiteSet theSolution = new BipartiteSet(n * m);

            /**
             * Création des arrays qui seront les domaines des variables X.
             * **/
            int[][] x = new int[n * m][];
            for (int i = offSet; i < n * m + offSet; i++) {
                x[i-offSet] = DominatingQueen.QDomain(i -(offSet-1), n, m);
            }
            /**
             * Ajustement des domaines pour avoir la bonne valeur minimale.
             * **/
            for (int i = 0; i < n * m; i++) {
                for (int j = 0; j < x[i].length; j++) {
                    x[i][j] += (offSet-1);
                }
            }
            for (GradientObject grad : grads) {
                for (int nStep : numberOfStep) {
                    int e = (nStep > 0) ? threshold.length : 1;
                    for(int s = 0; s < e; s++) {
                        Model model = new Model("Un Modele ");
                        IntVar N = model.intVar("N", 0, v[problem]);
                        IntVar[] X = new IntVar[n * m];
                        /**
                         * Création des variables Xs avec leur domaine.
                         * **/
                        for (int i = offSet; i < n * m+offSet; i++) {
                            int j = i - offSet;
                            X[i - offSet] = model.intVar("Q" + j, x[i - offSet]);
                        }
                        BoolVar[] Y = new BoolVar[n * m];
                        for(int j = 0; j < n * m; j++) {
                            Y[j] = model.boolVar("Y["+j+"]");
                        }

                        PropAtMostNValue propNValue = new PropAtMostNValue(X,  Y, N, N,concernedValues,grad, nStep, threshold[s]);
                        Constraint nValue = new Constraint("AtMostNValue", propNValue);
                        for(int j=0;j<m*m;j++){
                            model.post(new Constraint("", new PropUseValues(X, Y[j], j + offSet)));
                        }
                        nValue.post();
                        /**
                         * Search heuristic.
                         * **/
                        model.getSolver().setSearch(Search.inputOrderLBSearch(X));
                        model.setObjective(Model.MINIMIZE, N);
                        model.getSolver().limitTime("300s");
                        model.getSolver().solve();

                        System.out.print(n + ", ");
                        System.out.print(m + ", ");
                        System.out.print(v[problem] + ", ");
                        System.out.print(N.isInstantiated() + ", ");
                        System.out.print(grad.method + ", ");
                        System.out.print(grad.ratio + ", ");
                        System.out.print(grad.mu0 + ", ");
                        System.out.print(nStep + ", ");
                        System.out.print(threshold[s]+ ", ");
                        System.out.print(model.getSolver().getMeasures().getNodeCount() + ", ");
//                        System.out.print(model.getSolver().getMeasures().getBackTrackCount() + ", ");
                        System.out.print(model.getSolver().getMeasures().getFailCount() + ", ");
                        System.out.print(propNValue.dualsolver.iterTot + ", ");
                        System.out.print(model.getSolver().getMeasures().getTimeCount() + ", ");
                        System.out.print(N);
                        if(viewSolution){
                            theSolution.clear();
                            for(int j = 0; j < Y.length; j++){
                                if(Y[j].isInstantiatedTo(1)){
                                    theSolution.add(j);
                                }
                            }
                            System.out.println(", "+theSolution.pretty());
                        }else {System.out.println();}

                    }
                }
            }

        }

    }
}
