/*
 * Source of invoker
 * Copyright (C) 2023.  Zen.Liu
 *
 * SPDX-License-Identifier: GPL-2.0-only WITH Classpath-exception-2.0"
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; version 2.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Class Path Exception
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *  As a special exception, the copyright holders of this library give you permission to link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this library, you may extend this exception to your version of the library, but you are not obligated to do so. If you do not wish to do so, delete this exception statement from your version.
 */

package cn.zenliu.java.invoker;

import lombok.SneakyThrows;
import lombok.var;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Zen.Liu
 * @since 2023-08-24
 */
public final class Accessor {
    public final GenericGetter getter;
    public final GenericSetter setter;
    public final Class<?> type;

    public boolean isPrimitives() {
        return !(getter instanceof Getter.Impl) && !(getter instanceof StaticGetter.Impl);
    }

    public boolean canSet() {
        return setter != null;
    }

    private Accessor(GenericGetter getter, GenericSetter setter, Class<?> type) {
        this.getter = getter;
        this.setter = setter;
        this.type = type;
    }


    interface GenericSetter {
        void set(@Nullable Object instance, Object value);
    }

    interface GenericGetter {
        Object get(@Nullable Object instance);
    }

    public interface ValueType {
        Class<?> valueType();
    }

    public interface Getter extends ValueType, GenericGetter {
        Object get(Object instance);

        final class Impl implements Getter {
            private final MethodHandle handle;
            private final Class<?> type;

            public Impl(MethodHandle handle, Class<?> type) {
                this.handle = handle;
                this.type = type;
            }

            @Override
            public Class<?> valueType() {
                return type;
            }

            @SneakyThrows
            @Override
            public Object get(Object instance) {
                return handle.invoke(instance);
            }
        }

    }

    public interface Setter extends ValueType, GenericSetter {
        void set(Object instance, Object value);

        final class Impl implements Setter {
            private final MethodHandle handle;
            private final Class<?> type;

            public Impl(MethodHandle handle, Class<?> type) {
                this.handle = handle;
                this.type = type;
            }

            @Override
            public Class<?> valueType() {
                return type;
            }

            @SneakyThrows
            @Override
            public void set(Object instance, Object v) {
                handle.invoke(instance, v);
            }
        }
    }

    public interface StaticGetter extends ValueType, GenericGetter {
        @Override
        default Object get(@Nullable Object instance) {
            assert instance == null : "no instance is expected";
            return get();
        }

        Object get();

        final class Impl implements StaticGetter {
            private final MethodHandle handle;
            private final Class<?> type;

            public Impl(MethodHandle handle, Class<?> type) {
                this.handle = handle;
                this.type = type;
            }

            @Override
            public Class<?> valueType() {
                return type;
            }

            @SneakyThrows
            @Override
            public Object get() {
                return handle.invoke();
            }
        }

    }

    public interface StaticSetter extends ValueType, GenericSetter {
        @Override
        default void set(@Nullable Object instance, Object value) {
            assert instance == null : "no instance is expected";
            set(value);
        }

        void set(Object v);

        final class Impl implements StaticSetter {
            private final MethodHandle handle;
            private final Class<?> type;

            public Impl(MethodHandle handle, Class<?> type) {
                this.handle = handle;
                this.type = type;
            }

            @Override
            public Class<?> valueType() {
                return type;
            }

            @SneakyThrows
            @Override
            public void set(Object v) {
                handle.invoke(v);
            }
        }
    }

