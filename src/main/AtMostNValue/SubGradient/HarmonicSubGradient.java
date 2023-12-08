package main.AtMostNValue.SubGradient;

public class HarmonicSubGradient extends SubGradient{

    public HarmonicSubGradient(GradientObject object) {
        this.object = object;
        this.muk = object.mu0;
    }
    @Override
    public boolean coefficient(int k) {
        if(k==0){
            muk = object.ratio;
            return true;
        }else{
            muk = object.mu0 * (object.ratio / k);
            return !(muk < 0.0001);

        }
    }

    @Override
    public double getMuk() {
        return muk;
    }

    @Override
    public void reset(double UB, double LB, double[] lambda, int[] nbYSetToOneInDom) {

    }
}

