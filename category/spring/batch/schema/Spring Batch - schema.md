

## @JobScope와 @StepScope
---
Scope는 스프링 컨테이너에서 빈이 관리되는 범위를 의미합니다.
+ Job과 Step의 빈 생성과 실행에 관여하는 스코프입니다.
+ __프록시 모드__ 를 기본값으로 합니다.
+ 프록시 모드이기 때문에 애플리케이션 구동 시점에는 빈의 프록시 빈이 생성되고 실행 시점에 빈 생성이 이뤄집니다.
    - 이를 통해 빈의 실행 시점에 값을 참조할 수 있는 일종의 Lazy Binding이 가능해집니다.
        - @Value("#{jobParameters[파라미터명]}")
        - @Value("#{jobExecutionContext[파라미터명]}")
        - @Value("#{stepExecutionContext[파라미터명]}")
        - 을 사용해서 값을 주입받습니다.
    - @Value를 사용할 경우 빈 선언문에 @JobScope, @StepScope를 반드시 정의해야 합니다.
+ 병렬 처리 시 각 스레드 마다 생성된 스코프 빈이 할당되기 때문에 스레드에 안전하게 실행이 가능합니다
    - 각 스레드마다 생성된 스코프 빈이 할당되어 각 스레드마다 프록시를 갖고 있어 빈을 호출 시 스레드마다 각각의 빈을 따로 생성하여 갖게 됩니다.
+ Bean과 연관되어 사용하는 것이기 때문에 Tasklet도 당연히 빈등록 해줘야 합니다.

### JobScope
+ Step 선언문에 붙입니다.
+ @Value로 JobParameter과 JobExectionContext만 사용 가능합니다.

### StepScope
+ Tasklet이나 ItemReader, ItemWriter, ItemProcessor 선언문에 붙입니다.
+ @Value로 JobParameter, JobExecutionContext, StepExecutionContet 사용 가능합니다.


### 예시
```java
@Configuration
@RequiredArgsConstructor
public class Test2Config {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("job")
                .start(step1(null)) // 런타임시 주입받을 것이므로 현재는 null로 주입
                .listener(new CustomJobListener())
                .build();
    }

    @Bean
    @JobScope
    public Step step1(@Value("#{jobParameters['message']}") String message) {
        System.out.println("message = " + message);
        return stepBuilderFactory.get("step1")
                .tasklet(tasklet(null,null)) // 런타임 시 주입되므로 null 
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    @StepScope
    public Tasklet tasklet(@Value("#{jobExecutionContext['name']}") String name,
                           @Value("#{stepExecutionContext['name2']}") String name2){
        return (stepContribution, chunkContext) -> {
            System.out.println("name = " + name);
            System.out.println("name2 = " + name2);
            return RepeatStatus.FINISHED;
        };
    }
}
-------------------------------------------------------
public class CustomJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().putString("name","user1");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

    }
}
-------------------------------------------------------
public class CustomStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putString("name2","user2");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
```
@Value를 통해서 런타임시 주입되는 값들에 대해서는 코드로 아무것도 주지 않으면 컴파일 에러가 나기 때문에 null값으로 채워줍니다.  
리스너를 통해서 name, name2 값을 넣어줬고, 실행 시점에 IDE의 Configuration을 통해서 arguments로 message=message로 주고 실행시키면 주입한 값이 정상적으로 찍히게 됩니다.

### 사용하는 이유
처음 공부하는 입장에서는 사실 위에 설명과 예시를 보고 도대체 왜 사용하는지에 대한 의문이 들 수 있어서 다시 정리하겠습니다.
+ 표현식 언어를 통해 유연하고 편리하게 주입받아 파라미터로 사용할 수 있게 됩니다.
    - StepContribution에서 일일이 원하는 값을 꺼내서 사용하지 않아도 된다는 뜻입니다.
