package main.AtMostNValue.SubGradient;

public class GradientObject {
    public GradientMethod method;
    public double ratio;
    public double mu0;

    public GradientObject(GradientMethod method, double ratio, double mu0){
        this.method = method;
        this.ratio = ratio;
        this.mu0 = mu0;
    }
}