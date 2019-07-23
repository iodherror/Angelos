# Angelos

Angelos是一个轻量级java工具库。旨在强化Java开发效率。目前主要涉及提升java函数式编程的体验。

## pom引入
~~~
        <dependency>
            <groupId>tk.qcsoft.angelos</groupId>
            <artifactId>exia</artifactId>
            <version>1.0.3</version>
        </dependency>
~~~


## Exia模块

提供类似kotlin的函数式编程语法体验。简单易用。提供多种对目标类的结构化函数操作。
需要jdk8+。

简单示例：
~~~java
public class Person {

    private int age;
    private String name;

    public int getAge() {
        return age;
    }

    public Person setAge(int age) {
        this.age = age;
        return this;
    }

    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public void sayHello(){
        System.out.println("Hello");
    }

    public Boolean isAdult(){
        return age>=18;
    }

    public static void main(String[] args) {
        boolean isBillAdult = Exia.of(new Person())   //对Person对象进行封装
                .apply(o-> o.setName("Bill").setAge(1))   //对Person进行初始化
                .apply(Person::sayHello)    //执行Person的方法  输出 Hello
                .run(Person::isAdult);      //执行Person的方法并获取其返回值
        System.out.println(isBillAdult);  //输出 false
        
        System.out.println(Switcher.of("31","Level 0")
                .eCase(s -> "level 1","10","11")
                .eCase(s -> "level 2","22","21")
                .eCase(s -> "level 3","31","32")
                .eCase(s -> "level 4","31","41")   //从第三个eCase匹配到了 所以此行不会再执行
                .run());    //输出 level 3
    }
}                          

~~~

## Kyrios模块

提供JOOQ的快速集成功能。
需要jdk8+。
通过``kyrios.yml``进行数据库连接池的相关配置后，
用``Kyrios.Companion.getCtx()``来获取``DSLContext``对象，JOOQ的准备工作就完成了。

## Dynames模块

常用设计模式的落地，用于优化if/else逻辑，增强代码可读性。
业务处理类实现``Dynames``接口，定义好处理的对象范围以及具体的处理方式。
简单的对``services``进行配置之后，就可以直接使用``Ptolemaios``将处理器调起。支持缓存以及热加载。

需要jdk8+。
简单示例：
~~~java
public class TestDynames {

    public static void main(String[] args) {
        //直接调取对应的Dynames实现类，找不到时会抛出异常
        System.out.println(Ptolemaios.<String,String>launchDynames("big","red").fire("1"));
        
        //查找对应的Dynames实现类并返回Optional对象
        Ptolemaios.<Integer,Boolean>findDynames("small","yellow").ifPresent(dynames -> System.out.println(dynames.fire(2)));
        Ptolemaios.<String,String>findDynames("big","pink").ifPresent(dynames -> System.out.println(dynames.fire("3")));
    }
}         
~~~

## Virtue模块

优化异常处理逻辑，提供了多个异常断言方法。
需要jdk8+。
主要功能位于``ExceptionLauncherImpl``.

简单示例：
~~~java
public class Person {

    //省略

    public static void main(String[] args) {
        
        ExceptionLauncher exceptionLauncher = new ExceptionLauncherImpl();//spring中你可以注入该对象，或者其他方式。
        
        boolean isBillAdult = Exia.of(new Person())   //对Person对象进行封装
                .apply(o-> o.setName("Bill").setAge(1))   //对Person进行初始化
                .apply(Person::sayHello)    //执行Person的方法  输出 Hello
                .run(Person::isAdult);      //执行Person的方法并获取其返回值
        
        exceptionLauncher.checkArgument(isBillAdult,"bill must be adult");// throw exception
        
        System.out.println(isBillAdult); //没有执行
    }
}                          

~~~
