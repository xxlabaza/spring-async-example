# Описание #

## Что происходит? ##

В встроенное базе данных лежит 100 записей, по запросу к сервису:

```bash
$> curl localhost:8080/
```

Считается из БД 25 первых записей и разабьются на 5 разделов по их **id**, каждый из которых будет асинхронно обработан методом сервиса [PersonService](https://github.com/xxlabaza/spring-async-example/blob/master/src/main/java/ru/xxlabaza/test/async/PersonService.java). Записи в лог сервиса будут выглядеть примерно вот так:

```bash
THREAD:      myThreadPoolTaskExecutor-2
DELAYED:     3s
ID SEQUENCE: 15, 16, 17, 18, 19
RESULT:      Петя[15], Андрей[16], Надя[17], Андрей[18], Миша[19]

THREAD:      myThreadPoolTaskExecutor-3
DELAYED:     4s
ID SEQUENCE: 20, 21, 22, 23, 24
RESULT:      Лиза[20], Паша[21], Надя[22], Миша[23], Паша[24]

2016-01-21 01:06:55.276 ERROR 1537 --- [lTaskExecutor-1] .a.i.SimpleAsyncUncaughtExceptionHandler : Unexpected error occurred invoking async method 'public void ru.xxlabaza.test.async.PersonService.doAsync(java.util.List)'.

java.lang.RuntimeException: Oh no! It contains 2!
    at ru.xxlabaza.test.async.PersonService.doAsync(PersonService.java:51) ~[classes/:na]
    at ru.xxlabaza.test.async.PersonService$$FastClassBySpringCGLIB$$b6049e3a.invoke(<generated>) ~[classes/:na]
    at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:204) ~[spring-core-4.2.4.RELEASE.jar:4.2.4.RELEASE]
    at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:720) ~[spring-aop-4.2.4.RELEASE.jar:4.2.4.RELEASE]
    at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157) ~[spring-aop-4.2.4.RELEASE.jar:4.2.4.RELEASE]
    at org.springframework.aop.interceptor.AsyncExecutionInterceptor$1.call(AsyncExecutionInterceptor.java:108) ~[spring-aop-4.2.4.RELEASE.jar:4.2.4.RELEASE]
    at java.util.concurrent.FutureTask.run(FutureTask.java:266) [na:1.8.0_66]
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_66]
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_66]
    at java.lang.Thread.run(Thread.java:745) [na:1.8.0_66]


THREAD:      myThreadPoolTaskExecutor-2
DELAYED:     2s
ID SEQUENCE: 5, 6, 7, 8, 9
RESULT:      Оля[5], Костя[6], Надя[7], Лиза[8], Ангелина[9]

THREAD:      myThreadPoolTaskExecutor-3
DELAYED:     2s
ID SEQUENCE: 10, 11, 12, 13, 14
RESULT:      Даня[10], Лиза[11], Таня[12], Катя[13], Костя[14]

```

## Настройка TaskExecutor ##

В классе [TaskExecutorConfiguration](https://github.com/xxlabaza/spring-async-example/blob/master/src/main/java/ru/xxlabaza/test/async/TaskExecutorConfiguration.java) происходит настройка пула потоков, который используется в [PersonService](https://github.com/xxlabaza/spring-async-example/blob/master/src/main/java/ru/xxlabaza/test/async/PersonService.java) по имени создаваемого бина (смотри значение в аннотации **@Async** над методом **doAsync()**).

Создаваемый бин класса **ThreadPoolTaskExecutor**'а имеет три основные опции:

* **corePoolSize** - начальное количество потоков, которые будут обрабатывать задачи;
* **maxPoolSize** - максимально допустимое количество потоков;
* **queueCapacity** - очередь в которую попадают задачи на выполнение.

Из настроек пула и, доступного выше, вывода сервиса, видно что было назначено 5 задач (по количеству разделов) и ообработало их 3 потока, потому что очередь задачь переполнилась и пришлось выделить ещё обработчиков. По диапозону разделов видно что 2 и 3 потоки обрабатывают последнии две порции раделов, а первый поток выпал с эксепшеном...ну просто потому что мне так захотелось, это видно по коду [PersonService](https://github.com/xxlabaza/spring-async-example/blob/master/src/main/java/ru/xxlabaza/test/async/PersonService.java), а почему бы и нет?
