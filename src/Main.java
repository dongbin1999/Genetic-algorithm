import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

class data {
    int x,y;
}

public class Main {

    // 2^(range - 1) < a, b, c < 2^(range - 1). MSB 는 부호 결정.
    public static final int range = 8;
    //모든 변수에 대해서 for 문 iterate 번만큼 반복.
    public static final int iterate = 50000;
    //매 시행마다 자손의 개체수.
    public static final int offspring = 4;

    //MSB 고려해서 실제 숫자대로 복원.
    public static int restore(int n) {
        if((n & (1<<(range - 1))) == 0) return n % (1<<(range - 1));
        else return -(n % (1<<(range - 1)));
    }

    //y=ax^2+bx+c
    public static long fx(int a, int b, int c, data[] d) {
        long mse = 0, yHat;

        a = restore(a);
        b = restore(b);
        c = restore(c);

        for (int i = 0; i < d.length; i++) {
            yHat = (long)a * (long)d[i].x * (long)d[i].x + (long)b * (long)d[i].x + (long) c;
            mse += ((long)d[i].y - yHat) * ((long)d[i].y - yHat);
        }
        return mse;
    }

    //맨처음 자손의 값을 랜덤으로 설정.
    public static int[] init() {
        Random r = new Random();
        int[] arr = new int[offspring];
        for(int i=0; i<offspring; i++) {
            arr[i] = r.nextInt(1<<range);
            //System.out.println(arr[i]);
        }
        //System.out.println();
        return arr;
    }

    //x : 현재 관심있는 차수의 자손들, fixed : 현재 관심없는 자손들의 고정된 값들. Case : 현재 관심있는 차수.
    public static int[] selection(int[] x, int[] fixed, int Case, data[] d) {
        long[] f = new long[offspring];
        //mx : 현재 자손들 중 가장 높은 적합도. mn : 현재 자손들 중 가장 낮은 적합도. (적합도 역전에 필요)
        long mx = Long.MIN_VALUE, mn = Long.MAX_VALUE, sum = (long)0;
        for(int i=0; i<offspring; i++) {
            //궁금한 값이 a인 경우. (b, c값을 0으로 고정)
            if(Case == 2) f[i] = fx(x[i], fixed[1], fixed[2], d);
                //궁금한 값이 b인 경우. (a값을 구해둔 값, c값을 0으로 고정)
            else if(Case == 1) f[i] = fx(fixed[0], x[i], fixed[2], d);
                //궁금한 값이 b인 경우. (a, b값을 구해둔 값으로 고정)
            else f[i] = fx(fixed[0], fixed[1], x[i], d);
            mx = Math.max(mx, f[i]);
            mn = Math.min(mn, f[i]);
        }
        //System.out.println("mx : " + mx);
        //System.out.println("mn : " + mn);
        //System.out.println();

        //적합도 역전. (표준편차가 작을수록 높은 확률을 가지므로)
        for(int i=0; i<offspring; i++) {
            f[i] = mx + mn - f[i];
            sum += f[i];
            //System.out.println("f["+i+"] = "+f[i]);
        }

        //데이터와 그래프가 정확히 일치하는 경우, 그대로 리턴.
        if(sum == (long)0) return x;

        double[] ratio = new double[offspring];
        ratio[0] = (double)(f[0] / sum);
        //System.out.println("ratio[" + 0 + "] = " + ratio[0]);
        for (int i = 1; i < offspring; i++) {
            ratio[i] = ratio[i-1] + (double)(f[i] / sum);
            //System.out.println("ratio[" + i + "] = " + ratio[i]);
        }

        //sx : 선택된 자손들.
        int[] sx = new int[offspring];
        Random r = new Random();
        for(int i=0; i<offspring; i++) {
            double p = r.nextDouble();
            int j = 0;
            for (;j < offspring - 1; j++) {
                if(p < ratio[j]) {
                    sx[i] = x[j];
                    break;
                }
            }
            if (j == offspring - 1) sx[i] = x[offspring - 1];
        }
        return sx;
    }

    //%ㅁs에 해당하는 ㅁ을 range 값으로 설정해주어야함에 주의!
    public static String int2String(String k) {
        return String.format("%8s", k).replace(' ', '0');
    }

