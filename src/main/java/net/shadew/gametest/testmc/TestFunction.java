package net.shadew.gametest.testmc;

import net.minecraft.util.Rotation;

import java.util.function.Consumer;

public class TestFunction {
   private final String batch;
   private final String id;
   private final String structure;
   private final boolean required;
   private final Consumer<TestHelper> method;
   private final int timeout;
   private final long predelay;
   private final Rotation rot;

   public TestFunction(String cls, String id, GameTest annotation, Consumer<TestHelper> method) {
      this.id = cls + "." + id;

      String struct = annotation.structure();
      String batch = annotation.batch();
      this.batch = batch.isEmpty() ? cls : batch;
      this.structure = struct.isEmpty() ? id : struct;
      this.required = annotation.required();
      this.method = method;
      this.timeout = annotation.timeout();
      this.predelay = annotation.predelay();
      this.rot = TestStructureHelper.getRotation(annotation.rotation());
   }

   public void start(TestHelper testHelper) {
      method.accept(testHelper);
   }

   public String id() {
      return id;
   }

   public String structure() {
      return structure;
   }

   @Override
   public String toString() {
      return id;
   }

   public int timeout() {
      return timeout;
   }

   public boolean required() {
      return required;
   }

   public String batch() {
      return batch;
   }

   public long predelay() {
      return predelay;
   }

   public Rotation rotation() {
      return rot;
   }
}
