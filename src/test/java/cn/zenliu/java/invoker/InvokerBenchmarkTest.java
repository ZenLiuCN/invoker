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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Zen.Liu
 * @since 2023-08-24
 */


public class InvokerBenchmarkTest {
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

        public int compute(int in) {
            val += in;
            return val;
        }

        public Integer compute(Integer in) {
            value = (value == null ? 0 : val) + in;
            return value;
        }
    }

    @State(Scope.Thread)
    public static class Status {

        int length;
        int[] values;
        Holder o;
        Method pm;
        Method bm;
        Invoker ip;
        Invoker ib;
        Invoker.ix11 iip;
        Invoker.ix11 iib;

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

            pm = Holder.class.getDeclaredMethod("compute", int.class);
            pm.setAccessible(true);
            bm = Holder.class.getDeclaredMethod("compute", Integer.class);
            bm.setAccessible(true);

            ip = Invoker.make(lk, pm);
            ib = Invoker.make(lk, bm);
            iip = ((Invoker.ix11) ip);
            iib = ((Invoker.ix11) ib);
            o = new Holder();
            o.val = rnd.nextInt();
            o.value = rnd.nextInt();

        }
    }

    @Benchmark
    public void direct(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.o.compute(status.values[i]));
        }
    }

    @Benchmark
    public void directObject(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.o.compute((Integer) status.values[i]));
        }
    }

    @SneakyThrows
    @Benchmark
    public void reflect(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.pm.invoke(status.o, status.values[i]));
        }
    }

    @SneakyThrows
    @Benchmark
    public void reflectObject(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.bm.invoke(status.o, status.values[i]));
        }
    }

    @Benchmark
    public void invoker(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.ip.invoke(status.o, status.values[i]));
        }
    }

    @Benchmark
    public void invokerObject(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.ip.invoke(status.o, status.values[i]));
        }
    }

    @Benchmark
    public void invokerUnder(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.iip.i(status.o, status.values[i]));
        }
    }

    @Benchmark
    public void invokerUnderObject(Status status, Blackhole hole) {
        for (int i = 0; i < status.length; i++) {
            hole.consume(status.iib.i(status.o, status.values[i]));
        }
    }
}
