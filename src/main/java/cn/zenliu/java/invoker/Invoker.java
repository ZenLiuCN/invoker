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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.*;

/**
 * Invoker is the generic interface to build lambda for MethodHandle.<br/>
 *
 * @author Zen.Liu
 * @since 2023-08-23
 */
@SuppressWarnings("unused")
public interface Invoker {

    @ApiStatus.AvailableSince("0.0.1")
    /**
     * @param instance the object instance
     * @param args     args of function
     * @return null if build from a function without return value.
     */
    Object invoke(@Nullable Object instance, Object... args);

    /**
     * @return does have return value
     */
    @ApiStatus.AvailableSince("0.0.1")
    boolean hasReturns();

    /**
     * Does accept a pure varargs array as parameter, if true, only first arg as array should be passed.
     */
    @ApiStatus.AvailableSince("0.0.1")
    boolean isOnlyVarArgs();

    /**
     * Does require a instance, for constructor, static field, static method, the instance does not require.
     */
    @ApiStatus.AvailableSince("0.0.1")
    boolean isStatic();

    /**
     * How many args required, except the instance.(for varargs always 1)
     */
    @ApiStatus.AvailableSince("0.0.1")
    int args();

    /**
     * Convert invoker to  Function accept objects
     */
    @ApiStatus.AvailableSince("0.0.1")
    default Function<Object[], Object> asFunction() {
        assert hasReturns() && isStatic() && !isOnlyVarArgs() : "Contract 'isReturns() && isStatic() && !isOnlyVarArgs()' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert invoker to Function accept object
     */
    @ApiStatus.AvailableSince("0.0.1")
    default Function<Object, Object> asStaticFunction() {
        assert hasReturns() && isStatic() && args() == 1 : "Contract 'isReturns() && isStatic() && args() == 1' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert to Function accept instance object
     */
    @ApiStatus.AvailableSince("0.0.1")
    default Function<Object, Object> asInstanceSupplier() {
        assert hasReturns() && !isStatic() && args() == 0 : "Contract 'isReturns() && !isStatic() && args() == 0' not match";
        return this::invoke;
    }

    /**
     * Convert to a BiFunction accept instance and arguments
     */
    @ApiStatus.AvailableSince("0.0.1")
    default BiFunction<Object, Object[], Object> asBiFunction() {
        assert hasReturns() && !isStatic() : "not match contract";
        return this::invoke;
    }

    /**
     * Convert to an Objects Consumer
     */
    @ApiStatus.AvailableSince("0.0.1")
    default Consumer<Object[]> asConsumer() {
        assert !hasReturns() && isStatic() : "Contract: '!isReturns() && isStatic()' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert to an Object Consumer
     */
    @ApiStatus.AvailableSince("0.0.1")
    default Consumer<Object> asValueConsumer() {
        assert !hasReturns() && isStatic() && args() == 1 : "Contract ' !isReturns() && isStatic() && args() == 1' not match";
        return a -> invoke(null, a);
    }

    /**
     * Convert to a Runnable
     */
    @ApiStatus.AvailableSince("0.0.1")
    default Runnable asRunnable() {
        assert !hasReturns() && isStatic() && args() == 0 : "Contract '!isReturns() && isStatic() && args() == 0' not match";
        return () -> invoke(null);
    }

    /**
     * Convert to a Supplier
     */
    @ApiStatus.AvailableSince("0.0.1")
    default Supplier<Object> asSupplier() {
        assert hasReturns() && isStatic() && args() == 0 : "Contract 'isReturns() && isStatic() && args() == 0' not match";
        return () -> invoke(null);
    }

    /**
     * Convert to a BiConsumer
     */
    @ApiStatus.AvailableSince("0.0.1")
    default BiConsumer<Object, Object[]> asBiConsumer() {
        assert !hasReturns() && !isStatic() : "Contract '!isReturns() && !isStatic()' not match";
        return this::invoke;
    }

    @ApiStatus.AvailableSince("0.0.1")
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

    @ApiStatus.AvailableSince("0.0.1")
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

    @ApiStatus.AvailableSince("0.0.1")
    @SneakyThrows
    static Invoker make(MethodHandles.Lookup lookup, Constructor<?> m) {
        m.setAccessible(true);
        MethodHandle handle = lookup.unreflectConstructor(m);
        boolean isVararg = m.isVarArgs();
        int counts = m.getParameterCount();
        return build(lookup, handle, true, isVararg, true, counts);
    }

    @ApiStatus.AvailableSince("0.0.1")
    @SneakyThrows
    static Invoker makeGetter(MethodHandles.Lookup lookup, Field m) {
        m.setAccessible(true);
        MethodHandle handle = lookup.unreflectGetter(m);
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        return isStatic ?
                (Invoker) (sx10) () -> inv(handle)
                : (Invoker) (ix10) o -> inv(handle, o);
    }

    @ApiStatus.AvailableSince("0.0.1")
    @SneakyThrows
    static Invoker makeSetter(MethodHandles.Lookup lookup, Field m) {
        m.setAccessible(true);
        MethodHandle handle = lookup.unreflectSetter(m);
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        return isStatic ?
                (Invoker) (sx01) (a) -> invA(handle, a)
                : (Invoker) (ix01) (o, a) -> invA(handle, o, a);
    }

    @ApiStatus.Internal
    @SneakyThrows
    static Object inv(MethodHandle handle) {
        return handle.invoke((Object) null);
    }

    @ApiStatus.Internal
    @SneakyThrows
    static Object inv(MethodHandle handle, Object o) {
        return handle.invoke(o);
    }

    @ApiStatus.Internal
    @SneakyThrows
    static Object invA(MethodHandle handle, Object a) {
        return handle.invoke(null, a);
    }

    @ApiStatus.Internal
    @SneakyThrows
    static Object invA(MethodHandle handle, Object o, Object a) {
        return handle.invoke(o, a);
    }

    //region SAM
    interface iv01 extends sam {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0]);
            return null;
        }

        void i(Object instance, Object arg0);

        @Override
        default boolean hasReturns() {
            return false;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return true;
        }

        @Override
        default boolean isStatic() {
            return false;
        }

        @Override
        default int args() {
            return 1;
        }
    }


