<p>
<img src="https://img.shields.io/badge/license-GPLv2%20CE-green?style=plastic" alt="license"/>
<img src="https://img.shields.io/badge/java-8+-yellowgreen?style=plastic" alt="java version"/>
<a href="https://central.sonatype.com/search?smo=true&q=invoker&namespace=io.github.zenliucn">
<img src="https://img.shields.io/maven-central/v/io.github.zenliucn/invoker?style=plastic" alt="maven central"/>
</a>
</p>

# Invoker

Java method, function, field to interface util.

# Usage
```xml
          <dependency>
            <groupId>io.github.zenliucn</groupId>
            <artifactId>invoker</artifactId>
            <version>0.0.1</version>
        </dependency>
```
```java
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
```
