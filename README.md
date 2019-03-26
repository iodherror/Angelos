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



