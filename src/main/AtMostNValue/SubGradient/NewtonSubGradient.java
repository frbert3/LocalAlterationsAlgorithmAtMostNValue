package main.AtMostNValue.SubGradient;

public class NewtonSubGradient extends SubGradient{

    protected double UB;
    protected double LB;
    protected double[] lambda;
    protected int[] nbYSetToOneInDom;
    protected double numerator;
    protected double denominator;
    public NewtonSubGradient(GradientObject object) {
        this.object = object;
        this.muk = object.mu0;
    }
    @Override
    public boolean coefficient(int k) {
        if(denominator==0){
            return false;
        }else {
            double alpha = (object.mu0 / Math.pow(2, Math.floor(k / 10.0)));
            muk = (alpha * numerator) / denominator; // celui d'hadrien

            if(muk>UB*alpha) { // to avoid numerical issue
                muk = UB*alpha;
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
//        System.out.println(numerator);
        CalculDenominateur();

    }

    public double CalculDenominateur(){
        double gamma = 0d;
        for(int i = 0; i < nbYSetToOneInDom.length; i++){
            double valGrad = 1.0 - nbYSetToOneInDom[i];
            if(!((lambda[i]==0)&&(valGrad<0))){ // H a if( (lambda != 0) || (subGrab > 0)) (le !() du mien)
//                gamma+=Math.pow(subGrad[i], 2);
                gamma+= valGrad * valGrad;
            }
        }
        this.denominator = gamma;
        return gamma;
    }

    public double CalculWeightedDenominateur(int[] capacity){
        double gamma = 0d;
        for(int i = 0; i < nbYSetToOneInDom.length; i++){
            double valGrad = capacity[i] - nbYSetToOneInDom[i];
//            if(!((lambda[i]==0)&&(valGrad>0))){ // H a if( (lambda != 0) || (subGrab > 0)) (le !() du mien)
//                gamma+=Math.pow(subGrad[i], 2);
            gamma+= valGrad * valGrad;
//            }
        }
        this.denominator = gamma;
        return gamma;
    }

    //    @Override
    public void reset2(double UB, double LB, double[] lambda, int[] nbYSetToOneInDom, int[] capacity) {
        this.UB = UB;
        this.LB = LB;
        this.lambda = lambda;
        this.nbYSetToOneInDom = nbYSetToOneInDom;
        this.numerator = UB + 0.0001 - LB;
//        System.out.println(numerator);
        CalculWeightedDenominateur(capacity);

    }
}
