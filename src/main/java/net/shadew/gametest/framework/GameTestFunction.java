package net.shadew.gametest.framework;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.world.server.ServerWorld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import net.shadew.gametest.framework.api.annotation.GameTest;
import net.shadew.gametest.framework.api.TestController;
import net.shadew.gametest.framework.api.exception.TestException;
import net.shadew.gametest.util.Utils;

public class GameTestFunction {
    private final String testClass;
    private final String batch;
    private final ResourceLocation name;
    private final ResourceLocation templateLocation;
    private final int predelay;
    private final int timeout;
    private final boolean required;
    private final Rotation rotation;
    private final Consumer<TestController> testMethod;

    public GameTestFunction(String namespace, Class cls, String clsName, Method method, GameTest testAnnotation, GameTest.Class classAnnotation) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalTestException("@GameTest method is not static");
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalTestException("@GameTest method is not public");
        }

        Class[] params = method.getParameterTypes();
        if (params.length < 1) {
            throw new IllegalTestException("@GameTest method does not have a first TestController argument");
        }

        if (params[0] != TestController.class) {
            throw new IllegalTestException("@GameTest method first argument is not of type TestController");
        }

        boolean acceptWorld = false;

        if (params.length > 1) {
            if (params.length != 2) {
                throw new IllegalTestException("@GameTest method has extra arguments");
            } else {
                if (params[1] != ServerWorld.class) {
                    throw new IllegalTestException("@GameTest method second argument is not of type ServerWorld");
                }
            }
            acceptWorld = true;
        }

        String name = method.getName().toLowerCase();
        String overrideName = testAnnotation.value();
        if (!overrideName.isEmpty()) {
            name = overrideName;
        }

        String batch = testAnnotation.batch();

        this.testClass = clsName;
        this.batch = batch.isEmpty() ? clsName : batch;
        this.name = new ResourceLocation(namespace, clsName + "." + name);

        this.predelay = testAnnotation.predelay() < 0 ? classAnnotation.predelay() : testAnnotation.predelay();
        this.timeout = testAnnotation.timeout() < 0 ? classAnnotation.timeout() : testAnnotation.timeout();
        this.required = testAnnotation.required();
        this.rotation = Utils.rotationFromSteps(testAnnotation.rotation());

        String template = testAnnotation.template();
        if(template.isEmpty()) {
            this.templateLocation = this.name;
        } else if(template.contains(":")) {
            this.templateLocation = new ResourceLocation(template);
        } else {
            this.templateLocation = new ResourceLocation(namespace, template);
        }

        if (acceptWorld) {
            testMethod = controller -> {
                try {
                    method.invoke(null, controller, controller.getWorld());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new TestException(e);
                }
            };
        } else {
            testMethod = controller -> {
                try {
                    method.invoke(null, controller);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new TestException(e);
                }
            };
        }
    }

    public String getTestClass() {
        return testClass;
    }

    public String getBatch() {
        return batch;
    }

    public ResourceLocation getTestClassId() {
        return new ResourceLocation(getName().getNamespace(), getTestClass());
    }

    public ResourceLocation getBatchId() {
        return new ResourceLocation(getName().getNamespace(), getBatch());
    }

    public ResourceLocation getName() {
        return name;
    }

    public ResourceLocation getTemplate() {
        return templateLocation;
    }

    public int getPredelay() {
        return predelay;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isOptional() {
        return !required;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void runTestMethod(TestController controller) {
        testMethod.accept(controller);
    }
}
