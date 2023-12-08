package main.AtMostNValue.InstanceGenerator;


public class DominatingQueen {

    public static int[][] ChessBoard(int n, int m){
        int[][] Board = new int[n][m];
        for(int i = 1; i<=n*m; i++){
            Board[(int) Math.floor((double) (i-1) /(m))][(i-1)%m] = i;
        }
        return Board;
    }

    public  static int[] QDomain(int x, int n, int m){
        int i = (int) Math.floor((double) (x-1) /(m));
        int j = (x-1)%m;
        int count = 1;
        for(int k = 0; k < m; k++){
            if(k!=j){
                count+=1;
            }
        }
        for(int k = 0; k < n; k++){
            if(k!=i){
                count+=1;
            }
        }
        int ibar = i + 1;
        int jbar = j + 1;
        while(ibar < n && jbar < m){
            count += 1;
            ibar+=1;
            jbar+=1;
        }

        ibar = i - 1;
        jbar = j - 1;
        while(ibar >= 0 && jbar >= 0){
            count += 1;
            ibar-=1;
            jbar-=1;
        }

        ibar = i - 1;
        jbar = j + 1;
        while(ibar >= 0 && jbar < m){
            count += 1;
            ibar-=1;
            jbar+=1;
        }

        ibar = i + 1;
        jbar = j - 1;
        while(ibar < n  && jbar >= 0){
            count += 1;
            ibar+=1;
            jbar-=1;
        }

        int[] dom = new int[count];
        dom[0] = x;
        int index = 1;
        for(int k = 0; k < m; k++){
            if(k!=j){
                dom[index] = m*i+k+1;
                index+=1;
            }
        }
        for(int k = 0; k < n; k++){
            if(k!=i){
                dom[index] = m*k+j+1;
                index+=1;
            }
        }
        ibar = i + 1;
        jbar = j + 1;
        while(ibar < n && jbar < m){
            dom[index] = m*ibar+jbar+1;
            index += 1;
            ibar+=1;
            jbar+=1;
        }

        ibar = i - 1;
        jbar = j - 1;
        while(ibar >= 0 && jbar >= 0){
            dom[index] = m*ibar+jbar+1;
            index += 1;
            ibar-=1;
            jbar-=1;
        }

        ibar = i - 1;
        jbar = j + 1;
        while(ibar >= 0 && jbar < m){
            dom[index] = m*ibar+jbar+1;
            index += 1;
            ibar-=1;
            jbar+=1;
        }

        ibar = i + 1;
        jbar = j - 1;
        while(ibar < n  && jbar >= 0){
            dom[index] = m*ibar+jbar+1;
            index += 1;
            ibar+=1;
            jbar-=1;
        }

        return dom;
    }


}
