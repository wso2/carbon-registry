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
package org.wso2.carbon.registry.social.impl.people.userprofile;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.people.PersonManager;
import org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager;
import org.wso2.carbon.registry.social.api.people.userprofile.Person;
import org.wso2.carbon.registry.social.api.people.userprofile.model.*;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;
import org.wso2.carbon.registry.social.impl.SocialImplConstants;
import org.wso2.carbon.registry.social.impl.internal.SocialDSComponent;
import org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl;
import org.wso2.carbon.registry.social.impl.people.userprofile.model.PersonImpl;
import org.wso2.carbon.registry.social.impl.people.userprofile.model.impl.*;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;

import java.io.Serializable;
import java.util.*;

/**
 * An implementation of the {@link org.wso2.carbon.registry.social.api.people.PersonManager} interface
 * <p>
 * This implementation uses the {@link org.wso2.carbon.user.core.UserStoreManager} to store {@link org.wso2.carbon.registry.social.api.people.userprofile.Person} data
 * </p>
 * <p>
 * Complex attributes of {@link org.wso2.carbon.registry.social.api.people.userprofile.Person} are converted to name-value pairs and stored when possible
 * </p>
 */

public class PersonManagerImpl implements PersonManager {

    private static Log log = LogFactory.getLog(PersonManagerImpl.class);
    private UserStoreManager userStoreManager = null;
    private ClaimManager claimManager = null;

    public void setUserStoreManager(UserStoreManager userStoreManager) {
        this.userStoreManager = userStoreManager;
    }

    public UserStoreManager getUserStoreManager() throws RegistryException, UserStoreException {
        if (this.userStoreManager != null) {
            return this.userStoreManager;
        } else {
            return SocialDSComponent.getUserStoreManager();
        }
    }

    public void setClaimManager(ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    public ClaimManager getClaimManager() throws RegistryException, UserStoreException {
        if (this.claimManager != null) {
            return this.claimManager;
        } else {
            return SocialDSComponent.getClaimManager();
        }
    }

    /**
     * Persists the details of the person
     *
     * @param userId The userId of the person whose details to be stored
     * @param person The person details to be stored
     * @return True - if successfully save else False
     * @throws SocialDataException
     */
    public boolean savePerson(String userId, Person person) throws SocialDataException {
        try {
            userStoreManager = getUserStoreManager();

           if (!userStoreManager.isExistingUser(userId)) {
                //TODO: Creating new user
                //TODO: Below addUser function has to be done properly,until that it
                //TODO: has been commented out.
                //userStoreManager.addUser(userId, "abcd123", new String[]{"admin"}, null, null);
            }
            userStoreManager.setUserClaimValues(userId, retrieveClaimValues(person), null);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while saving person with id " + userId, e);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while saving person with id " + userId, e);
        }
        return true;
    }

    public void saveUserClaims(String userId, Map<String, String> claims)
            throws SocialDataException {
        try {
            userStoreManager = getUserStoreManager();
            if (userStoreManager.isExistingUser(userId)) {
                userStoreManager.setUserClaimValues(userId, claims, null);
            } else {
                throw new SocialDataException("No user existing with id " + userId);
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while saving person with id " + userId, e);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while saving person with id " + userId, e);
        }
    }

    /**
     * Updates/Modify person details
     *
     * @param userId The userId of the person whose details to be modified
     * @param person The person details to be modified
     * @return True - if the data updated successfully esle False
     * @throws SocialDataException
     */
    public boolean updatePerson(String userId, Person person) throws SocialDataException {
        return savePerson(userId, person);
    }

