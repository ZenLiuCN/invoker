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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InvokerTest {
    public static void main(String[] args) {
        sam();
        build();
    }

    @SneakyThrows
    static void sam() {
        StringBuilder b = new StringBuilder();
        String name = "";
        for (int i = 0; i < 30; i++) {
            //region sx0
            name = String.format("sx0%d.java", i);
            b.append(String.format("package cn.zenliu.java.invoker;\n" +
                                   "import org.jetbrains.annotations.Nullable;\n" +
                                   "public interface sx0%d extends sx0 { @Override\n" +
                                   "    default Object invoke(@Nullable Object instance, Object... args) {\n" +
                                   "        assert instance == null : \"no instance required\";\n" +
                                   "        i(", i));
            for (int j = 0; j < i; j++) {
                if (j > 0) b.append(" , ");
                b.append(String.format("args[%d]", j));
            }
            b.append(");\n" +
                     "        return null;\n" +
                     "    }\n" +
                     "Object i(");
            for (int j = 0; j < i; j++) {
                if (j > 0) b.append(" , ");
                b.append(String.format("Object a%d", j));
            }
            b.append(String.format(");\n" +
                                   "        @Override\n" +
                                   "        default int args() {\n" +
                                   "                return %d;\n" +
                                   "        }\n" +
                                   "    }\n", i));

            Files.write(Paths.get("src/main/java/cn/zenliu/java/invoker/" + name),
                    b.toString().getBytes(StandardCharsets.UTF_8));
            b.setLength(0);
            //endregion

            //region sx1
            name = String.format("sx1%d.java", i);
            b.append(String.format("package cn.zenliu.java.invoker;\n" +
                                   "import org.jetbrains.annotations.Nullable;\n" +
                                   "public interface sx1%d extends sx1 { @Override\n" +
                                   "    default Object invoke(@Nullable Object instance, Object... args) {\n" +
                                   "        assert instance == null : \"no instance required\";\n" +
                                   "        return i(", i));
            for (int j = 0; j < i; j++) {
                if (j > 0) b.append(" , ");
                b.append(String.format("args[%d]", j));
            }
            b.append(");\n" +
                     "    }\n" +
                     "Object i(");
            for (int j = 0; j < i; j++) {
                if (j > 0) b.append(" , ");
                b.append(String.format("Object a%d", j));
            }
            b.append(String.format(");\n" +
                                   "        @Override\n" +
                                   "        default int args() {\n" +
                                   "                return %d;\n" +
                                   "        }\n" +
                                   "    }\n", i));

            Files.write(Paths.get("src/main/java/cn/zenliu/java/invoker/" + name),
                    b.toString().getBytes(StandardCharsets.UTF_8));
            b.setLength(0);
            //endregion

            //region ix0
            name = String.format("ix0%d.java", i);
            b.append(String.format("package cn.zenliu.java.invoker;\n" +
                                   "import java.util.Objects;\n" +
                                   "import org.jetbrains.annotations.Nullable;\n" +
                                   "public interface ix0%d extends ix0 { @Override\n" +
                                   "    default Object invoke(@Nullable Object instance, Object... args) {\n" +
                                   "        i(Objects.requireNonNull(instance, \"instance required\")", i));
            for (int j = 0; j < i; j++) {
                b.append(String.format(" , args[%d]", j));
            }
            b.append(");\n" +
                     "        return null;\n" +
                     "    }\n" +
                     "Object i(Object instance");
            for (int j = 0; j < i; j++) {
                b.append(String.format(" , Object a%d", j));
            }
            b.append(String.format(");\n" +
                                   "        @Override\n" +
                                   "        default int args() {\n" +
                                   "                return %d;\n" +
                                   "        }\n" +
                                   "    }\n", i));

            Files.write(Paths.get("src/main/java/cn/zenliu/java/invoker/" + name),
                    b.toString().getBytes(StandardCharsets.UTF_8));
            b.setLength(0);
            //endregion

            //region ix1
            name = String.format("ix1%d.java", i);
            b.append(String.format("package cn.zenliu.java.invoker;\n" +
                                   "import java.util.Objects;\n" +
                                   "import org.jetbrains.annotations.Nullable;\n" +
                                   "public interface ix1%d extends ix1 { @Override\n" +
                                   "    default Object invoke(@Nullable Object instance, Object... args) {\n" +
                                   "        return i(Objects.requireNonNull(instance, \"instance required\")", i));
            for (int j = 0; j < i; j++) {
                b.append(String.format(" , args[%d]", j));
            }
            b.append(");\n" +
                     "    }\n" +
                     "Object i(Object instance");
            for (int j = 0; j < i; j++) {
                b.append(String.format(" , Object a%d", j));
            }
            b.append(String.format(");\n" +
                                   "        @Override\n" +
                                   "        default int args() {\n" +
                                   "                return %d;\n" +
                                   "        }\n" +
                                   "    }\n", i));

            Files.write(Paths.get("src/main/java/cn/zenliu/java/invoker/" + name),
                    b.toString().getBytes(StandardCharsets.UTF_8));
            b.setLength(0);
            //endregion
        }

    }

    static void build() {
        StringBuilder b = new StringBuilder("@SneakyThrows static Invoker build(MethodHandles.Lookup lookup, MethodHandle handle, " +
                                            "boolean isStatic, boolean isVarArgs, boolean hasReturn, int argumentCounts) {\n" +
                                            "    MethodType sam=handle.type().wrap().generic();\n" +
                                            "    MethodType src=handle.type().wrap();   " +
                                            "    if (isVarArgs && argumentCounts == 1) {\n" +
                                            "                if (isStatic && hasReturn)\n" +
                                            "                    return (Invoker) (sv11) LambdaMetafactory.metafactory(\n" +
                                            "                            lookup,\n" +
                                            "                            \"i\",\n" +
                                            "                            MethodType.methodType(sv11.class),\n" +
                                            "                            sam," +
                                            "                            handle,\n" +
                                            "                            src).getTarget().invokeExact();\n" +
                                            "                else if (isStatic)\n" +
                                            "                    return (Invoker) (sv01) LambdaMetafactory.metafactory(\n" +
                                            "                            lookup,\n" +
                                            "                            \"i\",\n" +
                                            "                            MethodType.methodType(sv01.class),\n" +
                                            "                            sam," +
                                            "                            handle,\n" +
                                            "                            src).getTarget().invokeExact();\n" +
                                            "                else if (hasReturn)\n" +
                                            "                    return (Invoker) (iv11) LambdaMetafactory.metafactory(\n" +
                                            "                            lookup,\n" +
                                            "                            \"i\",\n" +
                                            "                            MethodType.methodType(iv11.class),\n" +
                                            "                            sam," +
                                            "                            handle,\n" +
                                            "                            src).getTarget().invokeExact();\n" +
                                            "                else return (Invoker) (iv01) LambdaMetafactory.metafactory(\n" +
                                            "                            $.lookup,\n" +
                                            "                            \"i\",\n" +
                                            "                            MethodType.methodType(iv01.class),\n" +
                                            "                            sam," +
                                            "                            handle,\n" +
                                            "                            src).getTarget().invokeExact();\n" +
                                            "            }" +
                                            "      switch ( argumentCounts ) {\n"
        );
        for (int i = 0; i < 30; i++) {
            b.append(String.format("        case %1$d : {\n" +
                                   "                    if (isStatic && hasReturn)\n" +
                                   "                        return (Invoker) (sx1%1$d) LambdaMetafactory.metafactory(\n" +
                                   "                                lookup,\n" +
                                   "                                \"i\",\n" +
                                   "                                MethodType.methodType(sx1%1$d.class),\n" +
                                   "                                sam," +
                                   "                                handle,\n" +
                                   "                                src).getTarget().invokeExact();\n" +
                                   "                    else if (isStatic)\n" +
                                   "                        return (Invoker) (sx0%1$d) LambdaMetafactory.metafactory(\n" +
                                   "                                lookup,\n" +
                                   "                                \"i\",\n" +
                                   "                                MethodType.methodType(sx0%1$d.class),\n" +
                                   "                                sam," +
                                   "                                handle,\n" +
                                   "                                src).getTarget().invokeExact();\n" +
                                   "                    else if (hasReturn)\n" +
                                   "                        return (Invoker) (ix1%1$d) LambdaMetafactory.metafactory(\n" +
                                   "                                lookup,\n" +
                                   "                                \"i\",\n" +
                                   "                                MethodType.methodType(ix1%1$d.class),\n" +
                                   "                                sam," +
                                   "                                handle,\n" +
                                   "                                src).getTarget().invokeExact();\n" +
                                   "                    else return (Invoker) (ix0%1$d) LambdaMetafactory.metafactory(\n" +
                                   "                                lookup,\n" +
                                   "                                \"i\",\n" +
                                   "                                MethodType.methodType(ix0%1$d.class),\n" +
                                   "                                sam," +
                                   "                                handle,\n" +
                                   "                                src).getTarget().invokeExact();\n" +
                                   "                }\n", i));
        }
        b.append("      default :\n" +
                 "          throw new IllegalStateException(\"not support arguments more that 30\");" +
                 "  }");
        System.out.println(b);
    }


    @Test
    @SneakyThrows
    void testInvokerMethod() {
        var lookup = MethodHandles.lookup();
        var m = String.class.getMethod("toString");
        var i = Invoker.make(lookup, m);
        assertTrue(i.hasReturns());
        assertEquals(0, i.args());
        assertFalse(i.isStatic());
        assertEquals("123", i.invoke("123"));

        m = Map.class.getMethod("put", Object.class, Object.class);
        i = Invoker.make(lookup, m);
        assertTrue(i.hasReturns());
        assertEquals(2, i.args());
        assertFalse(i.isStatic());
        var is = new HashMap<>();
        assertNull(i.invoke(is, 1, 2));
        assertEquals(2, is.get(1));
    }

    @Test
    @SneakyThrows
    void testInvokerCtor() {
        var lookup = MethodHandles.lookup();
        var m = String.class.getConstructor(byte[].class);
        var i = Invoker.make(lookup, m);
        assertTrue(i.hasReturns());
        assertEquals(1, i.args());
        assertTrue(i.isStatic());
        assertEquals("123", i.invoke(null, (Object) "123".getBytes(StandardCharsets.UTF_8)));
    }

    public static class SomePojo {
        public final int value;
        public int value1;

        public SomePojo(int value) {
            this.value = value;
        }
    }

    @Test
    @SneakyThrows
    void testInvokerField() {
        var lookup = MethodHandles.lookup();
        var m = SomePojo.class.getDeclaredField("value");
        var i = Invoker.makeGetter(lookup, m);
        assertTrue(i.hasReturns());
        assertEquals(0, i.args());
        assertFalse(i.isStatic());
        assertEquals(1, i.invoke(new SomePojo(1)));

        m = SomePojo.class.getDeclaredField("value1");
        i = Invoker.makeSetter(lookup, m);
        assertFalse(i.hasReturns());
        assertEquals(1, i.args());
        assertFalse(i.isStatic());
        var p = new SomePojo(1);
        p.value1 = 2;
        assertNull(i.invoke(p, 3));
        assertEquals(3, p.value1);
    }
}