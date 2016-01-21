/* 
 * Copyright 2016 xxlabaza.
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

import java.util.stream.IntStream;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 *
 * @author Artem Labazin
 * <p>
 * @since Jan 16, 2016 | 6:43:37 PM
 * <p>
 * @version 1.0.0
 */
@RestController
class PersonController {

    private final static int BATCH_SIZE;

    private final static int PARTITIONS;

    static {
        BATCH_SIZE = 5;
        PARTITIONS = 5;
    }

    @Autowired
    private PersonService personService;

    @RequestMapping
    @ResponseStatus(NO_CONTENT)
    public void doAsync () {
        val ids = personService.findAllIdsInRange(0, BATCH_SIZE * PARTITIONS);
        IntStream.range(0, PARTITIONS)
                .mapToObj(index -> {
                    int from = index * BATCH_SIZE;
                    int to = Math.min(ids.size(), (index + 1) * BATCH_SIZE);
                    return ids.subList(from, to);
                })
                .forEach(personService::doAsync);
    }
}
