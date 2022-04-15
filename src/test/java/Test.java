import com.fernando.myspring.pojo.MyAnnotationConfigApplicationContext;

public class Test {
    @org.junit.Test
    public void test1(){
        // 扫描指定包下的类并注入到容器中
        MyAnnotationConfigApplicationContext applicationContext = new MyAnnotationConfigApplicationContext("com.fernando.myspring.entity");

        // 遍历容器 测试结果
        System.out.println("当前包下有"+applicationContext.getBeanDefinitionCount()+"个bean");
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.print(beanDefinitionName+":");
            System.out.println(applicationContext.getBean(beanDefinitionName));
        }
    }
}
