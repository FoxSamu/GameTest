package net.shadew.gametest.testmc;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

public class TestResults {
    private final Collection<TestInstance> tests = Lists.newArrayList();
    @Nullable
    private Collection<ITestCallback> listeners = Lists.newArrayList();

    public TestResults() {
    }

    public TestResults(Collection<TestInstance> tests) {
        tests.addAll(tests);
    }

    public void add(TestInstance test) {
        tests.add(test);
        listeners.forEach(test::addListener);
    }

    public void addListener(ITestCallback callback) {
        listeners.add(callback);
        tests.forEach(test -> test.addListener(callback));
    }

    public void onFailed(Consumer<TestInstance> consumer) {
        addListener(new ITestCallback() {
            @Override
            public void onStarted(TestInstance instance) {
            }

            @Override
            public void onFailed(TestInstance instance) {
                consumer.accept(instance);
            }

            @Override
            public void onPassed(TestInstance instance) {
            }
        });
    }

    public int getFailedRequiredTestCount() {
        return (int) tests.stream()
                          .filter(TestInstance::isFailed)
                          .filter(TestInstance::isRequired)
                          .count();
    }

    public int getFailedOptionalTestCount() {
        return (int) tests.stream()
                          .filter(TestInstance::isFailed)
                          .filter(TestInstance::isOptional)
                          .count();
    }

    public int getCompletedTestCount() {
        return (int) tests.stream()
                          .filter(TestInstance::isCompleted)
                          .count();
    }

    public boolean failed() {
        return getFailedRequiredTestCount() > 0;
    }

    public boolean hasFailedOptionalTests() {
        return getFailedOptionalTestCount() > 0;
    }

    public int getTestCount() {
        return tests.size();
    }

    public boolean isDone() {
        return getCompletedTestCount() == getTestCount();
    }

    public String getResultString() {
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        tests.forEach(inst -> {
            if (!inst.isStarted()) {
                buf.append(' ');
            } else if (inst.isPassed()) {
                buf.append('+');
            } else if (inst.isFailed()) {
                buf.append(inst.isRequired() ? 'X' : 'x');
            } else {
                buf.append('_');
            }
        });
        buf.append(']');
        return buf.toString();
    }

    public String toString() {
        return getResultString();
    }
}
