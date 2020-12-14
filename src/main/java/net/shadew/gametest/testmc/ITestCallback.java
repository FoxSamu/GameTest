package net.shadew.gametest.testmc;

public interface ITestCallback {
   void onStarted(TestInstance instance);
   void onFailed(TestInstance instance);
   void onPassed(TestInstance instance);
}
