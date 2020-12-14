package net.shadew.gametest.framework.test;

import net.shadew.gametest.framework.api.annotation.GameTest;

@GameTest.Class("gametest")
public final class TestTests {
//    @BeforeBatch("sandtests")
//    public static void beforeSand(ServerWorld world) {
//        System.out.println("BeforeBatch :)");
//    }

//    @GameTest(batch = "sandtests")
//    public static void sand(TestController controller) {
//        System.out.println("TestFunction :)");
//        GameTestInstance instance = controller.getInstance();
//
//        BlockPos source = instance.getFramePos("source");
//        BlockPos result = instance.getFramePos("result");
//        instance.getWorld().setBlockState(source, Blocks.SAND.getDefaultState());
//
//        instance.addMarker(result, Marker.WORKING, "");
//
//        instance.runAtTick(70, () -> {
//            BlockState state = instance.getWorld().getBlockState(result);
//            System.out.println("Assertion time! :)");
//
//            if(state.isIn(Blocks.SAND)) {
//                instance.addMarker(result, Marker.OK, "");
//                instance.pass();
//            } else {
//                instance.addMarker(result, Marker.ERROR, "Expected sand here");
//                instance.fail(new Exception("Expected sand"));
//            }
//        });
//    }
//
//    @GameTest(batch = "sandtests")
//    public static void sandFail(TestController controller) {
//        System.out.println("TestFunction :)");
//        GameTestInstance instance = controller.getInstance();
//
//        BlockPos source = instance.getFramePos("source");
//        BlockPos result = instance.getFramePos("result");
//        instance.getWorld().setBlockState(source, Blocks.GRAVEL.getDefaultState());
//
//        instance.runAtTick(70, () -> {
//            System.out.println("Assertion time! :)");
//            BlockState state = instance.getWorld().getBlockState(result);
//            if(state.isIn(Blocks.SAND)) {
//                instance.pass();
//            } else {
//                instance.fail(new Exception("Expected sand"));
//            }
//        });
//    }
}
