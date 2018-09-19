package Server.RL;

import java.util.*;

public class Policy {


    public static int getAction(Double[][] w, State state, int nbAction) {

        double[] actionProb;

        Double[] actionValues = getQ(w, state, nbAction);

        actionProb = softmax(actionValues);

        for (int i = 1; i < actionProb.length; i++)
            actionProb[i] += actionProb[i - 1];

        Random rand = new Random(new Date().getTime());
        float randFloat = rand.nextFloat();

        for (int i = 0; i < actionProb.length; i++) {
            if (randFloat < actionProb[i])
                return i;
        }

        return rand.nextInt(actionProb.length);
    }

    public static Double[] getQ(Double[][] w, State state, int nbAction) {
        Double[] actionValues = new Double[nbAction];
        for (int i = 0; i < nbAction; i++) {
            Double[] weight = w[i];
            double sum = 0;

            for (int j = 0; j < weight.length; j++) {
                if (j < weight.length / 2) {

                    sum += weight[j] * state.cpuLoads.get(j);
                } else {

                    sum += weight[j] * state.taskCounts.get(j / 2);
                }
            }
            actionValues[i] = sum;
        }
        return actionValues;
//        double max=0.0;
//
//                for(int i=0;i<actionValues.length;i++){
//                if (max<actionValues[i]){
//                    max=actionValues[i];
//                    k=i;
//                }
//                }
//        return max ;
    }


    public static Double[] gradian(Double[][] w, State state, int nbAction) {
        Double gradian[];
        gradian = new Double[2 * state.cpuLoads.size()];
        Double[] actionValues;
        double[] actionProb;
        int maxActionIndex = 0;
        actionValues = getQ(w, state, nbAction);
        double max = 0.0;
        for (int i = 0; i < actionValues.length; i++) {
            if (max < actionValues[i]) {
                max = actionValues[i];
                maxActionIndex = i;
            }
        }
        for (int i = 0; i < w[maxActionIndex].length; i++) {
            if (i < w[maxActionIndex].length / 2) {
                gradian[i] = state.cpuLoads.get(i);
            } else {
                gradian[i] = Double.valueOf(state.taskCounts.get(i / 2));
            }
        }
        return gradian;
    }

    private static double[] softmax(Double[] actionValues) {
        double[] actionProb = new double[actionValues.length];
        double sum = 0.0;
        for (int i = 0; i < actionValues.length; i++)
            sum += Math.exp(actionValues[i] / 10);

        if (sum == 0)
            sum = 1;
        for (int i = 0; i < actionValues.length; i++)
            actionProb[i] = Math.exp(actionValues[i] / 10) / sum;


//        for (int i = 0; i < actionProb.length; i++) {
//            if (sum == 0)
//                sum = 1;
//            actionProb[i] = actionValues[i] / sum;
//        }

        return actionProb;
    }

}
