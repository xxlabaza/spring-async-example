/*
 * Copyright 2016 Pivotal Software, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.xxlabaza.test.async;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.joining;

/**
 *
 * @author Artem Labazin
 * <p>
 * @since Jan 20, 2016 | 11:09:38 PM
 * <p>
 * @version 1.0.0
 */
@Service
class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Async("myThreadPoolTaskExecutor")
    @SneakyThrows(InterruptedException.class)
    public void doAsync (List<Integer> sequence) {
        int delay = ThreadLocalRandom.current().nextInt(1, 6);
        TimeUnit.SECONDS.sleep(delay);

        if (sequence.contains(2)) {
            throw new RuntimeException("Oh no! It contains 2!");
        }

        val ids = sequence.stream().map(Object::toString).collect(joining(", "));
        val result = personRepository.findAll(sequence).stream().map(Person::toString).collect(joining(", "));

        System.out.format("\n" +
                "THREAD:      %s\n" +
                "DELAYED:     %ds\n" +
                "ID SEQUENCE: %s\n" +
                "RESULT:      %s\n",
                Thread.currentThread().getName(), delay, ids, result
        );
    }

    public List<Integer> findAllIdsInRange (int page, int size) {
        return personRepository.findAllIdsInRange(new PageRequest(page, size));
    }
}
