# mySpring
### 2022.4.15 version 0.1
* 实现了IOC容器最基本的功能（但还不够完善）创建容器并实现基本的注入功能
* 实现注解@Component、@Value、@Autowired、@Qualifier所涵盖的功能

### 2022.4.17 version 0.2
* 实现注解@Controller、@Service、@Repository等功能 细化了Spring Bean的类型

### 2022.4.18 version 0.3
* 实现@Lazy懒加载、@Scope指定bean的生成规则等功能
* IOC容器初始化的基本功能已实现

### 2022.4.22 version 0.4
* 新增配置类功能，实现@Configuration和@Bean注解及注入配置类的逻辑
* 对容器做升级，增加多级缓存

### 2022.4.23 version 0.5
* 细化每个步骤所需维护的全局集合
* 增加容器接口的一些抽象方法并实现

### 2022.4.25 version 1.0
* 实现带有扩展性的IOC容器 支持在容器初始化过程中完成代码扩展

### 2022.4.28 version 1.1
* 实现@Aspect及基本AOP功能的动态代理步骤 支持jdkProxy代理
* 实现@After、@AfterReturning、@AfterThrowing、@Before四个切入点注解

### 2022.4.30 version 1.5
* 实现AOP功能的切面类的通知方法与目标方法组合的逻辑

### 2022.5.2 version 2.0
* 自定义并完善容器初始化过程中抛出的异常
* 实现proxy工厂的另一实现类CGLibProxy

### 2022.5.4 version 2.3
* 实现大部分功能的测试
* 规范化、完善日志打印逻辑