+ Step 빈 생성이 구동시점이 아닌 런타임 시점에 생성되어 객체의 지연 로딩이 가능해집니다.
    - 이 덕분에 위에 표현식을 사용할 수 있는 것입니다.
    - 표현식으로 적은 값들은 컴파일 시점에 존재하지 않고 런타임 시점에 채워지면서 존재하는 값입니다.
    - 만약 빈이 애플리케이션 로딩 시점에 만들어진다면 DI를 해야하는데 해당 값들이 현재 존재하지 않기 때문에 찾을 수가 없습니다.
    - 하지만 런타임 시점에 빈을 만들게 되면 값을 다 받아놓고(표현식에 명시한 값들) 빈을 만들기 때문에 주입이 가능하게 됩니다.
+ 병렬 처리시에 각 스레드마다 Step 객체가 생성되어 할당되기 때문에 Tasklet에 멤버 변수가 존재해도 동시성에 문제가 없습니다.

### 아키텍처
+ 프록시 객체 생성
    - @JobScope, @StepScope 애노테이션이 붙은 빈 선언은 내부적으로 프록시 빈 객체가 생성되어 등록됩니다.
    - Job 실행 시 Proxy 객체가 실제 빈을 호출해서 해당 메서드를 실행시키는 구조 입니다.
+ JobScope, StepScope
    - 애노테이션이 붙은 것과는 다른 것으로 Proxy 객체의 실제 대상이 되는 Bean을 등록, 해제하는 역할을 하는 클래스입니다.
    - 실제 대상이 되는 빈을 저장하고 있는 JobContext, StepContext를 갖고 있습니다.
    - Job의 실행 시점에 프록시 객체는 실제 빈을 찾기 위해서 JobScope, StepScope의 JobContext, StepContext를 찾게 됩니다.


![그림8](https://github.com/backtony/blog-code/blob/master/spring/img/batch/5/5-8.PNG?raw=true)
1. JobScope가 붙어서 프록시로 생성된 Step에 요청이 들어옵니다.
2. 프록시는 JobScope의 JobContext에서 실제 타겟 빈이 존재하는지 확인합니다.
3. 있으면 찾아서 반환합니다.
4. 없으면 빈 팩토리에서 실제 Step빈을 생성하고 JobContext에 담고 이를 반환합니다.

### Chunk 기반에서 사용 시 주의사항
```java
@Bean
@StepScope
public ItemReader<? extends Customer> customItemReader(
        @Value("#{stepExecutionContext['minValue']}") Long minValue,
        @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        ....

        return new JpaPagingItemReaderBuilder<Customer>()
                ....
                .build();
}
```
처음에 위와 같이 사용했다가 null 포인트 예외가 터져서 한참 찾았습니다.  
Scope가 아닐 경우에는 Jpa 구현체가 빈으로 등록되기 때문에 전혀 문제가 되지 않습니다.  
하지만 위 코드와 같이 Scope를 사용하면 구현체가 아니라 ItemReader 인터페이스의 프록시 객체가 빈을 등록되서 문제가 발생합니다.  
구현체의 경우 ItemReader와 ItemStream을 모두 구현하고 있기 때문에 문제가 없지만 ItemReader는 read 메서드만 있습니다.  
실제로 stream을 open/close하는 메서드는 ItemStream에 있습니다.  
즉, 위와 같이 사용하면 EntityManagerFactory에서 entityManager을 생성하는게 원래 Stream에서 진행되는 거라 itemReader인 프록시는 그런게 없기 때문에 null 포인트 예외가 발생하게 됩니다.  
이에 대한 해결책은 그냥 구현체를 반환하면 됩니다.
```java
@Bean
@StepScope
public JpaPagingItemReader<? extends Customer> customItemReader(
        @Value("#{stepExecutionContext['minValue']}") Long minValue,
        @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        ....

        return new JpaPagingItemReaderBuilder<Customer>()
                ....
                .build();
}
```
더욱 자세한 내용은 [여기](https://jojoldu.tistory.com/132)를 참고하시면 좋을 것 같습니다.