    interface iv11 extends sam {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0]);
        }

        Object i(Object instance, Object arg0);

        @Override
        default boolean hasReturns() {
            return true;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return true;
        }

        @Override
        default boolean isStatic() {
            return false;
        }

        @Override
        default int args() {
            return 1;
        }
    }


    interface ix0 extends sam {
        @Override
        default boolean hasReturns() {
            return false;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return false;
        }

        @Override
        default boolean isStatic() {
            return false;
        }
    }

    interface ix00 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"));
            return null;
        }

        Object i(Object instance);

        @Override
        default int args() {
            return 0;
        }
    }


    interface ix1 extends sam {
        @Override
        default boolean hasReturns() {
            return true;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return false;
        }

        @Override
        default boolean isStatic() {
            return false;
        }
    }

    interface ix01 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0]);
            return null;
        }

        Object i(Object instance, Object a0);

        @Override
        default int args() {
            return 1;
        }
    }

    interface ix02 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1);

        @Override
        default int args() {
            return 2;
        }
    }

    interface ix03 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2);

        @Override
        default int args() {
            return 3;
        }
    }

    interface ix04 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3);

        @Override
        default int args() {
            return 4;
        }
    }

    interface ix05 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4);

        @Override
        default int args() {
            return 5;
        }
    }

    interface ix06 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5);

        @Override
        default int args() {
            return 6;
        }
    }

    interface ix07 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6);

        @Override
        default int args() {
            return 7;
        }
    }

    interface ix08 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7);

        @Override
        default int args() {
            return 8;
        }
    }

    interface ix09 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8);

        @Override
        default int args() {
            return 9;
        }
    }

    interface ix10 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"));
        }

        Object i(Object instance);

        @Override
        default int args() {
            return 0;
        }
    }

    interface ix010 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9);

        @Override
        default int args() {
            return 10;
        }
    }

    interface ix11 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0]);
        }

        Object i(Object instance, Object a0);

        @Override
        default int args() {
            return 1;
        }
    }

    interface ix011 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10);

        @Override
        default int args() {
            return 11;
        }
    }

    interface ix12 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1]);
        }

        Object i(Object instance, Object a0, Object a1);

        @Override
        default int args() {
            return 2;
        }
    }

    interface ix012 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11);

        @Override
        default int args() {
            return 12;
        }
    }

    interface ix13 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2);

        @Override
        default int args() {
            return 3;
        }
    }

    interface ix013 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12);

        @Override
        default int args() {
            return 13;
        }
    }

    interface ix14 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3);

        @Override
        default int args() {
            return 4;
        }
    }

    interface ix014 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13);

        @Override
        default int args() {
            return 14;
        }
    }

    interface ix15 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4);

        @Override
        default int args() {
            return 5;
        }
    }

    interface ix015 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14);

        @Override
        default int args() {
            return 15;
        }
    }

    interface ix16 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5);

        @Override
        default int args() {
            return 6;
        }
    }

    interface ix016 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15);

        @Override
        default int args() {
            return 16;
        }
    }

    interface ix17 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6);

        @Override
        default int args() {
            return 7;
        }
    }

    interface ix017 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16);

        @Override
        default int args() {
            return 17;
        }
    }

    interface ix18 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7);

        @Override
        default int args() {
            return 8;
        }
    }

    interface ix018 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17);

        @Override
        default int args() {
            return 18;
        }
    }

    interface ix19 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8);

        @Override
        default int args() {
            return 9;
        }
    }

    interface ix019 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18);

        @Override
        default int args() {
            return 19;
        }
    }

    interface ix020 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19);

        @Override
        default int args() {
            return 20;
        }
    }

    interface ix021 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20);

        @Override
        default int args() {
            return 21;
        }
    }

    interface ix022 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21);

        @Override
        default int args() {
            return 22;
        }
    }

    interface ix023 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22);

        @Override
        default int args() {
            return 23;
        }
    }

    interface ix024 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23);

        @Override
        default int args() {
            return 24;
        }
    }

    interface ix025 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24);

        @Override
        default int args() {
            return 25;
        }
    }

    interface ix026 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25);

        @Override
        default int args() {
            return 26;
        }
    }

    interface ix027 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26);

        @Override
        default int args() {
            return 27;
        }
    }

    interface ix028 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27);

        @Override
        default int args() {
            return 28;
        }
    }

    interface ix029 extends ix0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28]);
            return null;
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27, Object a28);

        @Override
        default int args() {
            return 29;
        }
    }

    interface ix110 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9);

        @Override
        default int args() {
            return 10;
        }
    }

    interface ix111 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10);

        @Override
        default int args() {
            return 11;
        }
    }

    interface ix112 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11);

        @Override
        default int args() {
            return 12;
        }
    }

    interface ix113 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12);

        @Override
        default int args() {
            return 13;
        }
    }

    interface ix114 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13);

        @Override
        default int args() {
            return 14;
        }
    }

    interface ix115 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14);

        @Override
        default int args() {
            return 15;
        }
    }

    interface ix116 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15);

        @Override
        default int args() {
            return 16;
        }
    }

    interface ix117 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16);

        @Override
        default int args() {
            return 17;
        }
    }

    interface ix118 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17);

        @Override
        default int args() {
            return 18;
        }
    }

    interface ix119 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18);

        @Override
        default int args() {
            return 19;
        }
    }

    interface ix120 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19);

        @Override
        default int args() {
            return 20;
        }
    }

    interface ix121 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20);

        @Override
        default int args() {
            return 21;
        }
    }

    interface ix122 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21);

        @Override
        default int args() {
            return 22;
        }
    }

    interface ix123 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22);

        @Override
        default int args() {
            return 23;
        }
    }

    interface ix124 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23);

        @Override
        default int args() {
            return 24;
        }
    }

    interface ix125 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24);

        @Override
        default int args() {
            return 25;
        }
    }

    interface ix126 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25);

        @Override
        default int args() {
            return 26;
        }
    }

    interface ix127 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26);

        @Override
        default int args() {
            return 27;
        }
    }

    interface ix128 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27);

        @Override
        default int args() {
            return 28;
        }
    }

    interface ix129 extends ix1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            return i(Objects.requireNonNull(instance, "instance required"), args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28]);
        }

        Object i(Object instance, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27, Object a28);

        @Override
        default int args() {
            return 29;
        }
    }

    /**
     * Naming strategy:<br/>
     * 1. i or s : instance or static. <br/>
     * 2. v or x : simple one varargs or complex. <br/>
     * 3. 0 or 1: no return or returns.<br/>
     * 4. 0 - 30: arguments count.<br/>
     *
     * @author Zen.Liu
     * @since 2023-08-23
     */
    interface sam extends Invoker {

    }


    interface sv01 extends sam {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(null, args[0]);
            return null;
        }

        void i(Object instance, Object arg0);

        @Override
        default boolean hasReturns() {
            return false;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return true;
        }

        @Override
        default boolean isStatic() {
            return true;
        }

        @Override
        default int args() {
            return 1;
        }
    }


    interface sv11 extends sam {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(null, args[0]);
        }

        Object i(Object instance, Object arg0);

        @Override
        default boolean hasReturns() {
            return false;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return true;
        }

        @Override
        default boolean isStatic() {
            return false;
        }

        @Override
        default int args() {
            return 1;
        }
    }


    interface sx0 extends sam {
        @Override
        default boolean hasReturns() {
            return false;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return false;
        }

        @Override
        default boolean isStatic() {
            return true;
        }
    }

    interface sx00 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i();
            return null;
        }

        Object i();

        @Override
        default int args() {
            return 0;
        }
    }


    interface sx1 extends sam {
        @Override
        default boolean hasReturns() {
            return true;
        }

        @Override
        default boolean isOnlyVarArgs() {
            return false;
        }

        @Override
        default boolean isStatic() {
            return true;
        }
    }

    interface sx01 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0]);
            return null;
        }

        Object i(Object a0);

        @Override
        default int args() {
            return 1;
        }
    }

    interface sx02 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1]);
            return null;
        }

        Object i(Object a0, Object a1);

        @Override
        default int args() {
            return 2;
        }
    }

    interface sx03 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2);

        @Override
        default int args() {
            return 3;
        }
    }

    interface sx04 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3);

        @Override
        default int args() {
            return 4;
        }
    }

    interface sx05 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4);

        @Override
        default int args() {
            return 5;
        }
    }

    interface sx06 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5);

        @Override
        default int args() {
            return 6;
        }
    }

    interface sx07 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6);

        @Override
        default int args() {
            return 7;
        }
    }

    interface sx08 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7);

        @Override
        default int args() {
            return 8;
        }
    }

    interface sx09 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8);

        @Override
        default int args() {
            return 9;
        }
    }

    interface sx10 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i();
        }

        Object i();

        @Override
        default int args() {
            return 0;
        }
    }

    interface sx010 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9);

        @Override
        default int args() {
            return 10;
        }
    }

    interface sx11 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0]);
        }

        Object i(Object a0);

        @Override
        default int args() {
            return 1;
        }
    }

    interface sx011 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10);

        @Override
        default int args() {
            return 11;
        }
    }

    interface sx12 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1]);
        }

        Object i(Object a0, Object a1);

        @Override
        default int args() {
            return 2;
        }
    }

    interface sx012 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11);

        @Override
        default int args() {
            return 12;
        }
    }

    interface sx13 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2]);
        }

        Object i(Object a0, Object a1, Object a2);

        @Override
        default int args() {
            return 3;
        }
    }

    interface sx013 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12);

        @Override
        default int args() {
            return 13;
        }
    }

    interface sx14 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3);

        @Override
        default int args() {
            return 4;
        }
    }

    interface sx014 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13);

        @Override
        default int args() {
            return 14;
        }
    }

    interface sx15 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4);

        @Override
        default int args() {
            return 5;
        }
    }

    interface sx015 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14);

        @Override
        default int args() {
            return 15;
        }
    }

    interface sx16 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5);

        @Override
        default int args() {
            return 6;
        }
    }

    interface sx016 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15);

        @Override
        default int args() {
            return 16;
        }
    }

    interface sx17 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6);

        @Override
        default int args() {
            return 7;
        }
    }

    interface sx017 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16);

        @Override
        default int args() {
            return 17;
        }
    }

    interface sx18 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7);

        @Override
        default int args() {
            return 8;
        }
    }

    interface sx018 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17);

        @Override
        default int args() {
            return 18;
        }
    }

    interface sx19 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8);

        @Override
        default int args() {
            return 9;
        }
    }

    interface sx019 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18);

        @Override
        default int args() {
            return 19;
        }
    }

    interface sx020 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19);

        @Override
        default int args() {
            return 20;
        }
    }

    interface sx021 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20);

        @Override
        default int args() {
            return 21;
        }
    }

    interface sx022 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21);

        @Override
        default int args() {
            return 22;
        }
    }

    interface sx023 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22);

        @Override
        default int args() {
            return 23;
        }
    }

    interface sx024 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23);

        @Override
        default int args() {
            return 24;
        }
    }

    interface sx025 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24);

        @Override
        default int args() {
            return 25;
        }
    }

    interface sx026 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25);

        @Override
        default int args() {
            return 26;
        }
    }

    interface sx027 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26);

        @Override
        default int args() {
            return 27;
        }
    }

    interface sx028 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27);

        @Override
        default int args() {
            return 28;
        }
    }

    interface sx029 extends sx0 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28]);
            return null;
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27, Object a28);

        @Override
        default int args() {
            return 29;
        }
    }

    interface sx110 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9);

        @Override
        default int args() {
            return 10;
        }
    }

    interface sx111 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10);

        @Override
        default int args() {
            return 11;
        }
    }

    interface sx112 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11);

        @Override
        default int args() {
            return 12;
        }
    }

    interface sx113 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12);

        @Override
        default int args() {
            return 13;
        }
    }

    interface sx114 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13);

        @Override
        default int args() {
            return 14;
        }
    }

    interface sx115 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14);

        @Override
        default int args() {
            return 15;
        }
    }

    interface sx116 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15);

        @Override
        default int args() {
            return 16;
        }
    }

    interface sx117 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16);

        @Override
        default int args() {
            return 17;
        }
    }

    interface sx118 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17);

        @Override
        default int args() {
            return 18;
        }
    }

    interface sx119 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18);

        @Override
        default int args() {
            return 19;
        }
    }

    interface sx120 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19);

        @Override
        default int args() {
            return 20;
        }
    }

    interface sx121 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20);

        @Override
        default int args() {
            return 21;
        }
    }

    interface sx122 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21);

        @Override
        default int args() {
            return 22;
        }
    }

    interface sx123 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22);

        @Override
        default int args() {
            return 23;
        }
    }

    interface sx124 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23);

        @Override
        default int args() {
            return 24;
        }
    }

    interface sx125 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24);

        @Override
        default int args() {
            return 25;
        }
    }

    interface sx126 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25);

        @Override
        default int args() {
            return 26;
        }
    }

    interface sx127 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26);

        @Override
        default int args() {
            return 27;
        }
    }

    interface sx128 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27);

        @Override
        default int args() {
            return 28;
        }
    }

    interface sx129 extends sx1 {
        @Override
        default Object invoke(@Nullable Object instance, Object... args) {
            assert instance == null : "no instance required";
            return i(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28]);
        }

        Object i(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10, Object a11, Object a12, Object a13, Object a14, Object a15, Object a16, Object a17, Object a18, Object a19, Object a20, Object a21, Object a22, Object a23, Object a24, Object a25, Object a26, Object a27, Object a28);

        @Override
        default int args() {
            return 29;
        }
    }
    //endregion
}