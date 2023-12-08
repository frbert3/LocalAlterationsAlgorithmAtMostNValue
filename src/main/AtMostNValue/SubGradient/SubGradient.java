package main.AtMostNValue.SubGradient;

public abstract class SubGradient {
    protected GradientObject object;
    protected double muk;

    public abstract boolean coefficient(int k);
    public abstract double getMuk();
    public abstract void reset(double UB, double LB, double[] lambda, int[] nbYSetToOneInDom);
//    public abstract void analysisCoefficient( double UpperBound, double LBCourante, double[] subGrad, double[] lambda)
}
