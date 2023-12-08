package main.AtMostNValue.SubGradient;

public class GeometricSubGradient extends SubGradient{

    protected double ratio;

    public GeometricSubGradient(GradientObject object) {
        this.object = object;
        this.muk = object.mu0;
        this.ratio = object.ratio;
    }
    @Override
    public boolean coefficient(int k) {
        muk = object.mu0 * Math.pow(ratio, k);
        if(muk<0.0001){
            return false;
        }else{return true;}
//        return true;
    }

    @Override
    public double getMuk() {
        return muk;
    }

    @Override
    public void reset(double UB, double LB, double[] lambda, int[] nbYSetToOneInDom) {
    }
}
