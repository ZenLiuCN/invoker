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
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Zen.Liu
 * @since 2023-08-24
 */


public class AccessorBenchmarkTest {
    @Test
    public void runBenchmarks() throws Exception {
        var options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(6)
                .threads(1)
                .measurementIterations(6)
                .forks(3)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .build();

        new Runner(options).run();
    }

    public static class Holder {
        int val;
        Integer value;
    }

    @State(Scope.Thread)
    public static class Status {

        int length;
        int[] values;
        Field ref;
        Field refObject;
        Accessor acc;
        Accessor accObject;
        Accessor.IntSetter intSetter;
        Accessor.IntGetter intGetter;
        Accessor.Setter setter;
        Accessor.Getter getter;
        Accessor.Setter setterObject;
        Accessor.Getter getterObject;
        MethodHandle handle;
        MethodHandle handleGet;
        MethodHandle handleObject;
        MethodHandle handleGetObject;
        Holder o;

        @SneakyThrows
        @Setup(Level.Trial)
        public void initialize() {
            length = 100;
            values = new int[100];
            var rnd = new Random();
            for (int i = 0; i < 100; i++) {
                values[i] = rnd.nextInt();
            }

            var lk = MethodHandles.lookup();

            ref = Holder.class.getDeclaredField("val");
            ref.setAccessible(true);
            acc = Accessor.field(lk, ref, Holder.class);
            setter = ((Accessor.Setter) acc.setter);
            getter = ((Accessor.Getter) acc.getter);
            intSetter = ((Accessor.IntSetter) acc.setter);
            intGetter = ((Accessor.IntGetter) acc.getter);
            handle = lk.unreflectSetter(ref);
            handleGet = lk.unreflectGetter(ref);


            refObject = Holder.class.getDeclaredField("value");
            refObject.setAccessible(true);
            accObject = Accessor.field(lk, refObject, Holder.class);
            setterObject = ((Accessor.Setter) accObject.setter);
            getterObject = ((Accessor.Getter) accObject.getter);
            handleObject = lk.unreflectSetter(refObject);
            handleGetObject = lk.unreflectGetter(refObject);

            o = new Holder();
            o.val = rnd.nextInt();
            o.value = rnd.nextInt();

        }
    }

    @SneakyThrows
    @Benchmark
    public void reflect(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.ref;
        for (int i = 0; i < state.length; i++) {
            s.set(o, values[i]);
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void direct(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        for (int i = 0; i < state.length; i++) {
            o.val = values[i];
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void handle(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.handle;
        for (int i = 0; i < state.length; i++) {
            s.invoke(o, values[i]);
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void handleExact(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.handle;
        for (int i = 0; i < state.length; i++) {
            s.invokeExact(o, values[i]);
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void accGeneric(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.acc.setter;
        for (int i = 0; i < state.length; i++) {
            s.set(o, values[i]);
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void accPrimitive(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.intSetter;
        for (int i = 0; i < state.length; i++) {
            s.setValue(o, values[i]);
        }
        hole.consume(o);
    }


    @SneakyThrows
    @Benchmark
    public void reflectObject(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.refObject;
        for (int i = 0; i < state.length; i++) {
            s.set(o, values[i]);
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void directObject(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        for (int i = 0; i < state.length; i++) {
            o.value = values[i];
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void handleObject(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.handleObject;
        for (int i = 0; i < state.length; i++) {
            s.invoke(o, values[i]);
        }
        hole.consume(o);
    }


    @SneakyThrows
    @Benchmark
    public void accGenericObject(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.accObject.setter;
        for (int i = 0; i < state.length; i++) {
            s.set(o, values[i]);
        }
        hole.consume(o);
    }

    @SneakyThrows
    @Benchmark
    public void accSetterGenericObject(Status state, Blackhole hole) {
        var o = new Holder();
        var values = state.values;
        var s = state.setterObject;
        for (int i = 0; i < state.length; i++) {
            s.set(o, values[i]);
        }
        hole.consume(o);
    }


    @SneakyThrows
    @Benchmark
    public void reflectGet(Status state, Blackhole hole) {
        hole.consume(state.ref.get(state.o));
    }

    @SneakyThrows
    @Benchmark
    public void directGet(Status state, Blackhole hole) {
        hole.consume(state.o.val);
    }

    @SneakyThrows
    @Benchmark
    public void handleGet(Status state, Blackhole hole) {
        hole.consume(state.handleGet.invoke(state.o));
    }


    @SneakyThrows
    @Benchmark
    public void accGenericGet(Status state, Blackhole hole) {
        hole.consume(state.acc.getter.get(state.o));
    }

    @SneakyThrows
    @Benchmark
    public void accPrimitiveGet(Status state, Blackhole hole) {
        hole.consume(state.intGetter.get(state.o));
    }


    @SneakyThrows
    @Benchmark
    public void reflectGetObject(Status state, Blackhole hole) {
        hole.consume(state.refObject.get(state.o));
    }

    @SneakyThrows
    @Benchmark
    public void directGetObject(Status state, Blackhole hole) {
        hole.consume(state.o.value);
    }

    @SneakyThrows
    @Benchmark
    public void handleGetObject(Status state, Blackhole hole) {
        hole.consume(state.handleGetObject.invoke(state.o));
    }


    @SneakyThrows
    @Benchmark
    public void accGenericGetObject(Status state, Blackhole hole) {
        hole.consume(state.accObject.getter.get(state.o));
    }

    @SneakyThrows
    @Benchmark
    public void accGetterGenericGetObject(Status state, Blackhole hole) {
        hole.consume(state.getterObject.get(state.o));
    }

}
