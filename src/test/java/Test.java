import com.fernando.myspring.applicationContext.impl.MyAnnotationConfigApplicationContext;
import com.fernando.myspring.entity.AOP.Book;

public class Test {
    // 测试IOC功能
    @org.junit.Test
    public void test1() throws Exception {
        // 扫描指定包下的类并注入到容器中
        MyAnnotationConfigApplicationContext applicationContext = new MyAnnotationConfigApplicationContext("com.fernando.myspring.entity.IOC");

        // 遍历容器 测试结果
        System.out.println("当前包下有:" + applicationContext.getBeanDefinitionCount() + "个bean");
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.print(beanDefinitionName + ":");
            System.out.println(applicationContext.getBean(beanDefinitionName));
        }
    }

    // 测试AOP功能
    @org.junit.Test
    public void test2() throws Exception {
        MyAnnotationConfigApplicationContext applicationContext = new MyAnnotationConfigApplicationContext("com.fernando.myspring.entity.AOP");

        applicationContext.getBean(Book.class).getBook(1,"sanguo");
    }
}
