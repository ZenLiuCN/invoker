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
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AccessorTest {
    static List<Map.Entry<Class<?>, Class<?>>> p = Stream.of(
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(boolean.class, Boolean.class),
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(byte.class, Byte.class),
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(short.class, Short.class),
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(char.class, Character.class),
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(int.class, Integer.class),
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(long.class, Long.class),
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(float.class, Float.class),
            new AbstractMap.SimpleEntry<Class<?>, Class<?>>(double.class, Double.class)
    ).collect(Collectors.toList());
    static Function<String, String> cap = s -> Character.toUpperCase(s.charAt(0)) + s.substring(1);

    public static void main(String[] args) {
        genFaces();
    }

    static void genFaces() {
        var b = new StringBuilder();
        for (var e : p) {
            var type = e.getKey();
            var box = e.getValue();
            b.append(String.format("\n" +
                                   "public interface %1$sValueType extends ValueType{\n" +
                                   "\n" +
                                   "    @Override\n" +
                                   "    default Class<?> valueType(){\n" +
                                   "        return %2$s.class;\n" +
                                   "    }\n" +
                                   "}\n" +
                                   "public interface %1$sGetter extends Getter,%1$sValueType{\n" +
                                   "    @Override\n" +
                                   "    default" +
                                   " Object get(Object instance) {\n" +
                                   "        assert instance != null : \"expect instance\";\n" +
                                   "        return getValue(instance);\n" +
                                   "    }\n" +
                                   "    %2$s getValue(Object instance);\n" +
                                   "}\n" +
                                   "public interface %1$sSetter extends Setter ,%1$sValueType{\n" +
                                   "    @Override\n" +
                                   "    default void set(Object instance, Object value){\n" +
                                   "        assert instance != null : \"expect instance\";\n" +
                                   "        assert value instanceof %3$s : \"only allow %2$s value\";\n" +
                                   "        setValue(instance,(%2$s)value);\n" +
                                   "    }\n" +
                                   "    void setValue(Object instance,%2$s value);\n" +
                                   "\n" +
                                   "}\n" +
                                   "public interface %1$sStaticGetter extends StaticGetter ,%1$sValueType{\n" +
                                   "    @Override\n" +
                                   "    default Object get() {\n" +
                                   "        return getValue();\n" +
                                   "    }\n" +
                                   "    %2$s getValue();\n" +
                                   "\n" +
                                   "\n" +
                                   "}\n" +
                                   "public interface %1$sStaticSetter extends StaticSetter ,%1$sValueType{\n" +
                                   "    @Override\n" +
                                   "    default void set(Object value){\n" +
                                   "        assert value instanceof %3$s : \"only allow %2$s value\";\n" +
                                   "        setValue((%2$s)value);\n" +
                                   "    }\n" +
                                   "    void setValue(%2$s value);\n" +
                                   " " +
                                   "\n" +
                                   "}", cap.apply(type.getSimpleName()), type.getSimpleName(), box.getSimpleName()));
        }

        System.out.println(b);
    }

    static void genBuild() {
        var b = new StringBuilder();
        b.append(" public static Accessor build(MethodHandles.Lookup lookup, Field field, Class<?> holder) {\n" +
                 "        var isStatic = Modifier.isStatic(field.getModifiers());\n" +
                 "        var type = field.getType();\n" +
                 "        var isFinal = Modifier.isFinal(field.getModifiers());\n" +
                 "        holder = holder == null ? field.getDeclaringClass() : holder;\n" +
                 "        if (isStatic && isFinal) {\n" +
                 "            var g = lookup.unreflectGetter(field);\n"
        );
        for (var e : p) {
            var type = e.getKey();
            var box = e.getValue();
            b.append(String.format("            if (%1$s.class.isAssignableFrom(type)) {\n" +
                                   "                return new Accessor(\n" +
                                   "                        (%2$sStaticGetter) () -> {\n" +
                                   "                            try {\n" +
                                   "                                return (%1$s) g.invoke();\n" +
                                   "                            } catch (Throwable e) {\n" +
                                   "                                sneak(e);\n" +
                                   "                                throw new IllegalStateException();\n" +
                                   "                            }\n" +
                                   "                        },\n" +
                                   "                        null,\n" +
                                   "                        holder\n" +
                                   "                );\n" +
                                   "            }\n", type.getSimpleName(), cap.apply(type.getSimpleName())));
        }
        b.append("            else {\n" +
                 "                return new Accessor(\n" +
                 "                       new StaticGetter.Impl(g,type),\n" +
                 "                        null,\n" +
                 "                        holder\n" +
                 "                );\n" +
                 "            }\n" +
                 "      }\n" +
                 "else if( isStatic ){\n" +
                 "    var g = lookup.unreflectGetter(field);\n" +
                 "    var s = lookup.unreflectSetter(field);\n");
        for (var e : p) {
            var type = e.getKey();
            b.append(String.format("            if (%1$s.class.isAssignableFrom(type)) {\n" +
                                   "                return new Accessor(\n" +
                                   "                        (%2$sStaticGetter) () -> {\n" +
                                   "                            try {\n" +
                                   "                                return (%1$s) g.invoke();\n" +
                                   "                            } catch (Throwable e) {\n" +
                                   "                                sneak(e);\n" +
                                   "                                throw new IllegalStateException();\n" +
                                   "                            }\n" +
                                   "                        },\n" +
                                   "                        (%2$sStaticSetter) (v) -> {\n" +
                                   "                            try {\n" +
                                   "                                  s.invoke(v);\n" +
                                   "                            } catch (Throwable e) {\n" +
                                   "                                sneak(e);\n" +
                                   "                                throw new IllegalStateException();\n" +
                                   "                            }\n" +
                                   "                        },\n" +
                                   "                        holder\n" +
                                   "                );\n" +
                                   "            }\n", type.getSimpleName(), cap.apply(type.getSimpleName())));
        }
        b.append("            else {\n" +
                 "                return new Accessor(\n" +
                 "                       new StaticGetter.Impl(g,type),\n" +
                 "                       new StaticSetter.Impl(s,type),\n" +
                 "                        holder\n" +
                 "                );\n" +
                 "            }\n" +
                 "        }\n" +
                 "    else  if( isFinal ){\n" +
                 "          var g = lookup.unreflectGetter(field);\n");
        for (var e : p) {
            var type = e.getKey();
            b.append(String.format("            if (%1$s.class.isAssignableFrom(type)) {\n" +
                                   "                return new Accessor(\n" +
                                   "                        (%2$sGetter) (o) -> {\n" +
                                   "                            try {\n" +
                                   "                                return (%1$s) g.invoke(o);\n" +
                                   "                            } catch (Throwable e) {\n" +
                                   "                                sneak(e);\n" +
                                   "                                throw new IllegalStateException();\n" +
                                   "                            }\n" +
                                   "                        },\n" +
                                   "                        null,\n" +
                                   "                        holder\n" +
                                   "                );\n" +
                                   "            }\n", type.getSimpleName(), cap.apply(type.getSimpleName())));
        }
        b.append("            else {\n" +
                 "                return new Accessor(\n" +
                 "                       new Getter.Impl(g,type),\n" +
                 "                       null,\n" +
                 "                        holder\n" +
                 "                );\n" +
                 "            }\n" +
                 "      }\n" +
                 "      else {\n" +
                 "      var g = lookup.unreflectGetter(field);\n" +
                 "      var s = lookup.unreflectSetter(field);\n");
        for (var e : p) {
            var type = e.getKey();
            b.append(String.format("            if (%1$s.class.isAssignableFrom(type)) {\n" +
                                   "                return new Accessor(\n" +
                                   "                        (%2$sGetter) (o) -> {\n" +
                                   "                            try {\n" +
                                   "                                return (%1$s) g.invoke(o);\n" +
                                   "                            } catch (Throwable e) {\n" +
                                   "                                sneak(e);\n" +
                                   "                                throw new IllegalStateException();\n" +
                                   "                            }\n" +
                                   "                        },\n" +
                                   "                        (%2$sSetter) (o,v) -> {\n" +
                                   "                            try {\n" +
                                   "                                  s.invoke(o,v);\n" +
                                   "                            } catch (Throwable e) {\n" +
                                   "                                sneak(e);\n" +
                                   "                                throw new IllegalStateException();\n" +
                                   "                            }\n" +
                                   "                        },\n" +
                                   "                        holder\n" +
                                   "                );\n" +
                                   "            }\n", type.getSimpleName(), cap.apply(type.getSimpleName())));

        }
        b.append("            else {\n" +
                 "                return new Accessor(\n" +
                 "                       new Getter.Impl(g,type),\n" +
                 "                       new Setter.Impl(s,type),\n" +
                 "                        holder\n" +
                 "                );\n" +
                 "            }\n" +
                 "    }\n" +
                 "}\n"
        );
        System.out.println(b);
    }

    static class Some {
        private static final int fsval = 12345;
        private final int fval;
        private Integer val;

        Some(int fval) {
            this.fval = fval;
        }
    }

    @SneakyThrows
    @Test
    void field() {
        var acc = Accessor.field(MethodHandles.lookup(), Some.class.getDeclaredField("fval"), Some.class);
        assertFalse(acc.canSet());
        assertTrue(acc.isPrimitives());
        var i = new Some(12);
        assertEquals(12, acc.getter.get(i));
        assertTrue(acc.getter instanceof Accessor.IntGetter);
        //static
        acc = Accessor.field(MethodHandles.lookup(), Some.class.getDeclaredField("fsval"), Some.class);
        assertFalse(acc.canSet());
        assertTrue(acc.isPrimitives());
        assertEquals(12345, acc.getter.get(null));

        acc = Accessor.field(MethodHandles.lookup(), Some.class.getDeclaredField("val"), Some.class);
        assertTrue(acc.canSet());
        assertFalse(acc.isPrimitives());
        var finalAcc = acc;
        assertDoesNotThrow(() -> finalAcc.setter.set(i, 123));
        assertEquals(123, acc.getter.get(i));
    }

    @Test
    void fields() {
        var accessorMap = Accessor.fields(MethodHandles.lookup(), Some.class, null);
        assertEquals(new HashSet<>(Arrays.asList("val", "fval", "fsval")), accessorMap.keySet());
        var acc = accessorMap.get("fval");
        assertFalse(acc.canSet());
        assertTrue(acc.isPrimitives());
        var i = new Some(12);
        assertEquals(12, acc.getter.get(i));
        assertTrue(acc.getter instanceof Accessor.IntGetter);
        //static
        acc = accessorMap.get("fsval");
        ;
        assertFalse(acc.canSet());
        assertTrue(acc.isPrimitives());
        assertEquals(12345, acc.getter.get(null));

        acc = accessorMap.get("val");
        assertTrue(acc.canSet());
        assertFalse(acc.isPrimitives());
        var finalAcc = acc;
        assertDoesNotThrow(() -> finalAcc.setter.set(i, 123));
        assertEquals(123, acc.getter.get(i));
    }


    @Test
    void fieldsOfJvm() {
        var accessorMap = Accessor.fields(MethodHandles.lookup(), String.class, null);
        System.out.println(accessorMap);
    }
}