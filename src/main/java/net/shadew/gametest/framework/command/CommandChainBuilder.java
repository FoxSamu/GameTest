package net.shadew.gametest.framework.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public class CommandChainBuilder {
    private final List<Entry> entries = new ArrayList<>();

    public CommandChainBuilder required(ArgumentBuilder<CommandSource, ?> builder) {
        entries.add(new Entry(builder, true));
        return this;
    }

    public CommandChainBuilder optional(ArgumentBuilder<CommandSource, ?> builder) {
        entries.add(new Entry(builder, false));
        return this;
    }

    public ArgumentBuilder<CommandSource, ?> build() {
        if (entries.isEmpty()) {
            throw new IllegalStateException("Add at least one argument");
        }
        if (entries.size() > 1) {
            build(entries.get(0).builder, 1);
        }
        return entries.get(0).builder;
    }

    private void build(ArgumentBuilder<CommandSource, ?> prev, int index) {
        int last = entries.size() - 1;
        Entry entry = entries.get(index);
        if (index != last) {
            build(entry.builder, index + 1);
            prev.then(entry.builder);
            if (!entry.required) {
                build(prev, index + 1);
            }
        } else {
            prev.then(entry.builder);
        }
    }

    private static class Entry {
        private final ArgumentBuilder<CommandSource, ?> builder;
        private final boolean required;

        private Entry(ArgumentBuilder<CommandSource, ?> builder, boolean required) {
            this.builder = builder;
            this.required = required;
        }
    }
}
