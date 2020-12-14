package net.shadew.gametest.testmc;

import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

public class TestBatch {
   private final String id;
   private final Collection<TestFunction> functions;
   @Nullable
   private final Consumer<ServerWorld> worldSetter;

   public TestBatch(String id, Collection<TestFunction> functions, @Nullable Consumer<ServerWorld> worldSetter) {
      if (functions.isEmpty()) {
         throw new IllegalArgumentException("A TestBatch must include at least one TestFunction!");
      } else {
         this.id = id;
         this.functions = functions;
         this.worldSetter = worldSetter;
      }
   }

   public String getId() {
      return id;
   }

   public Collection<TestFunction> getFunctions() {
      return functions;
   }

   public void setWorld(ServerWorld world) {
      if (worldSetter != null) {
         worldSetter.accept(world);
      }
   }
}