    public static String[] crossOver(int[] x, data[] d) {
        String[] arr = new String[offspring];
        for(int i=0; i<offspring; i+=2) {
            String bit1 = int2String(Integer.toBinaryString(x[i]));
            String bit2 = int2String(Integer.toBinaryString(x[i+1]));
            //System.out.println("bit1 = "+bit1);
            //System.out.println("bit2 = "+bit2);

            arr[i] = bit1.substring(0, range / 2) + bit2.substring(range / 2, range);
            arr[i+1] = bit2.substring(0, range / 2) + bit1.substring(range / 2, range);
            //System.out.println("arr[" + i + "] = "+arr[i]);
            //System.out.println("arr[" + (i+1) + "] = "+arr[i + 1]);
        }
        return arr;
    }

    public static int invert(String k) {
        Random r = new Random();
        int a = Integer.parseInt(k, 2);
        for(int i=0; i<range; i++) {
            //10번 시행에 평균적으로 1개의 비트만 바꾸기
            double p = (double)1/ (double)(range * 4 * 10);
            if(r.nextDouble() < p) {
                a = (1 << i) ^ a;
            }
        }
        return a;
    }

    public static int[] mutation(String[] k) {
        int[] arr = new int[offspring];
        for (int i=0; i<offspring; i++) {
            arr[i] = invert(k[i]);
            //System.out.println("mutated arr[" + i + "] = "+arr[i]);
        }
        return arr;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        data[] d = new data[108];
        for (int i = 0; i < d.length; i++) {
            d[i] = new data();
            d[i].x = scanner.nextInt();
            d[i].y = scanner.nextInt();
        }

        //답이될 a,b,c가 저장될 자리.
        int[] solution = new int[3];
        solution[0] = solution[1] = solution[2] = 0;

        int Case = 2;
        int[] a = init();
        //best : 반복중 지금까지 찾은 최적의 적합도. (작을수록(찾으려는 데이터과 그래프가 가까울수록) 좋음)
        long best = Long.MAX_VALUE;
        //처음엔 a부터 구하기. (Case = 0)
        for(int i=0; i<iterate; i++) {
            int[] sx = selection(a, solution, Case, d);
            // System.out.println("after selection : ");
            //for (int j = 0; j < sx.length; j++) {
            //    System.out.println(sx[j]);
            //}
            String[] cx = crossOver(sx, d);
            // System.out.println("after crossover : ");
            //for (int j = 0; j < sx.length; j++) {
            //     System.out.println(cx[j]);
            //}
            int[] mx = mutation(cx);
            //System.out.println("after mutation : ");
            //for (int j = 0; j < sx.length; j++) {
            //    System.out.println(mx[j]);
            //}
            long[] f = new long[offspring];
            for(int j = 0; j < offspring; j++) {
                f[j] = fx(mx[j], 0, 0, d);
                if(best >= f[j]) {
                    best = f[j];
                    solution[0] = mx[j];
                }
            }
            a = mx;
        }

        int[] b = init();
        Case = 1;
        best = Long.MAX_VALUE;
        //구해진 a 로부터 b 구하기. (Case = 1)
        for(int i=0; i<iterate; i++) {
            int[] sx = selection(b, solution, Case, d);
            String[] cx = crossOver(sx, d);
            int[] mx = mutation(cx);
            long[] f = new long[offspring];
            for(int j = 0; j < offspring; j++) {
                f[j] = fx(solution[0], mx[j], 0, d);
                if(best >= f[j]) {
                    best = f[j];
                    solution[1] = mx[j];
                }
            }
            b = mx;
        }

        //구해진 a, b 로부터 c 구하기. (Case = 2)
        int[] c = init();
        Case = 0;
        best = Long.MAX_VALUE;
        for(int i=0; i<iterate; i++) {
            int[] sx = selection(c, solution, Case, d);
            String[] cx = crossOver(sx, d);
            int[] mx = mutation(cx);
            long[] f = new long[offspring];
            for(int j = 0; j < offspring; j++) {
                f[j] = fx(solution[0], solution[1], mx[j], d);
                if(best >= f[j]) {
                    best = f[j];
                    solution[2] = mx[j];
                }
            }
            c = mx;
        }

        for (int i = 0; i < 3; i++) {
            solution[i] = restore(solution[i]);
        }
        System.out.printf("y = (%d)x^2 + (%d)x + (%d)", solution[0], solution[1], solution[2]);
    }
}