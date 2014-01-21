/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.social.impl.test.people.userprofile;

import org.apache.commons.dbcp.BasicDataSource;
import org.wso2.carbon.registry.social.api.people.userprofile.Person;
import org.wso2.carbon.registry.social.api.people.userprofile.model.ListField;
import org.wso2.carbon.registry.social.api.people.userprofile.model.Name;
import org.wso2.carbon.registry.social.impl.people.userprofile.PersonManagerImpl;
import org.wso2.carbon.registry.social.impl.people.userprofile.model.PersonImpl;
import org.wso2.carbon.registry.social.impl.people.userprofile.model.impl.ListFieldImpl;
import org.wso2.carbon.registry.social.impl.people.userprofile.model.impl.NameImpl;
import org.wso2.carbon.registry.social.impl.test.SocialImplTestConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.DefaultRealm;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.io.InputStream;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class PersonManagerImplTest extends BaseTestCase {

    private BasicDataSource ds;
    private PersonManagerImpl personManager;
    public static final String JDBC_TEST_USERMGT_XML = "user-mgt-test.xml";
    private static String TEST_URL = "jdbc:h2:target/PersonManagerImplTest/CARBON_TEST";
    private UserRealm realm = null;
    private Registry registry;

    public void setUp() throws Exception {
        super.setUp();
    }


    public void initObjStuff() throws Exception {

        String dbFolder = "target/PersonManagerTest";
        if ((new File(dbFolder)).exists()) {
            deleteDir(new File(dbFolder));
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(SocialImplTestConstants.DB_DRIVER);
        ds.setUrl(TEST_URL);
        DatabaseCreator creator = new DatabaseCreator(ds);
        creator.createRegistryDatabase();

        realm = new DefaultRealm();
        InputStream inStream = this.getClass().getClassLoader().getResource(
                PersonManagerImplTest.JDBC_TEST_USERMGT_XML).openStream();
        RealmConfiguration realmConfig = TestRealmConfigBuilder
                .buildRealmConfigWithJDBCConnectionUrl(inStream, TEST_URL);
        realm.init(realmConfig, ClaimTestUtil.getClaimTestData(), ClaimTestUtil
                .getProfileTestData(), 0);

    }

   /* public void testCreatePerson() throws Exception {
        initObjStuff();
        personManager = new PersonManagerImpl();
        personManager.setUserStoreManager(realm.getUserStoreManager());
        personManager.setClaimManager(realm.getClaimManager());
        Person person = new PersonImpl();
        person.setId("admin12");
        person.setNickname("abcd");
        person.setAboutMe("About Me");
        Name personName = new NameImpl();
        personName.setGivenName("Admin");
        personName.setFamilyName("Abcd");
        person.setName(personName);
        ListField email = new ListFieldImpl();
        email.setValue("admin@abcd.org");
        List<ListField> emails = new ArrayList<ListField>();
        emails.add(email);
        person.setEmails(emails);
        ListField telephone = new ListFieldImpl();
        telephone.setValue("99999999");
        List<ListField> teleList = new ArrayList<ListField>();
        teleList.add(telephone);
        person.setPhoneNumbers(teleList);
        personManager.savePerson("admin12", person);
        Person person2;
        person2 = personManager.getPerson("admin12", new String[]{"NAME", "EMAILS", "ABOUT_ME", "PHONE_NUMBERS"});
        assertNotNull(person2);
        assertEquals(person2.getId(), "admin12");
        assertNotNull(person2.getName());
        assertNotNull(person2.getAboutMe());
        assertEquals(person2.getAboutMe(), "About Me");
        assertNotNull(person2.getName().getGivenName());
        assertEquals(person2.getName().getGivenName(), "Admin");
        assertNotNull(person2.getEmails());
        assertNotNull(person2.getEmails().get(0));
        assertEquals(person2.getEmails().size(), 1);
        assertEquals(person2.getEmails().get(0).getValue(), "admin@abcd.org");
        assertEquals(person2.getPhoneNumbers().get(0).getValue(), "99999999");


    } */

    public void testGetPerson1() throws Exception {
        initObjStuff();
        personManager = new PersonManagerImpl();
        personManager.setUserStoreManager(realm.getUserStoreManager());
        personManager.setClaimManager(realm.getClaimManager());
        Person person = new PersonImpl();
        person.setId("admin");
        person.setNickname("abcd");
        person.setAboutMe("About Me");
        Name personName = new NameImpl();
        personName.setGivenName("Admin");
        personName.setFamilyName("Abcd");
        person.setName(personName);
        ListField email = new ListFieldImpl();
        email.setValue("admin@abcd.org");
        List<ListField> emails = new ArrayList<ListField>();
        emails.add(email);
        person.setEmails(emails);
        ListField telephone = new ListFieldImpl();
        telephone.setValue("99999999");
        List<ListField> teleList = new ArrayList<ListField>();
        teleList.add(telephone);
        person.setPhoneNumbers(teleList);
        personManager.savePerson("admin", person);
        Person person2 = personManager.getPerson("admin");
        assertNotNull(person2);
        assertEquals(person2.getEmails().get(0).getValue(), "admin@abcd.org");
        assertNull(person2.getAddresses().get(0).getCountry());
    }

    // TODO: testCreatePerson(),testUpdatePerson(),testGetPeople(),testRemovePerson() will be added back after
    // TODO: update the social api/impl properly with mapped to opensocial 2.0
   /* public void testUpdatePerson() throws Exception {
        initObjStuff();
        personManager = new PersonManagerImpl();
        personManager.setUserStoreManager(realm.getUserStoreManager());
        personManager.setClaimManager(realm.getClaimManager());
        Person person = new PersonImpl();
        person.setId("abcd");
        person.setNickname("abcdQQ");
        person.setAboutMe("About Me");
        Name personName = new NameImpl();
        personName.setGivenName("Admin");
        personName.setFamilyName("Abcd");
        person.setName(personName);
        ListField email = new ListFieldImpl();
        email.setValue("abcd@abcd.org");
        List<ListField> emails = new ArrayList<ListField>();
        emails.add(email);
        person.setEmails(emails);
        ListField telephone = new ListFieldImpl();
        telephone.setValue("123456");
        List<ListField> teleList = new ArrayList<ListField>();
        teleList.add(telephone);
        person.setPhoneNumbers(teleList);
        personManager.savePerson("abcd", person);
        Person person2 = personManager.getPerson("abcd");
        assertNotNull(person2);
        person.setAboutMe("My name is abcd");
        personManager.updatePerson("abcd", person);
        person2 = personManager.getPerson("abcd");
        assertNotNull(person2);
        assertEquals(person2.getAboutMe(), "My name is abcd");


    }*/

   /* public void testRemovePerson() throws Exception {
        initObjStuff();
        personManager = new PersonManagerImpl();
        personManager.setUserStoreManager(realm.getUserStoreManager());
        personManager.setClaimManager(realm.getClaimManager());
        Person person = new PersonImpl();
        person.setId("wwww");
        person.setNickname("abcd");
        person.setAboutMe("About Me");
        Name personName = new NameImpl();
        personName.setGivenName("Admin");
        personName.setFamilyName("Abcd");
        person.setName(personName);
        ListField email = new ListFieldImpl();
        email.setValue("admin@abcd.org");
        List<ListField> emails = new ArrayList<ListField>();
        emails.add(email);
        person.setEmails(emails);
        ListField telephone = new ListFieldImpl();
        telephone.setValue("99999999");
        List<ListField> teleList = new ArrayList<ListField>();
        teleList.add(telephone);
        person.setPhoneNumbers(teleList);
        personManager.savePerson("wwww", person);
        Person person2;
        person2 = personManager.getPerson("wwww1", new String[]{"NAME", "EMAILS", "ABOUT_ME", "PHONE_NUMBERS"});
        assertNull(person2);
        person2 = personManager.getPerson("wwww1");
        assertNull(person2);
        person2 = personManager.getPerson("wwww");
        assertNotNull(person2);
        personManager.removePerson("wwww");
        person2 = personManager.getPerson("wwww");
        assertNull(person2);


    }

    public void testGetPeople() throws Exception {
        initObjStuff();
        personManager = new PersonManagerImpl();
        personManager.setUserStoreManager(realm.getUserStoreManager());
        personManager.setClaimManager(realm.getClaimManager());
        Person person = new PersonImpl();
        person.setId("user1");
        person.setNickname("User-1");
        person.setAboutMe("About Me");
        Name personName = new NameImpl();
        personName.setGivenName("User1");
        personName.setFamilyName("Abcd");
        person.setName(personName);
        ListField email = new ListFieldImpl();
        email.setValue("user1@abcd.org");
        List<ListField> emails = new ArrayList<ListField>();
        emails.add(email);
        person.setEmails(emails);
        ListField telephone = new ListFieldImpl();
        telephone.setValue("99999999");
        List<ListField> teleList = new ArrayList<ListField>();
        teleList.add(telephone);
        person.setPhoneNumbers(teleList);
        personManager.savePerson("user1", person);
        Person[] person2;
        person2 = personManager.getPeople(new String[]{"user1"}, "SELF", null, new String[]{"NAME", "EMAILS", "ABOUT_ME", "PHONE_NUMBERS"});
        assertNotNull(person2);
        assertEquals(person2.length, 1);
        person.setId("user2");
        person.setNickname("User-2");
        person.setAboutMe("About Me");
        personName = new NameImpl();
        personName.setGivenName("User2");
        personName.setFamilyName("Abcd");
        person.setName(personName);
        email = new ListFieldImpl();
        email.setValue("user2@abcd.org");
        emails = new ArrayList<ListField>();
        emails.add(email);
        person.setEmails(emails);
        telephone = new ListFieldImpl();
        telephone.setValue("123456");
        teleList = new ArrayList<ListField>();
        teleList.add(telephone);
        person.setPhoneNumbers(teleList);
        personManager.savePerson("user2", person);
        person2 = personManager.getPeople(new String[]{"user1"}, "ALL", null, new String[]{"NAME", "EMAILS", "ABOUT_ME", "PHONE_NUMBERS"});
        assertNotNull(person2);
        //user1, user2, admin,www,abcd -- all users added in the test
        assertEquals(person2.length, 5);
         person2 = personManager.getPeople(new String[]{"user1","user2"}, "SELF", null, new String[]{"NAME", "EMAILS", "ABOUT_ME", "PHONE_NUMBERS"});
        assertNotNull(person2);
        assertEquals(person2.length, 2);
        assertEquals(person2[0].getAboutMe(), "About Me");
        //TODO: test GroupId=FRIENDS

    } */
}