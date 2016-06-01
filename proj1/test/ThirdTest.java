package com.marques.test;
public class ThirdTest {
	public int test3() {
		int a = 3;
		int b = 2;
		int c = a * b;
		return c;
	}
}
class SecondTest {
	public int test1() {
		int n = 10;
		int sum = 0;
		for (int i = 0; i < n; i++) {
			sum = sum + i;
		}
		return sum;
	}
	public void test2() {
		int n = 10;
		while (n > 0) {
			n--;
		}
	}
}