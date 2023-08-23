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
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.*;

/**
 * Invoker is the generic interface to build lambda for MethodHandle.<br/>
 *
 * @author Zen.Liu
 * @since 2023-08-23
 */
@SuppressWarnings("unused")
public interface Invoker {


    /**
     * @param instance the object instance
     * @param args     args of function
     * @return null if build from a function without return value.
     */
    Object invoke(@Nullable Object instance, Object... args);

    /**
     * @return does have return value
     */
    boolean hasReturns();

    /**
     * Does accept a pure varargs array as parameter, if true, only first arg as array should be passed.
     */
    boolean isOnlyVarArgs();

    /**
     * Does require a instance, for constructor, static field, static method, the instance does not require.
     */
    boolean isStatic();

    /**
     * How many args required, except the instance.(for varargs always 1)
     */
    int args();

    /**
     * Convert invoker to  Function accept objects
     */
    default Function<Object[], Object> asFunction() {
        assert hasReturns() && isStatic() && !isOnlyVarArgs() : "Contract 'isReturns() && isStatic() && !isOnlyVarArgs()' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert invoker to Function accept object
     */
    default Function<Object, Object> asStaticFunction() {
        assert hasReturns() && isStatic() && args() == 1 : "Contract 'isReturns() && isStatic() && args() == 1' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert to Function accept instance object
     */
    default Function<Object, Object> asInstanceSupplier() {
        assert hasReturns() && !isStatic() && args() == 0 : "Contract 'isReturns() && !isStatic() && args() == 0' not match";
        return this::invoke;
    }

    /**
     * Convert to a BiFunction accept instance and arguments
     */
    default BiFunction<Object, Object[], Object> asBiFunction() {
        assert hasReturns() && !isStatic() : "not match contract";
        return this::invoke;
    }

    /**
     * Convert to an Objects Consumer
     */
    default Consumer<Object[]> asConsumer() {
        assert !hasReturns() && isStatic() : "Contract: '!isReturns() && isStatic()' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert to an Object Consumer
     */
    default Consumer<Object> asValueConsumer() {
        assert !hasReturns() && isStatic() && args() == 1 : "Contract ' !isReturns() && isStatic() && args() == 1' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert to a Runnable
     */
    default Runnable asRunnable() {
        assert !hasReturns() && isStatic() && args() == 0 : "Contract '!isReturns() && isStatic() && args() == 0' not match";
        return () -> invoke(null);
    }

    /**
     * Convert to a Supplier
     */
    default Supplier<Object> asSupplier() {
        assert hasReturns() && isStatic() && args() == 0 : "Contract 'isReturns() && isStatic() && args() == 0' not match";
        return () -> invoke(null);
    }

    /**
     * Convert to a BiConsumer
     */
    default BiConsumer<Object, Object[]> asBiConsumer() {
        assert !hasReturns() && !isStatic() : "Contract '!isReturns() && !isStatic()' not match";
        return this::invoke;
    }

    @SneakyThrows
    static Invoker build(MethodHandles.Lookup lookup, MethodHandle handle, boolean isStatic, boolean isVarArgs, boolean hasReturn, int argumentCounts) {
        MethodType sam = handle.type().wrap().generic();
        MethodType src = handle.type().wrap();
        if (isVarArgs && argumentCounts == 1) {
            if (isStatic && hasReturn)
                return (Invoker) (sv11) LambdaMetafactory.metafactory(
                        lookup,
                        "i",
                        MethodType.methodType(sv11.class),
                        sam,
                        handle,
                        src).getTarget().invokeExact();
            else if (isStatic)
                return (Invoker) (sv01) LambdaMetafactory.metafactory(
                        lookup,
                        "i",
                        MethodType.methodType(sv01.class),
                        sam,
                        handle,
                        src).getTarget().invokeExact();
            else if (hasReturn)
                return (Invoker) (iv11) LambdaMetafactory.metafactory(
                        lookup,
                        "i",
                        MethodType.methodType(iv11.class),
                        sam,
                        handle,
                        src).getTarget().invokeExact();
            else return (Invoker) (iv01) LambdaMetafactory.metafactory(
                        lookup,
                        "i",
                        MethodType.methodType(iv01.class),
                        sam,
                        handle,
                        src).getTarget().invokeExact();
        }
        switch (argumentCounts) {
            case 0: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx10) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx10.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx00) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx00.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix10) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix10.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix00) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix00.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 1: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx11) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx11.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx01) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx01.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix11) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix11.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix01) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix01.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 2: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx12) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx12.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx02) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx02.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix12) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix12.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix02) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix02.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 3: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx13) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx13.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx03) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx03.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix13) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix13.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix03) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix03.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 4: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx14) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx14.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx04) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx04.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix14) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix14.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix04) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix04.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 5: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx15) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx15.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx05) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx05.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix15) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix15.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix05) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix05.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 6: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx16) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx16.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx06) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx06.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix16) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix16.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix06) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix06.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 7: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx17) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx17.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx07) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx07.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix17) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix17.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix07) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix07.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 8: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx18) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx18.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx08) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx08.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix18) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix18.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix08) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix08.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 9: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx19) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx19.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx09) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx09.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix19) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix19.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix09) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix09.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 10: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx110) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx110.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx010) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx010.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix110) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix110.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix010) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix010.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 11: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx111) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx111.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx011) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx011.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix111) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix111.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix011) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix011.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 12: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx112) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx112.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx012) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx012.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix112) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix112.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix012) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix012.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 13: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx113) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx113.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx013) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx013.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix113) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix113.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix013) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix013.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 14: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx114) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx114.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx014) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx014.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix114) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix114.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix014) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix014.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 15: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx115) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx115.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx015) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx015.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix115) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix115.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix015) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix015.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 16: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx116) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx116.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx016) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx016.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix116) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix116.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix016) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix016.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 17: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx117) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx117.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx017) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx017.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix117) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix117.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix017) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix017.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 18: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx118) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx118.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx018) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx018.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix118) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix118.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix018) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix018.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 19: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx119) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx119.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx019) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx019.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix119) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix119.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix019) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix019.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 20: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx120) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx120.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx020) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx020.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix120) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix120.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix020) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix020.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 21: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx121) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx121.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx021) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx021.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix121) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix121.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix021) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix021.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 22: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx122) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx122.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx022) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx022.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix122) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix122.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix022) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix022.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 23: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx123) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx123.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx023) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx023.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix123) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix123.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix023) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix023.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 24: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx124) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx124.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx024) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx024.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix124) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix124.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix024) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix024.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 25: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx125) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx125.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx025) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx025.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix125) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix125.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix025) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix025.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 26: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx126) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx126.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx026) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx026.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix126) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix126.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix026) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix026.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 27: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx127) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx127.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx027) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx027.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix127) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix127.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix027) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix027.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 28: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx128) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx128.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx028) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx028.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix128) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix128.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix028) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix028.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            case 29: {
                if (isStatic && hasReturn)
                    return (Invoker) (sx129) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx129.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (isStatic)
                    return (Invoker) (sx029) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(sx029.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else if (hasReturn)
                    return (Invoker) (ix129) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix129.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
                else return (Invoker) (ix029) LambdaMetafactory.metafactory(
                            lookup,
                            "i",
                            MethodType.methodType(ix029.class),
                            sam,
                            handle,
                            src).getTarget().invokeExact();
            }
            default:
                throw new IllegalStateException("not support arguments more that 30");
        }
    }

    @SneakyThrows
    static Invoker make(MethodHandles.Lookup lookup, Method m) {
        m.setAccessible(true);
        MethodHandle handle = lookup.unreflect(m);
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        boolean isVararg = m.isVarArgs();
        int counts = m.getParameterCount();
        boolean hasReturn = !m.getReturnType().isAssignableFrom(void.class)
                            && !Void.class.isAssignableFrom(m.getReturnType());
        return build(lookup, handle, isStatic, isVararg, hasReturn, counts);
    }

    @SneakyThrows
    static Invoker make(MethodHandles.Lookup lookup, Constructor<?> m) {
        m.setAccessible(true);
        MethodHandle handle = lookup.unreflectConstructor(m);
        boolean isVararg = m.isVarArgs();
        int counts = m.getParameterCount();
        return build(lookup, handle, true, isVararg, true, counts);
    }

    @SneakyThrows
    static Invoker makeGetter(MethodHandles.Lookup lookup, Field m) {
        m.setAccessible(true);
        MethodHandle handle = lookup.unreflectGetter(m);
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        return isStatic ?
                (Invoker) (sx10) () -> inv(handle)
                : (Invoker) (ix10) o -> inv(handle, o);
    }

    @SneakyThrows
    static Invoker makeSetter(MethodHandles.Lookup lookup, Field m) {
        m.setAccessible(true);
        MethodHandle handle = lookup.unreflectSetter(m);
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        return isStatic ?
                (Invoker) (sx01) (a) -> invA(handle, a)
                : (Invoker) (ix01) (o, a) -> invA(handle, o, a);
    }

    @SneakyThrows
    static Object inv(MethodHandle handle) {
        return handle.invoke((Object) null);
    }

    @SneakyThrows
    static Object inv(MethodHandle handle, Object o) {
        return handle.invoke(o);
    }

    @SneakyThrows
    static Object invA(MethodHandle handle, Object a) {
        return handle.invoke(null, a);
    }

    @SneakyThrows
    static Object invA(MethodHandle handle, Object o, Object a) {
        return handle.invoke(o, a);
    }
}