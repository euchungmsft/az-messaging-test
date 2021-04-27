package com.eg.az.test.common;

public class TestHook extends Thread {
	int skip = 150;
	
	TestCase runner;
	
	public TestHook(TestCase runner) {
		this.runner = runner;
	}
	
	public void run() {
		long total = 0, max = 0, min = 10000, val;
		int len = runner.getData().size();
    	for(int i=skip; i<len; i++) {
    		val = runner.getData().get(i).longValue();
    		total += val;
    		max = max < val ? val : max;
    		min = min > val ? val : min;
    	}
    	
    	System.out.println("AVG : " + (total/(len-skip)));
    	System.out.println("MAX : " + max);
    	System.out.println("MIN : " + min);
    	System.out.println("CNT : " + (len-skip));
	}
}
