# Angelos

Angelos是一个轻量级java工具库。旨在强化Java开发效率。目前主要涉及提升java函数式编程的体验。


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
    }
}                          

~~~

## Kyrios模块

提供JOOQ的快速集成功能。
需要jdk8+。
通过``kyrios.yml``进行数据库连接池的相关配置后，
用``Kyrios.Companion.getCtx()``来获取``DSLContext``对象，JOOQ的准备工作就完成了。

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