    /**
     * Removes the person from the storage
     *
     * @param userId The userId of the person to be deleted
     * @return True- if the removal was successful else False
     * @throws SocialDataException
     */
    public boolean removePerson(String userId) throws SocialDataException {

        try {
            userStoreManager = getUserStoreManager();
            userStoreManager.deleteUser(userId);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while removing person with id " + userId, e);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while removing person with id " + userId, e);
        }
        return true;
    }

    /**
     * Fetches the person details for the given userId
     *
     * @param userId The id of ther person to fetch
     * @return A Person object for the given userId
     * @throws SocialDataException
     */
    public Person getPerson(String userId) throws SocialDataException {
        Person personObj;

        try {
            userStoreManager = getUserStoreManager();
            claimManager = getClaimManager();
            if (!userStoreManager.isExistingUser(userId)) {
                log.error("No user found for the id " + userId);
                return null;
            }
            String[] claims = claimManager.getAllClaimUris();
            Map<String, String> userClaims = userStoreManager.getUserClaimValues(userId,
                    claims,
                    null);
            personObj = getPersonWithClaims(userClaims, userId);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving data for person " + userId, e);
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving data for person " + userId, e);

        }

        return personObj;

    }

    /**
     * Returns an array of persons that correspond to the passed in useIds
     *
     * @param userIds       Array of userIds
     * @param groupId       The group
     * @param filterOptions How to filter, sort and paginate the collection being fetched
     * @param fields        The profile details to fetch. Empty set implies all
     * @return An array of Person objects correspond tot the passed in userIds
     * @throws SocialDataException
     */
    public Person[] getPeople(String[] userIds, String groupId, FilterOptions filterOptions,
                              String[] fields) throws SocialDataException {
        //TODO: filter options
        List<String> userIdsToFetch = new ArrayList<String>();
        RelationshipManager relationshipManager;
        if (groupId.equalsIgnoreCase(SocialImplConstants.GROUP_ID_SELF)) {
            userIdsToFetch = new ArrayList(Arrays.asList(userIds));
        } else if (groupId.equalsIgnoreCase(SocialImplConstants.GROUP_ID_FRIENDS)) {
            relationshipManager = new RelationshipManagerImpl();
            for (String userId : userIds) {
                for (String id : relationshipManager.getRelationshipList(userId)) {
                    userIdsToFetch.add(id);
                }
            }
        } else if (groupId.equalsIgnoreCase(GroupId.Type.all.toString())) {
            try {
                userStoreManager = getUserStoreManager();
                // get all users using the filter *
                String[] userList = userStoreManager.listUsers(
                        SocialImplConstants.DEFAULT_USER_FILTER_STRING,
                        SocialImplConstants.DEFAULT_RETURN_ARRAY_SIZE);
                userIdsToFetch = new ArrayList(Arrays.asList(userList));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new SocialDataException("Error while retrieving user list", e);
            }
        }
        Person[] peopleArray = new Person[userIdsToFetch.size()];
        int index = 0;
        for (String id : userIdsToFetch) {
            peopleArray[index++] = getPerson(id, fields);
        }
        return peopleArray;
    }

    /**
     * Returns a person that corresponds to the passed in userIds
     *
     * @param userId The userId of the persons whose details to be fetched
     * @param fields The fields to be fetched
     * @return A Person object for passes in details
     * @throws SocialDataException
     */
    public Person getPerson(String userId, String[] fields) throws SocialDataException {
        Person personObj;
        String[] socialFields;
        try {
            userStoreManager = getUserStoreManager();
            if (!userStoreManager.isExistingUser(userId)) {
                return null;
            }
            socialFields = getPersonSocialFields(fields);
            Map<String, String> userClaims =
                    userStoreManager.getUserClaimValues(userId, socialFields, null);
            personObj = getPersonWithClaims(userClaims, userId);
            // Check for necessary fields
            if (personObj != null && personObj.getDisplayName() == null) {
                personObj.setDisplayName(userId);
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving data for person " + userId, e);
        }
        catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving data for person " + userId, e);
        }
        return personObj;
    }

    /**
     * Maps each person attribue to claim-uri
     *
     * @param person The Person object to retrieve attributes
     * @return A Map of claim-value pairs
     */
    private Map<String, String> retrieveClaimValues(Person person) {
        Map<String, String> claimValues = new HashMap<String, String>();
        // map each person attribute to claim-url
        if (person.getDisplayName() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_DISPLAY_NAME, person.getDisplayName());
        }
        if (person.getName() != null && person.getName().getGivenName() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_GIVEN_NAME,
                    person.getName().getGivenName());
        }
        if (person.getName() != null && person.getName().getFamilyName() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_FAMILY_NAME,
                    person.getName().getFamilyName());
        }
        if (person.getNickname() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_NICK_NAME, person.getNickname());
        }
        if (person.getOrganizations() != null && person.getOrganizations().get(0) != null &&
                person.getOrganizations().get(0).getName() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_ORGANIZATION,
                    person.getOrganizations().get(0).getName());
        }
        if (person.getAddresses() != null && person.getAddresses().get(0) != null) {
            if (person.getAddresses().get(0).getStreetAddress() != null) {
                claimValues.put(SocialImplConstants.CLAIM_URI_STREET_ADDRESS,
                        person.getAddresses().get(0).getStreetAddress());
            }
            if (person.getAddresses().get(0).getRegion() != null) {
                claimValues.put(SocialImplConstants.CLAIM_URI_REGION,
                        person.getAddresses().get(0).getRegion());
            }
            if (person.getAddresses().get(0).getCountry() != null) {
                claimValues.put(SocialImplConstants.CLAIM_URI_COUNTRY,
                        person.getAddresses().get(0).getCountry());
            }
            if (person.getAddresses().get(0).getLatitude() != null) {
                claimValues.put(SocialImplConstants.CLAIM_URI_LATITUDE,
                        person.getAddresses().get(0).getLatitude().toString());
            }
            if (person.getAddresses().get(0).getLongitude() != null) {
                claimValues.put(SocialImplConstants.CLAIM_URI_LONGITUDE,
                        person.getAddresses().get(0).getLongitude().toString());
            }
            if (person.getAddresses().get(0).getPostalCode() != null) {
                claimValues.put(SocialImplConstants.CLAIM_URI_POSTAL_CODE,
                        person.getAddresses().get(0).getPostalCode());
            }
        }
        if (person.getEmails() != null && person.getEmails().get(0) != null &&
                person.getEmails().get(0).getValue() != null) {

            claimValues.put(SocialImplConstants.CLAIM_URI_EMAIL,
                    person.getEmails().get(0).getValue());
        }
        if (person.getPhoneNumbers() != null && person.getPhoneNumbers().get(0) != null &&
                person.getPhoneNumbers().get(0).getValue() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_PHONE_NUMBER,
                    person.getPhoneNumbers().get(0).getValue());
        }
        if (person.getIms() != null && person.getIms().get(0) != null &&
                person.getIms().get(0).getValue() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_IM,
                    person.getIms().get(0).getValue());
        }
        if (person.getUrls() != null && person.getUrls().get(0) != null &&
                person.getUrls().get(0).getValue() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_URL,
                    person.getUrls().get(0).getValue());
        }
        if (person.getAboutMe() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_ABOUT_ME,
                    person.getAboutMe());
        }
        if (person.getBirthday() != null && person.getBirthday().getTime() > 0) {
            claimValues.put(SocialImplConstants.CLAIM_URI_BIRTHDAY,
                    person.getBirthday().getTime() + "");
        }
        if (person.getRelationshipStatus() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_RELATIONSHIP_STATUS,
                    person.getRelationshipStatus());
        }
        if (person.getReligion() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_RELIGIOUS_VIEW,
                    person.getReligion());
        }
        if (person.getEthnicity() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_ETHNICITY,
                    person.getEthnicity());
        }
        if (person.getGender() != null && person.getGender().name() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_GENDER,
                    person.getGender().name());
        }
        if (person.getPoliticalViews() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_POLITICAL_VIEW,
                    person.getPoliticalViews());
        }
        if (person.getInterests() != null && person.getInterests().get(0) != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_INTERESTS,
                    person.getInterests().get(0));
        }
        if (person.getBooks() != null && person.getBooks().get(0) != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_BOOKS,
                    person.getBooks().get(0));
        }
        if (person.getJobInterests() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_JOB_INTERESTS,
                    person.getJobInterests());
        }
        if (person.getLanguagesSpoken() != null && person.getLanguagesSpoken().get(0) != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_LANGUAGE_SPOKEN,
                    person.getLanguagesSpoken().get(0));
        }
        if (person.getLookingFor() != null && person.getLookingFor().get(0) != null &&
                person.getLookingFor().get(0).name() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_LOOKING_FOR,
                    person.getLookingFor().get(0).name());
        }
        if (person.getMovies() != null && person.getMovies().get(0) != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_MOVIES,
                    person.getMovies().get(0));
        }
        if (person.getMusic() != null && person.getMusic().get(0) != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_MUSIC,
                    person.getMusic().get(0));
        }
        if (person.getQuotes() != null && person.getQuotes().get(0) != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_QUOTES,
                    person.getQuotes().get(0));
        }
        if (person.getHappiestWhen() != null) {
            claimValues.put(SocialImplConstants.CLAIM_URI_HAPPIEST_WHEN,
                    person.getHappiestWhen());

        }
        return claimValues;
    }

    /**
     * Add the claim values to the Person object as attributes
     *
     * @param claimValues The claim values of the person
     * @param userId      id of the person
     * @return The Person object with attribute values added
     */
    private Person getPersonWithClaims(Map<String, String> claimValues, String userId) {
        Person person;
        String displayName = claimValues.get(SocialImplConstants.CLAIM_URI_DISPLAY_NAME);
        Name userName = new NameImpl();
        userName.setGivenName(claimValues.get(SocialImplConstants.CLAIM_URI_GIVEN_NAME));
        userName.setFamilyName(claimValues.get(SocialImplConstants.CLAIM_URI_FAMILY_NAME));
        person = new PersonImpl(userId, displayName, userName);
        String value;
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_NICK_NAME)) != null) {
            person.setNickname(value);
        }
        Organization org = new OrganizationImpl();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_ORGANIZATION)) != null) {
            org.setName(value);
        }
        List<Organization> orgsList = new ArrayList<Organization>();
        orgsList.add(org);
        person.setOrganizations(orgsList);
        Address address = new AddressImpl();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_STREET_ADDRESS)) != null) {
            address.setStreetAddress(value);
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_REGION)) != null) {
            address.setRegion(value);
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_COUNTRY)) != null) {
            address.setCountry(value);
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_LATITUDE)) != null) {
            address.setLatitude(Float.valueOf(value));
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_LONGITUDE)) != null) {
            address.setLongitude(Float.valueOf(value));
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_POSTAL_CODE)) != null) {
            address.setPostalCode(value);
        }
        List<Address> addressList = new ArrayList<Address>();
        addressList.add(address);
        person.setAddresses(addressList);
        List<ListField> emailList = new ArrayList<ListField>();
        ListField email = new ListFieldImpl();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_EMAIL)) != null) {
            email.setValue(value);
        }
        emailList.add(email);
        person.setEmails(emailList);
        List<ListField> phoneNumberList = new ArrayList<ListField>();
        ListField phoneNumber = new ListFieldImpl();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_PHONE_NUMBER)) != null) {
            phoneNumber.setValue(value);
        }
        phoneNumberList.add(phoneNumber);
        person.setPhoneNumbers(phoneNumberList);
        List<ListField> imList = new ArrayList<ListField>();
        ListField im = new ListFieldImpl();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_IM)) != null) {
            im.setValue(value);
        }
        imList.add(im);
        person.setIms(imList);
        List<Url> urlList = new ArrayList<Url>();
        Url url = new UrlImpl();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_URL)) != null) {
            url.setValue(value);
        }
        urlList.add(url);
        person.setUrls(urlList);
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_ABOUT_ME)) != null) {
            person.setAboutMe(value);
        }
        Date birthday = new Date();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_BIRTHDAY)) != null) {
            birthday.setTime(Long.valueOf(value));
        }
        person.setBirthday(birthday);
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_RELATIONSHIP_STATUS)) != null) {
            person.setRelationshipStatus(value);
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_RELIGIOUS_VIEW)) != null) {
            person.setReligion(value);
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_ETHNICITY)) != null) {
            person.setEthnicity(value);
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_GENDER)) != null) {
            person.setGender(Person.Gender.valueOf(value));
        }
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_POLITICAL_VIEW)) != null) {
            person.setPoliticalViews(value);
        }
        List<String> interest = new ArrayList<String>();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_INTERESTS)) != null) {
            interest.add(value);
        }
        person.setInterests(interest);
        List<String> books = new ArrayList<String>();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_BOOKS)) != null) {
            books.add(value);
        }
        person.setBooks(books);
        person.setJobInterests(claimValues.get(SocialImplConstants.CLAIM_URI_JOB_INTERESTS));
        List<String> languageSpoken = new ArrayList<String>();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_LANGUAGE_SPOKEN)) != null) {
            languageSpoken.add(value);
        }
        person.setLanguagesSpoken(languageSpoken);
        List<String> movieList = new ArrayList<String>();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_MOVIES)) != null) {
            movieList.add(value);
        }
        person.setMovies(movieList);
        List<String> musicList = new ArrayList<String>();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_MUSIC)) != null) {
            musicList.add(value);
        }
        person.setMusic(musicList);
        List<String> quotesList = new ArrayList<String>();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_QUOTES)) != null) {
            quotesList.add(value);
        }
        person.setQuotes(quotesList);
        person.setHappiestWhen(claimValues.get(SocialImplConstants.CLAIM_URI_HAPPIEST_WHEN));
        List<Enum<LookingFor>> lookingFor = new ArrayList<Enum<LookingFor>>();
        if ((value = claimValues.get(SocialImplConstants.CLAIM_URI_LOOKING_FOR)) != null) {
            lookingFor.add(LookingFor.valueOf(value));
        }
        person.setLookingFor(lookingFor);

        return person;
    }

    /**
     * Converts the fields in to related claim-uri
     *
     * @param fields The Person fields
     * @return An array of String with the claim-uris of the given fields
     */
    private String[] getPersonSocialFields(String[] fields) {
        List<String> socialFields = new ArrayList<String>();
        for (String field : fields) {
            field = field.trim();
            if (SocialImplConstants.FIELD_DISPLAY_NAME.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_DISPLAY_NAME);
            } else if (SocialImplConstants.FIELD_NAME.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_GIVEN_NAME);
                socialFields.add(SocialImplConstants.CLAIM_URI_FAMILY_NAME);
            } else if (SocialImplConstants.FIELD_NICKNAME.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_NICK_NAME);
            } else if (SocialImplConstants.FIELD_ADDRESSES.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_STREET_ADDRESS);
                socialFields.add(SocialImplConstants.CLAIM_URI_REGION);
                socialFields.add(SocialImplConstants.CLAIM_URI_COUNTRY);
                socialFields.add(SocialImplConstants.CLAIM_URI_LONGITUDE);
                socialFields.add(SocialImplConstants.CLAIM_URI_LATITUDE);
                socialFields.add(SocialImplConstants.CLAIM_URI_POSTAL_CODE);
            } else if (SocialImplConstants.FIELD_ABOUT_ME.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_ABOUT_ME);
            } else if (SocialImplConstants.FIELD_BIRTHDAY.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_BIRTHDAY);
            } else if (SocialImplConstants.FIELD_EMAILS.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_EMAIL);
            } else if (SocialImplConstants.FIELD_ETHNICITY.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_ETHNICITY);
            } else if (SocialImplConstants.FIELD_HAPPIEST_WHEN.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_HAPPIEST_WHEN);
            } else if (SocialImplConstants.FIELD_IM.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_IM);
            } else if (SocialImplConstants.FIELD_INTERESTS.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_INTERESTS);
            } else if (SocialImplConstants.FIELD_JOB_INTERESTS.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_JOB_INTERESTS);
            } else if (SocialImplConstants.FIELD_LANGUAGE_SPOKEN.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_LANGUAGE_SPOKEN);
            } else if (SocialImplConstants.FIELD_LOOKING_FOR.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_LOOKING_FOR);
            } else if (SocialImplConstants.FIELD_MUSIC.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_MUSIC);
            } else if (SocialImplConstants.FIELD_MOVIES.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_MOVIES);
            } else if (SocialImplConstants.FIELD_QUOTES.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_QUOTES);
            } else if (SocialImplConstants.FIELD_POLITICAL_VIEW.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_POLITICAL_VIEW);
            } else if (SocialImplConstants.FIELD_GENDER.equalsIgnoreCase(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_GENDER);
            } else if (SocialImplConstants.FIELD_RELATIONSHIP_STATUS.equalsIgnoreCase(
                    field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_RELATIONSHIP_STATUS);
            } else if (SocialImplConstants.FIELD_RELIGION.equals(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_RELIGIOUS_VIEW);
            } else if (SocialImplConstants.FIELD_PHONE_NUMBERS.equals(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_PHONE_NUMBER);
            } else if (SocialImplConstants.FIELD_BOOKS.equals(field)) {
                socialFields.add(SocialImplConstants.CLAIM_URI_BOOKS);
            }
            socialFields.add(SocialImplConstants.CLAIM_URI_DISPLAY_NAME);
        }
        return socialFields.toArray(new String[socialFields.size()]);
    }

    /**
     * Returns the claim url & display name sorted according to the display order
     *
     * @return A two-dimensional array of String containing claim url and display names
     *         The first row contains the claim-url values
     *         The second row contains the display names
     * @throws SocialDataException
     */
    public ClaimMapping[] getOrderedUserClaimInfo() throws SocialDataException {
        ClaimMapping[] userClaims;
//        String[][] claimInfo;
        try {
            claimManager = getClaimManager();

	    userClaims = claimManager.getAllSupportClaimMappingsByDefault();
           /* 
 		// Commented out and use the claim mapping instead here with the new changes

            userClaims = (Claim[]) claimManager.getAllSupportClaimsByDefault();
            // sort the claims in display order
            Arrays.sort(userClaims, new UserClaimComparator());
            claimInfo = new String[2][userClaims.length];
            int index = 0;
            // retrieve claims uris in sorted order
            for (Claim claim : userClaims) {
                claimInfo[0][index] = claim.getClaimUri();
                claimInfo[1][index++] = claim.getDisplayTag();
                
            } */
	

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving claims", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving claims", e);
        }

        return userClaims;
    }

    /**
     * Retrieves the display name of the given person id
     *
     * @param userId id of the person
     * @return the displayname
     * @throws SocialDataException
     */
    public String getDisplayName(String userId) throws SocialDataException {

        if (userId == null || userId.trim().equals("")) {
            return null;
        }
        Person person = getPerson(userId);
        return person.getDisplayName();

    }


    public String[][] getUserList(String filter,int maxItemLimit) throws SocialDataException {
        String[][] userList = new String[0][0];
        try {
            userStoreManager = getUserStoreManager();
            String[] userIdList = userStoreManager.listUsers(filter,maxItemLimit);
            if (userIdList != null) {
                userList = new String[2][userIdList.length];
                int index = 0;
                for (String userId : userIdList) {
                    userList[0][index]=userId;
                    userList[1][index++] = (getDisplayName(userId) != null) ? getDisplayName(userId) : userId;
                }
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving user-list", e);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new SocialDataException("Error while retrieving user-list", e);
        }
        return userList;
    }

    /**
     * A Comparator to sort claim urls according to display order
     */
    static class UserClaimComparator implements Comparator<Claim>, Serializable {

        public int compare(Claim claim1, Claim claim2) {
            if (claim1.getDisplayOrder() == 0) {
                claim1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (claim2.getDisplayOrder() == 0) {
                claim2.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (claim1.getDisplayOrder() < claim2.getDisplayOrder()) {
                return -1;
            }
            if (claim1.getDisplayOrder() == claim2.getDisplayOrder()) {
                return 0;
            }
            if (claim1.getDisplayOrder() > claim2.getDisplayOrder()) {
                return 1;
            }
            return 0;
        }

    }
}
