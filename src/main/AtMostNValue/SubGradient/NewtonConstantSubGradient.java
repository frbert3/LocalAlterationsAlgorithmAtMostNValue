package main.AtMostNValue.SubGradient;

public class NewtonConstantSubGradient extends SubGradient{

    protected double UB;
    protected double LB;
    protected double[] lambda;
    protected int[] nbYSetToOneInDom;
    protected double numerator;
    protected double denominator;
    public NewtonConstantSubGradient(GradientObject object) {
        this.object = object;
        this.muk = object.mu0;
    }
    @Override
    public boolean coefficient(int k) {
        if(denominator==0){
            return false;
        }else {

            muk = (object.mu0 * numerator) / denominator;
            if(muk>UB*object.mu0) { // to avoid numerical issue
                muk = UB*object.mu0;
            }

            return (muk > 0.00001);

        }
    }

    @Override
    public double getMuk() {
        return muk;
    }


    @Override
    public void reset(double UB, double LB, double[] lambda, int[] nbYSetToOneInDom) {
        this.UB = UB;
        this.LB = LB;
        this.lambda = lambda;
        this.nbYSetToOneInDom = nbYSetToOneInDom;
        this.numerator = UB + 0.0001 - LB;
        CalculDenominateur();

    }

    public double CalculDenominateur(){
        double gamma = 0d;
        for(int i = 0; i < nbYSetToOneInDom.length; i++){
            double valGrad = 1.0 - nbYSetToOneInDom[i];
            if(!((lambda[i]==0)&&(valGrad<0))){
                gamma += valGrad * valGrad;
            }
        }
        this.denominator = gamma;
        return gamma;
    }
    public double getNumerator() {
        return numerator;
    }
    public double getDenominator() {
        return denominator;
    }
}
