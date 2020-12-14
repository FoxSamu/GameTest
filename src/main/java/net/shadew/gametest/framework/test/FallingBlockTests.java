package net.shadew.gametest.framework.test;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import net.shadew.gametest.framework.api.*;
import net.shadew.gametest.framework.api.annotation.GameTest;

@GameTest.Class("gametest")
@SuppressWarnings("deprecation")
public final class FallingBlockTests {
    private static void runTest(TestController c, Block block) {
        c.setBlock(c.frame("source"), block.getDefaultState());
        c.newSequence()
         .wait(() -> c.assertBlockMatches(c.frame("result"), (pos, state) -> !state.isAir()))
         .run(() -> c.assertBlockIs(c.frame("result"), block))
         .pass();
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void sand(TestController controller) {
        runTest(controller, Blocks.SAND);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void gravel(TestController controller) {
        runTest(controller, Blocks.GRAVEL);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void black(TestController controller) {
        runTest(controller, Blocks.BLACK_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void brown(TestController controller) {
        runTest(controller, Blocks.BROWN_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void blue(TestController controller) {
        runTest(controller, Blocks.BLUE_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void cyan(TestController controller) {
        runTest(controller, Blocks.CYAN_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void gray(TestController controller) {
        runTest(controller, Blocks.GRAY_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void green(TestController controller) {
        runTest(controller, Blocks.GREEN_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void lightBlue(TestController controller) {
        runTest(controller, Blocks.LIGHT_BLUE_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void lightGray(TestController controller) {
        runTest(controller, Blocks.LIGHT_GRAY_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void lime(TestController controller) {
        runTest(controller, Blocks.LIME_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void magenta(TestController controller) {
        runTest(controller, Blocks.MAGENTA_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void orange(TestController controller) {
        runTest(controller, Blocks.ORANGE_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void white(TestController controller) {
        runTest(controller, Blocks.WHITE_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void yellow(TestController controller) {
        runTest(controller, Blocks.YELLOW_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void pink(TestController controller) {
        runTest(controller, Blocks.PINK_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void purple(TestController controller) {
        runTest(controller, Blocks.PURPLE_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void red(TestController controller) {
        runTest(controller, Blocks.RED_CONCRETE_POWDER);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void anvil1(TestController controller) {
        runTest(controller, Blocks.ANVIL);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void anvil2(TestController controller) {
        runTest(controller, Blocks.CHIPPED_ANVIL);
    }

    @GameTest(template = "gametest:testtests.sand")
    public static void anvil3(TestController controller) {
        runTest(controller, Blocks.DAMAGED_ANVIL);
    }
}
