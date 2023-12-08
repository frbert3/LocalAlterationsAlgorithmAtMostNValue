package main.AtMostNValue.Experiments;

import main.AtMostNValue.FacilityLocationProblem.*;
import main.AtMostNValue.SubGradient.GradientMethod;
import main.AtMostNValue.SubGradient.GradientObject;
import main.AtMostNValue.heuristics.Heuristic;

import java.io.FileNotFoundException;
import java.util.Formatter;

public class PMedianProblem {

    public static double[] proba = new double[]{0.1, 0.2, 0.3};
    public static int nbS = 30;
    public static int nbW = 30;

    protected static double proba_current;
    public static int nbInst = 10;
    public static String file = "NONE";

    public static boolean domination= false;

    public static boolean warmstart=false;
    //    public static GradientMethod[] grad_modes = new GradientMethod[]{GradientMethod.Geometric,
//            GradientMethod.Harmonic,
//            GradientMethod.Newton};
    public static GradientMethod[] grad_modes = new GradientMethod[]{
            GradientMethod.Newton};
    public static Heuristic[] heurs_modes = new Heuristic[]{Heuristic.COST};
    public static int timelimit = 300;
    public static String pb = "dfl";

    public static void genericTest(FacilityLocationData wld, TestSolver.Heuristiques heuristic, double[] threshold, int[] NOS) {
        GradientObject gradientObject = null;

        for (GradientMethod gtype : grad_modes) {

            if (gtype == GradientMethod.Harmonic) {
//                    GlobalConstraintFactory.setHarmonicGradient(100000, 100000);
            } else if (gtype == GradientMethod.Geometric) {
//                    GlobalConstraintFactory.setPowGradient(1000, 0.95, 1000, 1000);
            } else if (gtype == GradientMethod.Newton) {//System.out.println("(Benchmark 381) Initialisation de Newton");
                gradientObject = new GradientObject(GradientMethod.Newton, 1.0, 5.0);
            }

            for(int ti = 0; ti < threshold.length; ti++){
                for(int ni = 0; ni < NOS.length;ni++){
                    runAndPrint(wld, new RestrictedFLSolverCP(wld, RelaxMode.LAG, TestSolver.CPModels.LNVAL, heuristic, timelimit, domination, gradientObject,NOS[ni],threshold[ti]), heuristic);
                    System.out.println("& "+NOS[ni]+" &   "+ (NOS[ni] > 0 ? threshold[ti] : 0.0));}
            }

//


        }

        //System.out.println();
    }
    public static void runAndPrint(FacilityLocationData wld, TestSolver test, TestSolver.Heuristiques heuristiques) {//System.out.println("(Benchmark 344) Juste avant le \"run\" qui filtre le problème @ "+test.getTime());
        test.run();
        if (test.isFeasible()) {//System.out.println("Feli was here @ Benchmark 340");
            FacilitySolution sol = test.getSolution();
            if (!sol.check()) {
                throw new Error("Error on solution registered by " + test);
            }
        }
        //System.out.println("Feli was here @ Benchmark 346");
        printRes(wld, test, heuristiques);

    }
    public static void printRes(FacilityLocationData wld, TestSolver stm, TestSolver.Heuristiques heuristiques) {
        printEntete(wld, heuristiques);
        Formatter fmt = new Formatter();
        System.out.print(fmt.format(" %10s & %6s & %8.3f & %10d & %5d & %1d & %1d & %5s ",
                stm.getName(),
                (stm.isUseLR() ? GradientMethod.Newton : ""),
                stm.getTime(),
                stm.getSearchSpace(),
                stm.getRootNodeLb(),
                (stm.isFeasible() ? 1 : 0),
                (stm.timeLimitReached() ? 0 : 1),
                (stm.isFeasible() ? stm.getOptCost() : stm.getOptCost())));
//                (stm.isFeasible() ? stm.getOptCost() : " ")));
    }
    private static String getLastLetter(String s) {
        return s.substring(s.length()-1, s.length());
    }

    public static void printEntete(FacilityLocationData data, TestSolver.Heuristiques heuristiques) {
        Formatter fmt = new Formatter();
        if (!file.equals("NONE")) {
            System.out.print(fmt.format("%18s & %5d & %5d & %5s &", data.getName(), data.getNbW(), data.getNbS(), getLastLetter(data.getName())));
        } else {
            System.out.print(fmt.format("%18s & %5d & %5d & %5.2f &", data.getName(), nbW, nbS, proba_current));
        }
        fmt = new Formatter();
        System.out.print(fmt.format(" %9s & %2s & %2s &", heuristiques, domination ? 1 : 0, warmstart ? 1 : 0));
    }



    public static void printTOPLine() {
        Formatter fmt = new Formatter();
        if (!file.equals("NONE")) {  //("%.3f & %d & %d & %d & %d & %d & ", stm.getTime(), stm.getSearchSpace(), stm.getRootNodeLb(), (stm.isFeasible() ? 1 : 0), (stm.timeLimitReached() ? 0 : 1), stm.getOptCost());
            System.out.print(fmt.format("%18s & %5s & %5s & %5s &", "NAME", "N", "M", "CLASS"));
        } else {
            System.out.print(fmt.format("%18s & %5s & %5s & %5s &", "NAME", "N", "M", "PROB"));
        }
        fmt = new Formatter();
        System.out.println(fmt.format(" %9s & %2s & %2s & %10s & %6s & %8s & %10s & %5s & %1s & %1s & %5s ", "HEUR", "DO", "WS", "ALGO", "GRAD", "CPU", "SEARCH", "RLB", "F", "S", "BEST"));
    }

    public static void p_median(int i,TestSolver.Heuristiques heuristic, double[] threshold, int[] NOS) {
        String extension = "A";
        if (i == 31) extension = "B";
        else if (i == 33) extension = "C";
        for (int j = 3; j <= 32; j++) {
            file = "data/pmed_dfl/" + j + "" + i + "PM_Gap" + extension + ".txt";
            FacilityLocationData wld = FacilityParseur.loadPMedianLib(file);
            genericTest(wld, heuristic,threshold, NOS);
        }
    }

    public static void p_median_specific(int i,TestSolver.Heuristiques heuristic, int instance, double[] threshold, int[] NOS) {
        String extension = "A";
        if (i == 31) extension = "B";
        else if (i == 33) extension = "C";
        file = "data/pmed_dfl/" + instance + "" + i + "PM_Gap" + extension + ".txt";
        FacilityLocationData wld = FacilityParseur.loadPMedianLib(file);
        genericTest(wld, heuristic,threshold, NOS);
    }


    public static void main(String[] args) throws FileNotFoundException {

//       PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
//       System.setOut(out);
        printTOPLine();
        domination = true;
        // 31 -> B, 32 -> A, 33 -> C.
        int pbClass = 32;
        pb = String.valueOf(pbClass);
        TestSolver.Heuristiques heuristic= TestSolver.Heuristiques.COST;
        double[] threshold = new double[]{0.40};
        int[] numberOfSteps = new int[]{0, 40};
        p_median(pbClass, heuristic, threshold, numberOfSteps);

        // specific instance
//        int instance = 7;
//        p_median_specific(pbClass, heuristic, instance, threshold, numberOfSteps);



    }
}
