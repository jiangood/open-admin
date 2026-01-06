package io.github.jiangood.sa.common.tools;

public class MathTool {

    public static float percent(int n, int total){
        return  n * 1.0F /total;
    }

    public static String percentStr(int n,int total){
        float percent = percent(n, total);
        String fixed2 = DecimalTool.toFixed2(percent * 100);
        return fixed2 + "%";
    }


}
