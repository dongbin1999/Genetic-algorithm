#include <cstdio>

int main(void)
{
	//a,b,c : y=ax^2+bx+c ÀÇ °è¼ö a, b, c.
	int a, b, c, y;
	scanf("%d %d %d", &a, &b, &c);
	for (int x = -50; x <= 57; x++)
	{
		y = a * x * x + b * x + c;
		printf("%d %d ", x, y);
	}
	return 0;
}