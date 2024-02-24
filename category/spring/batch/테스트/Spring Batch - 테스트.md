## H2로 DB 설정하기
```kotlin
@Configuration
class BatchConfig : DefaultBatchConfiguration() {

    override fun getDataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("/org/springframework/batch/core/schema-drop-h2.sql")
            .addScript("/org/springframework/batch/core/schema-h2.sql")
            .build()
    }

    override fun getTransactionManager(): PlatformTransactionManager {
        return ResourcelessTransactionManager()
    }
}

```
단순한 작업을 하는 경우 또는 재시도 같은 복잡한 작업이 필요 없어 job 이력을 관리할 필요가 없는 경우라면, 실제 운영환경에서도 h2db를 사용하는 경우가 많습니다. DB가 아닌 인메모리에 job을 기록하는 경우 jobRepository 내에서 트랜잭션 처리는 의미가 없으므로 txManager로 ResourcelessTransactionManager를 지정할 수 있습니다.

> spring batch 5.0 이전 버전에는 @EnableBatchProcessing애노테이션이 spring batch 자동 구성을 활성화하는데 사용되었지만 5.0부터는 자동 구성이 default로 설정되었고 @EnableBatchProcessing를 사용하면 자동구성이 되지 않습니다. 따라서 5.0 버전부터는 자동구성을 위해 @EnableBatchProcessing를 제거해야하며, 커스텀하기 위해서는 DefaultBatchConfiguration을 상속받아 커스텀합니다. [migration 문서](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#spring-batch-changes)

## 테스트
spring batch 테스트에는 의존성 추가가 필요합니다.

```groovy
testImplementation 'org.springframework.batch:spring-batch-test'
```

### SpringBootTest
```java
@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class BatchIntegrationTestSupport {

    @Autowired
    private lateinit var jobRepository: JobRepository
    protected val jobLauncherTestUtils = JobLauncherTestUtils()

    protected fun initBatchTestSupport(job: Job) {
        val jobLauncher = createJobLauncher()
        jobLauncherTestUtils.job = job
        jobLauncherTestUtils.jobLauncher = jobLauncher
        jobLauncherTestUtils.jobRepository = jobRepository
    }

    private fun createJobLauncher(): JobLauncher {
        val simpleJobLauncher = TaskExecutorJobLauncher()
        simpleJobLauncher.setJobRepository(jobRepository)
        // test code 에서는 동기 방식으로 job 실행 (job 성공 결과 확인을 위함)
        simpleJobLauncher.setTaskExecutor(SyncTaskExecutor())
        simpleJobLauncher.afterPropertiesSet()
        return simpleJobLauncher
    }
}
```
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL) : 테스트 생성자의 모든 파라미터에 대해 자동으로 의존성을 주입합니다. 이를 이용해서 테스트마다 필요한 Job을 주입받아 테스트합니다.

```java
class SampleJobIntegrationTest(
    private val sampleJob: Job,
) : BatchIntegrationTestSupport() {

    @BeforeEach
    fun init() {
        initBatchTestSupport(sampleJob)
    }

    @Test
    fun sampleTest() {
        // given
        val now = LocalDateTime.now()
        val jobParameters = getSampleJobParams(now)

        // when
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        assertThat(jobExecution).isNotNull
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
    }

    private fun getCleanJobParams(now: LocalDateTime): JobParameters {
        return JobParametersBuilder()
            .addString("targetStartLocalDateTime", now.toString())
            .toJobParameters()
    }
}
```
필요한 job을 인자로 주입받아 테스트할 수 있습니다.

### SpringBatchTest
SpringBootTest만으로도 테스트를 할 수는 있지만 수동으로 설정하는 작업이 필요한 경우들이 발생할 수 있는데 SpringBatchTest를 사용하면 조금 더 수월하게 세팅할 수 있습니다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@TestExecutionListeners(listeners = { StepScopeTestExecutionListener.class, JobScopeTestExecutionListener.class },
		mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ExtendWith(SpringExtension.class)
public @interface SpringBatchTest {

}
```

springBatchTest는 자동으로 ApplicationContext에 테스트에 필요한 여러 유틸 Bean을 등록해주는 애노테이션입니다.
+ JobLauncherTestUtils
    - launchJob(), launchStep() 과 같은 스프링 배치 테스트에 필요한 유틸성 메서드 지원
+ JobRepositoryTestUtils
    - JobRepository를 사용해서 JobExecution을 생성 및 삭제 기능 메서드 지원
+ StepScopeTestExecutionListener
    - @StepScope 컨텍스트를 생성해주며 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있습니다.
+ JobScopeTestExecutionListener
    - @JobScope 컨텍스트를 생성해 주며 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있습니다.


```java
@SpringBatchTest
@SpringJUnitConfig(SkipSampleConfiguration.class)
public class SkipSampleFunctionalTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void testJob(@Autowired Job job) throws Exception {
        this.jobLauncherTestUtils.setJob(job);
        this.jdbcTemplate.update("delete from CUSTOMER");
        for (int i = 1; i <= 10; i++) {
            this.jdbcTemplate.update("insert into CUSTOMER values (?, 0, ?, 100000)",
                    i, "customer" + i);
        }

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();


        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}
```
