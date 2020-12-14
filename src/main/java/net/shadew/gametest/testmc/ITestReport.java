package net.shadew.gametest.testmc;

public interface ITestReport {
   void onTestFailed(TestInstance instance);
   void onTestPassed(TestInstance instance);
}
