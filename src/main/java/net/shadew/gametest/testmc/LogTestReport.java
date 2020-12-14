package net.shadew.gametest.testmc;

import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTestReport implements ITestReport {
   private static final Logger LOGGER = LogManager.getLogger();

   @Override
   public void onTestFailed(TestInstance instance) {
      if (instance.isRequired()) {
         LOGGER.error(instance.getName() + " failed! " + Util.getInnermostMessage(instance.getError()));
      } else {
         LOGGER.warn("(optional) " + instance.getName() + " failed. " + Util.getInnermostMessage(instance.getError()));
      }
   }

   @Override
   public void onTestPassed(TestInstance instance) {

   }
}
