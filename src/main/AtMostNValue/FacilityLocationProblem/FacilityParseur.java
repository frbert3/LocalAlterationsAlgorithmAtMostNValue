package main.AtMostNValue.FacilityLocationProblem;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

/**
 *
 */
public class FacilityParseur {



    /**
     * PMEDIAN Parseur
     * Parsing for DISCRETE FACILITY LOCATION LIB
     * @param file
     * @return An object Warehouse data created from a CSP lib file
     */
    public static FacilityLocationData loadCapacitatedPMedianLib(String file) {

        class Point {
            private int x;
            private int y;
            private Point(int x, int y) {
                this.x = x;
                this.y = y;
            }
            private int distanceTo(Point p) {
                return (int) Math.sqrt((x-p.x)*(x-p.x) + (y-p.y)*(y-p.y));
            }
        }

        FacilityLocationData data = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            br.readLine();
            //problem size
            Scanner scan = new Scanner(br.readLine());
            int nbS = scan.nextInt();
            int nbW = nbS;
            data = new FacilityLocationData(nbW, nbS, new File(file).getName().replace(".txt",""));

            //read capacities and fixed cost
            int maxNbF      = scan.nextInt();
            data.setN(maxNbF);
            int capa        = scan.nextInt();
            for (int j = 0; j < nbW; j++) {
                data.setCapa(capa, j);
                data.setFixedCost(0, j);
            }

            String line = br.readLine();
            Point[] pts = new Point[nbS];
            while (line != null && !line.equals("")) {
                scan = new Scanner(line.trim());
                int facility  = scan.nextInt()-1;
                int x         = scan.nextInt();
                int y         = scan.nextInt();
                pts[facility] = new Point(x,y);
                data.setDemand(scan.nextInt(),facility);
                line = br.readLine();
            }
            for (int i = 0; i < nbS; i++) {
                for (int j = 0; j < nbS; j++) {
                    data.setSupplycosts(pts[i].distanceTo(pts[j]),i,j);
                }
            }
            data.setDemandConstraint(true);
            data.setCardinalityConstraint(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }


    /**
     * PMEDIAN Parseur
     * Parsing for DISCRETE FACILITY LOCATION LIB
     * @param file
     * @return An object Warehouse data
     */
    public static FacilityLocationData loadPMedianLib(String file) {
        FacilityLocationData data = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            br.readLine();
            br.readLine();
            //problem size
            Scanner scan = new Scanner(br.readLine());
            int nbW = scan.nextInt();
            int nbS = nbW;
            data = new FacilityLocationData(nbW, nbS, new File(file).getName().replace(".txt",""));

            //read capacities and fixed cost
            int maxNbF      = scan.nextInt();
            data.setN(maxNbF);
            for (int j = 0; j < nbW; j++) {
                data.setCapa(nbS, j);
                data.setFixedCost(0, j);
            }

            br.readLine();
            String line = br.readLine();
            while (line != null && !line.equals("")) {
                scan = new Scanner(line.trim().replace("\t", " "));
                int facility  = scan.nextInt()-1;
                int client    = Integer.parseInt(scan.next().trim())-1;
                int transcost = Integer.parseInt(scan.next().trim());
                data.setSupplycosts(transcost, client, facility);
                data.setDemand(1, client, facility);
                //System.out.println(line);
                line = br.readLine();
            }
            data.fillMissingDemandByMax();

            data.setDemandConstraint(false);
            data.setCardinalityConstraint(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * CAPACITATED PLANT LOCATION PARSEUR
     * Parsing for DISCRETE FACILITY LOCATION LIB
     * @param file
     * @return An object Warehouse data created from a CSP lib file
     */
    public static FacilityLocationData loadDFLLib(String file) {
        FacilityLocationData data = null;
        int cste = 10;
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            br.readLine();
            br.readLine();
            br.readLine();
            //problem size
            Scanner scan = new Scanner(br.readLine());
            int nbW = scan.nextInt();
            int nbS = nbW;
            data = new FacilityLocationData(nbW, nbS, new File(file).getName().replace(".txt",""));

            //read capacities and fixed cost
            int fixedcost = scan.nextInt()*cste;
            int capa      = scan.nextInt();
            for (int j = 0; j < nbW; j++) {
                data.setCapa(capa, j);
                data.setFixedCost(fixedcost, j);
            }

            br.readLine();
            String line = br.readLine();
            while (line != null && !line.equals("")) {
                scan = new Scanner(line.trim().replace("\t"," "));
                int facility  = scan.nextInt()-1;
                int client    = Integer.parseInt(scan.next().trim())-1;
                int transcost = (int) Math.round(Double.parseDouble(scan.next())*cste);
                int demand    = Integer.parseInt(scan.next().trim());

                data.setSupplycosts(transcost, client, facility);
                data.setDemand(demand, client, facility);
                line = br.readLine();
            }
            data.fillMissingDemandByMax();

            data.setDemandConstraint(true);
            data.setCardinalityConstraint(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Parsing for OR LIB Wharehouse location
     * @param file
     * @return An object Warehouse data created from a CSP lib file
     */
    public static FacilityLocationData loadORLib(String file) {
        FacilityLocationData data = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            //problem size
            Scanner scan = new Scanner(br.readLine());
            int nbW = scan.nextInt();
            int nbS = scan.nextInt();
            data = new FacilityLocationData(nbW,nbS);

            //read capacities and fixed cost
            for (int j = 0; j < nbW; j++) {
                scan = new Scanner(br.readLine().replace(".",""));
                data.setCapa(scan.nextInt(), j);
                data.setFixedCost(scan.nextInt(), j);
            }

            //read demand and supply costs
            for (int i = 0; i < nbS; i++) { //for each store
                data.setDemand(Integer.parseInt(br.readLine().trim()),i);
                int nbL = nbW/7 + ((nbW%7==0)?0:1);  //only 7 values per line
                int j = 0;
                for (int l = 0; l < nbL; l++) {
                    scan = new Scanner(br.readLine());
                    scan.useDelimiter(" ");
                    while (scan.hasNext()) { //for each warehouse
                        Double value = Double.parseDouble(scan.next());
                        int valueint = (int) Math.round(value*100);
                        data.setSupplycosts(valueint, i, j);
                        j++;
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Parsing for CSP LIB
     * @param file
     * @return An object Warehouse data created from a CSP lib file
     */
    public static FacilityLocationData loadCSPLib(String file) {
        FacilityLocationData data = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            //problem size
            int nbW = Integer.parseInt(br.readLine().replace("NbW=","").replace(";",""));
            int nbS = Integer.parseInt(br.readLine().replace("NbS=","").replace(";",""));
            data = new FacilityLocationData(nbW,nbS);

            //read fixed cost
            int fco = Integer.parseInt(br.readLine().replace("fixed=","").replace(";",""));
            for (int i = 0; i < nbW; i++) {
                data.setFixedCost(fco, i);
            }

            //read supply costs
            br.readLine();
            for (int i = 0; i < nbS; i++) { //for each store
                String line  = br.readLine().trim().replace("[","").replace("]","").trim();
                Scanner scan = new Scanner(line);
                scan.useDelimiter(",");
                int j = 0;
                while (scan.hasNext()) { //for each warehouse
                    int value = scan.nextInt();
                    data.setSupplycosts(value, i, j);
                    j++;
                }
            }

            //read capacities
            for (int i = 0; i < nbW; i++) {
                data.setCapa(nbS/2,i);
            }

            for (int i = 0; i < nbS; i++) {
                data.setDemand(1,i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

}