    //region Generated interfaces
    public interface BooleanValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return boolean.class;
        }
    }

    public interface BooleanGetter extends Getter, BooleanValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        boolean getValue(Object instance);
    }

    public interface BooleanSetter extends Setter, BooleanValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Boolean : "only allow boolean value";
            setValue(instance, (boolean) value);
        }

        void setValue(Object instance, boolean value);

    }

    public interface BooleanStaticGetter extends StaticGetter, BooleanValueType {
        @Override
        default Object get() {
            return getValue();
        }

        boolean getValue();


    }

    public interface BooleanStaticSetter extends StaticSetter, BooleanValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Boolean : "only allow boolean value";
            setValue((boolean) value);
        }

        void setValue(boolean value);

    }

    public interface ByteValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return byte.class;
        }
    }

    public interface ByteGetter extends Getter, ByteValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        byte getValue(Object instance);
    }

    public interface ByteSetter extends Setter, ByteValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Byte : "only allow byte value";
            setValue(instance, (byte) value);
        }

        void setValue(Object instance, byte value);

    }

    public interface ByteStaticGetter extends StaticGetter, ByteValueType {
        @Override
        default Object get() {
            return getValue();
        }

        byte getValue();


    }

    public interface ByteStaticSetter extends StaticSetter, ByteValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Byte : "only allow byte value";
            setValue((byte) value);
        }

        void setValue(byte value);

    }

    public interface ShortValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return short.class;
        }
    }

    public interface ShortGetter extends Getter, ShortValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        short getValue(Object instance);
    }

    public interface ShortSetter extends Setter, ShortValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Short : "only allow short value";
            setValue(instance, (short) value);
        }

        void setValue(Object instance, short value);

    }

    public interface ShortStaticGetter extends StaticGetter, ShortValueType {
        @Override
        default Object get() {
            return getValue();
        }

        short getValue();


    }

    public interface ShortStaticSetter extends StaticSetter, ShortValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Short : "only allow short value";
            setValue((short) value);
        }

        void setValue(short value);

    }

    public interface CharValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return char.class;
        }
    }

    public interface CharGetter extends Getter, CharValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        char getValue(Object instance);
    }

    public interface CharSetter extends Setter, CharValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Character : "only allow char value";
            setValue(instance, (char) value);
        }

        void setValue(Object instance, char value);

    }

    public interface CharStaticGetter extends StaticGetter, CharValueType {
        @Override
        default Object get() {
            return getValue();
        }

        char getValue();


    }

    public interface CharStaticSetter extends StaticSetter, CharValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Character : "only allow char value";
            setValue((char) value);
        }

        void setValue(char value);

    }

    public interface IntValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return int.class;
        }
    }

    public interface IntGetter extends Getter, IntValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        int getValue(Object instance);
    }

    public interface IntSetter extends Setter, IntValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Integer : "only allow int value";
            setValue(instance, (int) value);
        }

        void setValue(Object instance, int value);

    }

    public interface IntStaticGetter extends StaticGetter, IntValueType {
        @Override
        default Object get() {
            return getValue();
        }

        int getValue();


    }

    public interface IntStaticSetter extends StaticSetter, IntValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Integer : "only allow int value";
            setValue((int) value);
        }

        void setValue(int value);

    }

    public interface LongValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return long.class;
        }
    }

    public interface LongGetter extends Getter, LongValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        long getValue(Object instance);
    }

    public interface LongSetter extends Setter, LongValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Long : "only allow long value";
            setValue(instance, (long) value);
        }

        void setValue(Object instance, long value);

    }

    public interface LongStaticGetter extends StaticGetter, LongValueType {
        @Override
        default Object get() {
            return getValue();
        }

        long getValue();


    }

    public interface LongStaticSetter extends StaticSetter, LongValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Long : "only allow long value";
            setValue((long) value);
        }

        void setValue(long value);

    }

    public interface FloatValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return float.class;
        }
    }

    public interface FloatGetter extends Getter, FloatValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        float getValue(Object instance);
    }

    public interface FloatSetter extends Setter, FloatValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Float : "only allow float value";
            setValue(instance, (float) value);
        }

        void setValue(Object instance, float value);

    }

    public interface FloatStaticGetter extends StaticGetter, FloatValueType {
        @Override
        default Object get() {
            return getValue();
        }

        float getValue();


    }

    public interface FloatStaticSetter extends StaticSetter, FloatValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Float : "only allow float value";
            setValue((float) value);
        }

        void setValue(float value);

    }

    public interface DoubleValueType extends ValueType {

        @Override
        default Class<?> valueType() {
            return double.class;
        }
    }

    public interface DoubleGetter extends Getter, DoubleValueType {
        @Override
        default Object get(Object instance) {
            assert instance != null : "expect instance";
            return getValue(instance);
        }

        double getValue(Object instance);
    }

    public interface DoubleSetter extends Setter, DoubleValueType {
        @Override
        default void set(Object instance, Object value) {
            assert instance != null : "expect instance";
            assert value instanceof Double : "only allow double value";
            setValue(instance, (double) value);
        }

        void setValue(Object instance, double value);

    }

    public interface DoubleStaticGetter extends StaticGetter, DoubleValueType {
        @Override
        default Object get() {
            return getValue();
        }

        double getValue();


    }

    public interface DoubleStaticSetter extends StaticSetter, DoubleValueType {
        @Override
        default void set(Object value) {
            assert value instanceof Double : "only allow double value";
            setValue((double) value);
        }

        void setValue(double value);

    }

    //endregion
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    static <E extends Throwable> void sneak(Throwable t) throws E {
        throw (E) t;
    }

    /**
     * Generate from one Field
     *
     * @param lookup the lookup
     * @param field  field to generate
     * @param holder optional holder class , null will use {@link Field#getDeclaringClass()}
     * @return Accessor
     */
    @ApiStatus.AvailableSince("0.0.2")
    @SneakyThrows
    public static Accessor field(MethodHandles.Lookup lookup, Field field, @Nullable Class<?> holder) {
        field.setAccessible(true);
        var isStatic = Modifier.isStatic(field.getModifiers());
        var type = field.getType();
        var isFinal = Modifier.isFinal(field.getModifiers());
        holder = holder == null ? field.getDeclaringClass() : holder;
        if (isStatic && isFinal) {
            var g = lookup.unreflectGetter(field);
            if (boolean.class.isAssignableFrom(type)) {
                return new Accessor(
                        (BooleanStaticGetter) () -> {
                            try {
                                return (boolean) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (byte.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ByteStaticGetter) () -> {
                            try {
                                return (byte) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (short.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ShortStaticGetter) () -> {
                            try {
                                return (short) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (char.class.isAssignableFrom(type)) {
                return new Accessor(
                        (CharStaticGetter) () -> {
                            try {
                                return (char) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (int.class.isAssignableFrom(type)) {
                return new Accessor(
                        (IntStaticGetter) () -> {
                            try {
                                return (int) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (long.class.isAssignableFrom(type)) {
                return new Accessor(
                        (LongStaticGetter) () -> {
                            try {
                                return (long) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (float.class.isAssignableFrom(type)) {
                return new Accessor(
                        (FloatStaticGetter) () -> {
                            try {
                                return (float) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (double.class.isAssignableFrom(type)) {
                return new Accessor(
                        (DoubleStaticGetter) () -> {
                            try {
                                return (double) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            } else {
                return new Accessor(
                        new StaticGetter.Impl(g, type),
                        null,
                        holder
                );
            }
        } else if (isStatic) {
            var g = lookup.unreflectGetter(field);
            var s = lookup.unreflectSetter(field);
            if (boolean.class.isAssignableFrom(type)) {
                return new Accessor(
                        (BooleanStaticGetter) () -> {
                            try {
                                return (boolean) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (BooleanStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (byte.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ByteStaticGetter) () -> {
                            try {
                                return (byte) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (ByteStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (short.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ShortStaticGetter) () -> {
                            try {
                                return (short) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (ShortStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (char.class.isAssignableFrom(type)) {
                return new Accessor(
                        (CharStaticGetter) () -> {
                            try {
                                return (char) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (CharStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (int.class.isAssignableFrom(type)) {
                return new Accessor(
                        (IntStaticGetter) () -> {
                            try {
                                return (int) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (IntStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (long.class.isAssignableFrom(type)) {
                return new Accessor(
                        (LongStaticGetter) () -> {
                            try {
                                return (long) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (LongStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (float.class.isAssignableFrom(type)) {
                return new Accessor(
                        (FloatStaticGetter) () -> {
                            try {
                                return (float) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (FloatStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (double.class.isAssignableFrom(type)) {
                return new Accessor(
                        (DoubleStaticGetter) () -> {
                            try {
                                return (double) g.invoke();
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (DoubleStaticSetter) (v) -> {
                            try {
                                s.invoke(v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            } else {
                return new Accessor(
                        new StaticGetter.Impl(g, type),
                        new StaticSetter.Impl(s, type),
                        holder
                );
            }
        } else if (isFinal) {
            var g = lookup.unreflectGetter(field);
            if (boolean.class.isAssignableFrom(type)) {
                return new Accessor(
                        (BooleanGetter) (o) -> {
                            try {
                                return (boolean) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (byte.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ByteGetter) (o) -> {
                            try {
                                return (byte) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (short.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ShortGetter) (o) -> {
                            try {
                                return (short) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (char.class.isAssignableFrom(type)) {
                return new Accessor(
                        (CharGetter) (o) -> {
                            try {
                                return (char) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (int.class.isAssignableFrom(type)) {
                return new Accessor(
                        (IntGetter) (o) -> {
                            try {
                                return (int) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (long.class.isAssignableFrom(type)) {
                return new Accessor(
                        (LongGetter) (o) -> {
                            try {
                                return (long) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (float.class.isAssignableFrom(type)) {
                return new Accessor(
                        (FloatGetter) (o) -> {
                            try {
                                return (float) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            }
            if (double.class.isAssignableFrom(type)) {
                return new Accessor(
                        (DoubleGetter) (o) -> {
                            try {
                                return (double) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        null,
                        holder
                );
            } else {
                return new Accessor(
                        new Getter.Impl(g, type),
                        null,
                        holder
                );
            }
        } else {
            var g = lookup.unreflectGetter(field);
            var s = lookup.unreflectSetter(field);
            if (boolean.class.isAssignableFrom(type)) {
                return new Accessor(
                        (BooleanGetter) (o) -> {
                            try {
                                return (boolean) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (BooleanSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (byte.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ByteGetter) (o) -> {
                            try {
                                return (byte) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (ByteSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (short.class.isAssignableFrom(type)) {
                return new Accessor(
                        (ShortGetter) (o) -> {
                            try {
                                return (short) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (ShortSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (char.class.isAssignableFrom(type)) {
                return new Accessor(
                        (CharGetter) (o) -> {
                            try {
                                return (char) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (CharSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (int.class.isAssignableFrom(type)) {
                return new Accessor(
                        (IntGetter) (o) -> {
                            try {
                                return (int) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (IntSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (long.class.isAssignableFrom(type)) {
                return new Accessor(
                        (LongGetter) (o) -> {
                            try {
                                return (long) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (LongSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (float.class.isAssignableFrom(type)) {
                return new Accessor(
                        (FloatGetter) (o) -> {
                            try {
                                return (float) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (FloatSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            }
            if (double.class.isAssignableFrom(type)) {
                return new Accessor(
                        (DoubleGetter) (o) -> {
                            try {
                                return (double) g.invoke(o);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        (DoubleSetter) (o, v) -> {
                            try {
                                s.invoke(o, v);
                            } catch (Throwable e) {
                                sneak(e);
                                throw new IllegalStateException();
                            }
                        },
                        holder
                );
            } else {
                return new Accessor(
                        new Getter.Impl(g, type),
                        new Setter.Impl(s, type),
                        holder
                );
            }
        }
    }

    /**
     * Generate accessors of all class fields.
     *
     * @param lookup lookup
     * @param holder holder class
     * @param filter optional field filter
     * @return accessors mapped with field name
     */
    @ApiStatus.AvailableSince("0.0.2")
    public static @NotNull @Unmodifiable Map<String, Accessor> fields(MethodHandles.Lookup lookup, Class<?> holder, @Nullable Predicate<Field> filter) {
        if (filter != null) {
            var m = new HashMap<String, Accessor>();
            for (var f : holder.getDeclaredFields()) {
                if (filter.test(f)) {
                    m.put(f.getName(), field(lookup, f, holder));
                }
            }
            return Collections.unmodifiableMap(m);
        } else {
            var m = new HashMap<String, Accessor>();
            for (var f : holder.getDeclaredFields()) {
                m.put(f.getName(), field(lookup, f, holder));
            }
            return Collections.unmodifiableMap(m);
        }
    }
}
