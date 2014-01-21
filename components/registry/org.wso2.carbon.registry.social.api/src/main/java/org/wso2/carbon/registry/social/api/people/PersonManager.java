/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.social.api.people;

import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.people.userprofile.Person;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;

/**
 * This interface handles retrieval and storage of Person details
 * <p/>
 * Implement this interface to according to the persistence storage of social data
 */
public interface PersonManager {

    /**
     * Persists the details of the person
     *
     * @param userId The userId of the person whose details to be stored
     * @param user   The person details to be stored
     * @return
     * @throws SocialDataException
     */
    public boolean savePerson(String userId, Person user) throws SocialDataException;

    /**
     * Updates/Modify person details
     *
     * @param userId The userId of the person whose details to be modified
     * @param user   The person details to be modified
     * @return
     * @throws SocialDataException
     */
    public boolean updatePerson(String userId, Person user) throws SocialDataException;

    /**
     * Removes the person from the storage
     *
     * @param userId The userId of the person to be deleted
     * @return
     * @throws SocialDataException
     */
    public boolean removePerson(String userId) throws SocialDataException;

    /**
     * Fetches the person details for the given userId
     *
     * @param userId
     * @return A Person object for the given userId
     * @throws SocialDataException
     */
    public Person getPerson(String userId) throws SocialDataException;

    /**
     * Returns an array of persons that correspond to the passed in useIds
     *
     * @param userIds           Array of userIds
     * @param groupId           The group
     * @param filterOptions How to filter, sort and paginate the collection being fetched
     * @param fields            The profile details to fetch. Empty set implies all
     * @return An array of Person objects correspond tot the passed in userIds
     * @throws SocialDataException
     */
    public Person[] getPeople(String[] userIds, String groupId, FilterOptions filterOptions,
                              String[] fields) throws SocialDataException;

    /**
     * Returns a person that corresponds to the passed in userIds
     *
     * @param userId The userId of the persons whose details to be fetched
     * @param fields The fields to be fetched
     * @return A Person object for passes in details
     * @throws SocialDataException
     */
    public Person getPerson(String userId, String[] fields) throws SocialDataException;

}
