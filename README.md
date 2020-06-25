# 기말고사 프로젝트 (유전 알고리즘)

#### 201901694 이동빈



## #1. 코드 동작 방식

* 범위, 초기값 설정

  y=ax^2+bx+c 의 회귀식을 구하기 위해, -2^7 < a,b,c < 2^7 로 설정하였습니다.

  257개의 숫자를 나타내기 위해, 각 자손의 비트 수는 __8비트__를 사용하였습니다.

  _(음수를 표현하기 위해, MSB(최상위 비트)를 부호 비트로 사용하였습니다.)_

  

  반복의 횟수는 __각 계수당 5만번__씩 시행하였고,

  처음 자손들은 범위 내의 랜덤한 값을 가지고 시작합니다.

  각 세대마다 개체수는 __4개__로 설정하였고,
  
  평균적으로 10번의 시행 당 1개의 비트에 돌연변이가 일어나도록, 돌연변이 확률을 __1/320__ 으로 설정하였습니다.
  
  자손을 교차시킬때는, __앞부분 절반의 비트와 뒷부분 절반의 비트__ 를 교차시켰습니다.



## #2. 코드 성능 검증

위에서 설명한 유전 알고리즘 코드는 아래와 같습니다. (주석은 첨부된 파일에 달아놓았습니다.)

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

class data {
    int x,y;
}

public class Main {
    public static final int range = 8;
    public static final int iterate = 50000;
    public static final int offspring = 4;
    
    public static int restore(int n) {
        if((n & (1<<(range - 1))) == 0) return n % (1<<(range - 1));
        else return -(n % (1<<(range - 1)));
    }

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

    public static int[] init() {
        Random r = new Random();
        int[] arr = new int[offspring];
        for(int i=0; i<offspring; i++) {
            arr[i] = r.nextInt(1<<range);
        }
        return arr;
    }

