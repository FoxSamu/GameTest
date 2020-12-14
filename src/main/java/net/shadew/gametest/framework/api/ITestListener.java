package net.shadew.gametest.framework.api;

import net.shadew.gametest.framework.GameTestInstance;

public interface ITestListener {
    default void onStatusChange(GameTestInstance instance, TestStatus status) {

    }

    default void onInit(GameTestInstance instance) {

    }

    default void onStart(GameTestInstance instance) {

    }

    default void onPass(GameTestInstance instance) {

    }

    default void onFail(GameTestInstance instance, Throwable failure) {

    }
}
