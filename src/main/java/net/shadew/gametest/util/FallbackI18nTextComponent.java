package net.shadew.gametest.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UnknownFormatFlagsException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FallbackI18nTextComponent extends TextComponent implements ITargetedTextComponent {
    private static final Pattern FORMAT_FLAG = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    private static final Object[] NO_ARGS = new Object[0];
    private static final ITextProperties PERCENT_SIGN = ITextProperties.plain("%");
    private static final ITextProperties NULL = ITextProperties.plain("null");

    private final String key;
    private final String defaultTranslation;
    private final Object[] formatArgs;

    @Nullable
    private LanguageMap languageCache;

    private final List<ITextProperties> children = Lists.newArrayList();

    public FallbackI18nTextComponent(String key, String defaultTranslation) {
        this.key = key;
        this.defaultTranslation = defaultTranslation;
        this.formatArgs = NO_ARGS;
    }

    public FallbackI18nTextComponent(String key, String defaultTranslation, Object... formatArgs) {
        this.key = key;
        this.defaultTranslation = defaultTranslation;
        this.formatArgs = formatArgs;
    }

    private void ensureInitialized() {
        LanguageMap languageMap = LanguageMap.getInstance();
        if (languageMap != languageCache) {
            languageCache = languageMap;
            children.clear();

            String translation = languageMap.hasTranslation(key) ? languageMap.get(key) : defaultTranslation;
            try {
                setTranslation(translation);
            } catch (I18nException exc) {
                children.clear();
                children.add(ITextProperties.plain(translation));
            }
        }
    }

    private void setTranslation(String translation) {
        Matcher matcher = FORMAT_FLAG.matcher(translation);

        try {
            int nextArgumentIndex = 0;

            int last;
            int start, end;
            for (last = 0; matcher.find(last); last = end) {
                start = matcher.start();
                end = matcher.end();

                if (start > last) {
                    String sub = translation.substring(last, start);
                    if (sub.indexOf('%') != -1) {
                        throw new IllegalArgumentException();
                    }

                    children.add(ITextProperties.plain(sub));
                }

                String sign = matcher.group(2);
                String match = translation.substring(start, end);
                if ("%".equals(sign) && "%%".equals(match)) {
                    children.add(PERCENT_SIGN);
                } else {
                    if (!"s".equals(sign)) {
                        throw new UnknownFormatFlagsException("Unsupported format: '" + match + "'");
                    }

                    String number = matcher.group(1);
                    int index = number != null ? Integer.parseInt(number) - 1 : nextArgumentIndex++;
                    if (index < formatArgs.length) {
                        children.add(getArg(index));
                    }
                }
            }

            if (last == 0) {
                last = handleForge(translation);
            }
            if (last < translation.length()) {
                String suffix = translation.substring(last);
                if (suffix.indexOf('%') != -1) {
                    throw new IllegalArgumentException();
                }

                children.add(ITextProperties.plain(suffix));
            }

        } catch (IllegalArgumentException illegalargumentexception) {
            throw new I18nException(this, illegalargumentexception);
        }
    }

    public int handleForge(String format) {
        try {
            StringTextComponent component = new StringTextComponent(ForgeI18n.parseFormat(format, formatArgs));
            component.getStyle().withParent(getStyle());
            children.add(component);
            return format.length();
        } catch (IllegalArgumentException ex) {
            return 0;
        }
    }

    private ITextProperties getArg(int index) {
        if (index >= formatArgs.length) {
            throw new I18nException(this, index);
        } else {
            Object arg = formatArgs[index];
            if (arg instanceof ITextComponent) {
                return (ITextComponent) arg;
            } else {
                return arg == null ? NULL : ITextProperties.plain(arg.toString());
            }
        }
    }

    @Override
    public FallbackI18nTextComponent copy() {
        return new FallbackI18nTextComponent(key, defaultTranslation, formatArgs);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T> Optional<T> visitSelf(ITextProperties.IStyledTextAcceptor<T> acceptor, Style style) {
        ensureInitialized();

        for (ITextProperties props : children) {
            Optional<T> res = props.visit(acceptor, style);
            if (res.isPresent()) {
                return res;
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visitSelf(ITextProperties.ITextAcceptor<T> acceptor) {
        ensureInitialized();

        for (ITextProperties props : children) {
            Optional<T> res = props.visit(acceptor);
            if (res.isPresent()) {
                return res;
            }
        }

        return Optional.empty();
    }

    @Override
    public IFormattableTextComponent parse(@Nullable CommandSource src, @Nullable Entity entity, int recursionDepth) throws CommandSyntaxException {
        Object[] newArgs = new Object[formatArgs.length];

        for (int i = 0; i < newArgs.length; ++i) {
            Object arg = formatArgs[i];
            if (arg instanceof ITextComponent) {
                newArgs[i] = TextComponentUtils.parse(src, (ITextComponent) arg, entity, recursionDepth);
            } else {
                newArgs[i] = arg;
            }
        }

        return new FallbackI18nTextComponent(key, defaultTranslation, newArgs);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FallbackI18nTextComponent)) {
            return false;
        } else {
            FallbackI18nTextComponent that = (FallbackI18nTextComponent) o;
            return Arrays.equals(formatArgs, that.formatArgs) && key.equals(that.key) && super.equals(o);
        }
    }

    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + key.hashCode();
        return 31 * hash + Arrays.hashCode(formatArgs);
    }

    public String toString() {
        return "FallbackI18nComponent{" +
                   "key='" + key + "'" + ", " +
                   "args=" + Arrays.toString(formatArgs) + ", " +
                   "siblings=" + siblings + ", " +
                   "style=" + getStyle() + "}";
    }

    public String getKey() {
        return key;
    }

    public Object[] getFormatArgs() {
        return formatArgs;
    }
}