    public static int[] selection(int[] x, int[] fixed, int Case, data[] d) {
        long[] f = new long[offspring];
        long mx = Long.MIN_VALUE, mn = Long.MAX_VALUE, sum = (long)0;
        for(int i=0; i<offspring; i++) {
            if(Case == 2) f[i] = fx(x[i], fixed[1], fixed[2], d);
            else if(Case == 1) f[i] = fx(fixed[0], x[i], fixed[2], d);
            else f[i] = fx(fixed[0], fixed[1], x[i], d);
            mx = Math.max(mx, f[i]);
            mn = Math.min(mn, f[i]);
        }

        for(int i=0; i<offspring; i++) {
            f[i] = mx + mn - f[i];
            sum += f[i];
        }
        
        if(sum == (long)0) return x;

        double[] ratio = new double[offspring];
        ratio[0] = (double)(f[0] / sum);
        for (int i = 1; i < offspring; i++) {
            ratio[i] = ratio[i-1] + (double)(f[i] / sum);
        }

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

    public static String int2String(String k) {
        return String.format("%8s", k).replace(' ', '0');
    }

    public static String[] crossOver(int[] x, data[] d) {
        String[] arr = new String[offspring];
        for(int i=0; i<offspring; i+=2) {
            String bit1 = int2String(Integer.toBinaryString(x[i]));
            String bit2 = int2String(Integer.toBinaryString(x[i+1]));
            arr[i] = bit1.substring(0, range / 2) + bit2.substring(range / 2, range);
            arr[i+1] = bit2.substring(0, range / 2) + bit1.substring(range / 2, range);
        }
        return arr;
    }

    public static int invert(String k) {
        Random r = new Random();
        int a = Integer.parseInt(k, 2);
        for(int i=0; i<range; i++) {
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

        int[] solution = new int[3];
        solution[0] = solution[1] = solution[2] = 0;

        int Case = 2;
        int[] a = init();
        long best = Long.MAX_VALUE;
        for(int i=0; i<iterate; i++) {
            int[] sx = selection(a, solution, Case, d);
            String[] cx = crossOver(sx, d);
            int[] mx = mutation(cx);
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
```



이 코드가 잘 동작하는지 확인하기 위해, 몇가지 방정식에 대해 __오차가 없는 108개의 데이터__ 를 아래의 코드를 통해 생성하여 값을 구해보았습니다. 

```c++
#include <cstdio>

int main(void)
{
	//a,b,c : y=ax^2+bx+c 의 계수 a, b, c.
	int a, b, c, y;
	scanf("%d %d %d", &a, &b, &c);
	for (int x = -50; x <= 57; x++)
	{
		y = a * x * x + b * x + c;
		printf("%d %d ", x, y);
	}
	return 0;
}
```



그 결과는 아래와 같습니다.

|    실제 방정식     |   예측한 회귀식    |
| :----------------: | :----------------: |
|        y=50        |        y=50        |
|        y=2x        |        y=2x        |
|       y=3x+3       |       y=3x+3       |
|      y=-10x+7      |      y=-10x+7      |
|    y=-3x^2+5x+7    |    y=-3x^2+5x+7    |
|   y=10^x-19x+57    |   y=10^x-19x+57    |
|  y=100^x-50x+122   |  y=100^x-50x+122   |
| y=-127x^2-127x-127 | y=-127x^2-127x-127 |
| y=127x^2+127x+127  | y=127x^2+127x+127  |

오차가 없는 데이터에서는, 모든 예제에 대해 제대로된 답을 도출하는것을 확인할 수 있었습니다.



## #3. 데이터 선정

게임 LOL의 [통계자료](https://www.op.gg/statistics/tier/) 에서, 소환사의 협곡 랭크게임을 플레이하는 유저의 티어 대비 와드 구매 횟수가 우상향 직선 그래프와 유사한 모양임을 직접 확인하고싶었습니다.



그래서 아래의 기준을 모두 만족하는 데이터를 수집하였습니다.

1. 각 티어에서 200게임 이상 솔로랭크를 플레이한 유저의 전적
2. 게임 시간이 30분 근처인 게임 _(게임 진행시간이 횟수에 영향을 미칠 수 있어서)_
3. 게임에 참여한 10명 중 고의로 게임을 망친 플레이어가 없는 경우



데이터의 각 숫자가 의미하는 바는 아래와 같습니다.

|  x값  |      의미       |
| :---: | :-------------: |
|  1~4  |   아이언 I~IV   |
|  5~8  |   브론즈 I~IV   |
| 9~12  |    실버 I~IV    |
| 13~16 |    골드 I~IV    |
| 17~20 |  플래티넘 I~IV  |
| 21~24 | 다이아몬드 I~IV |
|  28   |     마스터      |
|  33   |  그랜드마스터   |
|  38   |     챌린저      |



|     y값     |                           의미                            |
| :---------: | :-------------------------------------------------------: |
| 0~제한 없음 | 게임을 플레이 한 팀원 5명의 와드 구매 횟수를 전부 더한 값 |



## #4. 결과

수집한 데이터를 통해 얻고싶었던 결과는 계수가 양수인 1차함수, 또는 계수가 1인 2차함수였고,

코드가 예측한 회귀식은 __y=x+1__ 로, 통계치와 비슷한 우상향 그래프임을 알 수 있었습니다.

(계수를 정수 범위에서 구했기 때문에, 실제로는 더 낮은 기울기임에도 1을 출력한게 아닌가 의심되어 데이터의 y값에 각각 5, 10을 곱하여 실험해 본 결과, 기울기가 그대로 5, 10임을 확인했습니다.)



그래프는 아래의 코드를 통해 출력하였습니다.

```python
import numpy as np
import matplotlib.pyplot as plt
x=np.arange(0,40)
y=x+1
fig = plt.figure(figsize=(12,7))
ax = fig.add_subplot(1,1,1)
ax.set_xticks([2.5,6.5,10.5,14.5,18.5,22.5,28,33,38])
ax.set_xticklabels(['Iron','Bronze','Silver','Gold','Platinum','Diamond','Master','Grandmaster','Challenger'])
plt.title('estimated graph', fontsize = 20)
plt.plot(x,y,label='y=x+1',linewidth=2)
plt.legend(fontsize=15)
plt.xlabel('User tier',fontsize = 20)
plt.ylabel('Wards(per 5 games)',fontsize = 15)
x = []
y = []
with open("./데이터.txt","r") as file:
  for i in range(110):
    tmp = file.readline().split()
    x.append(int(tmp[0]))
    y.append(int(tmp[1]))
plt.grid()
plt.scatter(x,y,color='red')
plt.show()
```

![Figure_1](/assets/images/Figure_1.png)

